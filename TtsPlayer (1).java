package my.mmu.rssnewsreader.service.tts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import my.mmu.rssnewsreader.R;
import my.mmu.rssnewsreader.data.entry.EntryRepository;
import my.mmu.rssnewsreader.data.sharedpreferences.SharedPreferencesRepository;
import my.mmu.rssnewsreader.ui.webview.WebViewActivity;
import my.mmu.rssnewsreader.ui.webview.WebViewListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;


@Singleton
public class TtsPlayer extends PlayerAdapter implements TtsPlayerListener {

    public static final String TAG = TtsPlayer.class.getSimpleName();

    private TextToSpeech tts;
    private PlaybackStateListener listener;
    private MediaSessionCompat.Callback callback;
    private WebViewListener webViewCallback;
    private Context context;
    private final TtsExtractor ttsExtractor;
    private final EntryRepository entryRepository;
    private final SharedPreferencesRepository sharedPreferencesRepository;

    private int sentenceCounter;
    private List<String> sentences = new ArrayList<>();

    private CountDownLatch countDownLatch;
    private int currentState;
    private long currentId = 0;
    private long feedId = 0;
    private String language;
    private boolean isInit = false;
    private boolean isPreparing = false;
    private boolean actionNeeded = false;
    private boolean isPausedManually;
    private boolean webViewConnected = false;
    private boolean uiControlPlayback = false;
    private boolean isManualSkip = false;
    private boolean isArticleFinished = false;
    private boolean isSettingUpNewArticle = false;
    private MediaPlayer mediaPlayer;
    private String currentUtteranceID = null;
    private boolean hasSpokenAfterSetup = false;
    private PlaybackUiListener playbackUiListener;
    private int currentExtractProgress = 0;

    @Inject
    public TtsPlayer(@ApplicationContext Context context, TtsExtractor ttsExtractor, EntryRepository entryRepository, SharedPreferencesRepository sharedPreferencesRepository) {
        super(context);
        this.ttsExtractor = ttsExtractor;
        this.entryRepository = entryRepository;
        this.sharedPreferencesRepository = sharedPreferencesRepository;
        this.context = context;
        this.isPausedManually = sharedPreferencesRepository.getIsPausedManually();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initTts(TtsService ttsService, PlaybackStateListener listener, MediaSessionCompat.Callback callback) {
        this.listener = listener;
        this.callback = callback;
        tts = new TextToSpeech(ttsService, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "initTts successful");
                isInit = true;

                if (actionNeeded) {
                    Log.d(TAG, "Deferred auto-play activated — TTS is now ready");
                    setupTts();
                    if (!isPausedManually) {
                        speak();
                    } else {
                        Log.d(TAG, "Deferred play skipped due to manual pause");
                    }
                    actionNeeded = false;
                }
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                if (isManualSkip) {
                    Log.d(TAG, "Manual skip — skipping sentenceCounter++ in onDone");
                    isManualSkip = false;

                    if (sentenceCounter < sentences.size() - 1) {
                        sentenceCounter++;
                        speak();
                        entryRepository.updateSentCount(sentenceCounter, currentId);
                        Log.d(TAG, "Manual skip done. Continuing at [#" + sentenceCounter + "]");
                    } else {
                        Log.d(TAG, "Manual skip finished last sentence. Moving to next article.");
                        entryRepository.updateSentCount(0, currentId);
                        sentenceCounter = 0;
                        isArticleFinished = true;
                        callback.onSkipToNext();
                    }
                    return;
                }

                if (isArticleFinished) {
                    Log.d(TAG, "Already finished article, skipping duplicate onDone");
                    return;
                }

                if (sentenceCounter < sentences.size() - 1) {
                    sentenceCounter++;
                    speak();
                    entryRepository.updateSentCount(sentenceCounter, currentId);
                    Log.d(TAG, "Finished [#" + sentenceCounter + "]: " + sentences.get(sentenceCounter));
                } else {
                    Log.d(TAG, "Finished last sentence. Moving to next article.");
                    entryRepository.updateSentCount(0, currentId);
                    sentenceCounter = 0;
                    isArticleFinished = true;
                    callback.onSkipToNext();
                }
            }

            @Override
            public void onError(String s) {
                Log.d("TTS", "onError");
            }
        });
    }

    public interface PlaybackUiListener {
        void onPlaybackStarted();
        void onPlaybackPaused();
    }

    public void setPlaybackUiListener(PlaybackUiListener listener) {
        this.playbackUiListener = listener;
    }

    public void stopTtsPlayback() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }

        currentId = -1;
        isPausedManually = false;
        isPreparing = false;
        isArticleFinished = false;
        isSettingUpNewArticle = false;
        setUiControlPlayback(false);
        if (playbackUiListener != null) {
            playbackUiListener.onPlaybackPaused();
        }
    }

    public void pauseTts() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
        isPausedManually = true;
        sharedPreferencesRepository.setIsPausedManually(true);
        setNewState(PlaybackStateCompat.STATE_PAUSED);
        setUiControlPlayback(false);
        if (playbackUiListener != null) {
            playbackUiListener.onPlaybackPaused();
        }
    }

    public void extract(long currentId, long feedId, String content, String language) {
        Log.d(TAG, "Switching to new article: ID=" + currentId);

        boolean wasSpeaking = tts != null && tts.isSpeaking();
        isPausedManually = !wasSpeaking && sharedPreferencesRepository.getIsPausedManually();
        sharedPreferencesRepository.setIsPausedManually(isPausedManually);
        Log.d(TAG, "Detected isPausedManually = " + isPausedManually);

        if (tts != null && tts.isSpeaking()) {
            Log.d(TAG, "stop current TTS");
            tts.stop();
        }

        isPreparing = true;
        isSettingUpNewArticle = true;
        sentences = new ArrayList<>();
        isArticleFinished = false;

        this.language = language;
        this.currentId = currentId;
        this.feedId = feedId;
        hasSpokenAfterSetup = false;
        countDownLatch = new CountDownLatch(1);

        if (language != null && !language.isEmpty()) {
            ttsExtractor.setCurrentLanguage(language, true);
            Log.d(TAG, "[extract] Locked language = " + language);
        }

        if (content != null) {
            new Thread(() -> {
                extractToTts(content, language);

                try {
                    countDownLatch.await();

                    if (isInit) {
                        setupTts();
                        if (!isPausedManually) {
                            Log.d(TAG, "TTS ready and not manually paused — auto speaking");
                            speak();
                        } else {
                            Log.d(TAG, "TTS ready but paused manually — not speaking");
                        }
                    } else {
                        Log.d(TAG, "TTS not initialized yet, setting actionNeeded = true");
                        actionNeeded = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            ttsExtractor.setCallback(this);
            ttsExtractor.prioritize();
        }
    }

    @Override
    public void extractToTts(String content, String language) {
        if (tts == null) {
            Log.w(TAG, "TTS engine is not initialized.");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            Log.w(TAG, "extractToTts: No content provided.");
            return;
        }

        sentences.clear();

        String[] raw = content.split(Pattern.quote(ttsExtractor.delimiter));
        List<String> sentenceList = new ArrayList<>(raw.length);
        for(String part : raw) {
            String trimmed = part.trim();
            if(! trimmed.isEmpty()) {
                sentenceList.add(trimmed);
            }
        }
        int totalSentences = sentenceList.size();

        new Thread(() -> {
            for (int i = 0; i < sentenceList.size(); i++) {
                String sentence = sentenceList.get(i);
                if (sentence.length() >= TextToSpeech.getMaxSpeechInputLength()) {
                    BreakIterator iterator = BreakIterator.getSentenceInstance();
                    iterator.setText(sentence);
                    int start = iterator.first();
                    for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                        sentences.add(sentence.substring(start, end));
                    }
                } else {
                    sentences.add(sentence);
                }

                currentExtractProgress = Math.min((int) (((double) sentences.size() / totalSentences) * 100), 95);

                if (i % 3 == 0 || i == sentenceList.size() - 1) {
                    ContextCompat.getMainExecutor(context).execute(() -> {
                        if (webViewCallback != null) {
                            webViewCallback.updateLoadingProgress(currentExtractProgress);
                        }
                    });
                }
            }

            if (sentences.size() < 2) {
                if (webViewCallback != null) webViewCallback.askForReload(feedId);
                sentences.clear();
                actionNeeded = false;
                return;
            } else {
                int savedProgress = entryRepository.getSentCount(currentId);
                sentenceCounter = Math.min(savedProgress, sentences.size() - 1);

                if (!isInit) {
                    Log.d(TAG, "TTS not initialized yet");
                    actionNeeded = true;
                } else {
                    Log.d(TAG, "TTS is initialized");
                    setupTts();
                }
            }
            countDownLatch.countDown();
        }).start();
    }

    private void setupTts() {
        ContextCompat.getMainExecutor(context).execute(() -> {
            Log.d(TAG, "[setupTts] currentLanguage = " + language + ", isLockedByTtsPlayer = " + ttsExtractor.isLocked() + ", ttsExtractor.language = " + ttsExtractor.getCurrentLanguage());
            if (sentences == null || sentences.isEmpty()) {
                Log.w(TAG, "No content to read in setupTts(), skipping...");
                return;
            }

            isPreparing = false;

            if (webViewCallback != null) {
                webViewCallback.finishedSetup();
                webViewCallback.updateLoadingProgress(100);
                webViewCallback.hideFakeLoading();
            }

            if (webViewCallback instanceof WebViewActivity) {
                ((WebViewActivity) webViewCallback).syncLoadingWithTts();
            }

            if (language == null || language.isEmpty()) {
                Log.w(TAG, "Warning: Language is null or empty, defaulting to English.");
                language = "en";
            }

            try {
                Log.d(TAG, "Setting TTS language to: " + language);
                Log.d(TAG, "setupTts() using language: " + language);
                setLanguage(new Locale(language), true);
            } catch (Exception e) {
                Log.d(TAG, "Invalid locale " + e.getMessage());
                setLanguage(Locale.ENGLISH, true);
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isSettingUpNewArticle = false;

                if (sentences.size() > 0 && !isPausedManually && !hasSpokenAfterSetup) {
                    hasSpokenAfterSetup = true;
                    speak();
                    if (webViewCallback != null) {
                        Log.d(TAG, "Hiding fake loading after TTS starts.");
                        webViewCallback.hideFakeLoading();
                    } else {
                        Log.w(TAG, "webViewCallback is null, cannot hideFakeLoading.");
                    }
                } else {
                    Log.d(TAG, "TTS ready, but paused manually or no content. Waiting for user to resume.");
                }
            }, 200);
        });
    }

    private void identifyLanguage(String sentence, boolean fromService) {
        float confidenceThreshold = (float) sharedPreferencesRepository.getConfidenceThreshold() / 100;

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(confidenceThreshold)
                        .build());
        languageIdentifier.identifyLanguage(sentence)
                .addOnSuccessListener(languageCode -> {
                    if (languageCode.equals("und")) {
                        Log.i(TAG, "Can't identify language.");
                        setLanguage(Locale.ENGLISH, fromService);
                    } else {
                        Log.i(TAG, "Language: " + languageCode);
                        setLanguage(new Locale(languageCode), fromService);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void setLanguage(Locale locale, boolean fromService) {
        if (tts == null) {
            return;
        }
        int result = tts.setLanguage(locale);

        Log.d(TAG, "setLanguage() called with: " + locale.toString());
        Log.d(TAG, "setLanguage() result: " + result);

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.d(TAG, "Language not supported" + locale);
            if (webViewCallback != null) {
                webViewCallback.makeSnackbar("Language not installed. Required language: " + locale.getDisplayLanguage());
            }
            tts.setLanguage(Locale.ENGLISH);
        }
        else {
            Log.d(TAG, "Language successfully set to: " + locale);
        }

        if (fromService) {
            callback.onCustomAction("playFromService", null);
        } else {
            Log.d(TAG, "Language set. Waiting for speak() to be called.");
        }
    }

    public void speak() {
        if (isSettingUpNewArticle) {
            Log.d(TAG, "TTS setup in progress, skipping speak()");
            return;
        }

        if (!isInit || tts == null) {
            Log.d(TAG, "speak() skipped — TTS not initialized yet. Waiting for init.");
            actionNeeded = true;
            return;
        }
        if (sentences == null || sentences.size() == 0) {
            Log.d(TAG, "Waiting latch");
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (sentenceCounter < 0) sentenceCounter = 0;

        if (sentenceCounter >= sentences.size()) {
            Log.d(TAG, "sentenceCounter out of bounds, skipping speak()");
            return;
        }

        if (sentences.size() != 0) {
            Log.d(TAG, "speak() with language = " + language + ", sentence = " + sentences.get(sentenceCounter));

            if (language == null) {
                identifyLanguage(sentences.get(sentenceCounter), false);
            } else {
                String sentence = sentences.get(sentenceCounter);
                Log.d(TAG, "TTS Speaking [#" + sentenceCounter + "]: " + sentence);
                int queueMode = isManualSkip ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
                tts.speak(sentence, queueMode, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
                setUiControlPlayback(true);
                setNewState(PlaybackStateCompat.STATE_PLAYING);
                if (playbackUiListener != null) {
                    playbackUiListener.onPlaybackStarted();
                }
                if (webViewCallback != null) {
                    webViewCallback.highlightText(sentence);
                }
            }
        }
    }

    public void fastForward() {
        if (tts != null && sentenceCounter < sentences.size() - 1) {
            isManualSkip = true;
            sentenceCounter++;
            entryRepository.updateSentCount(sentenceCounter, currentId);
            tts.stop();
            speak();
        } else {
            entryRepository.updateSentCount(0, currentId);
            callback.onSkipToNext();
        }
    }

    public void fastRewind() {
        if (tts != null && sentenceCounter > 0) {
            isManualSkip = true;
            sentenceCounter--;
            entryRepository.updateSentCount(sentenceCounter, currentId);
            tts.stop(); // interrupt current sentence
            speak();
        }
    }

    @Override
    public boolean isPlayingMediaPlayer() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void setupMediaPlayer(boolean forced) {
        if (forced) {
            stopMediaPlayer();
        }

        if (mediaPlayer == null && sharedPreferencesRepository.getBackgroundMusic()) {
            if (sharedPreferencesRepository.getBackgroundMusicFile().equals("default")) {
                mediaPlayer = MediaPlayer.create(context, R.raw.pianomoment);
            } else {
                File savedFile = new File(context.getFilesDir(), "user_file.mp3");
                if (savedFile.exists()) {
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(savedFile.getAbsolutePath()));
                } else {
                    mediaPlayer = MediaPlayer.create(context, R.raw.pianomoment);
                }
            }
            mediaPlayer.setLooping(true);
            changeMediaPlayerVolume();
        }
        playMediaPlayer();
    }

    @Override
    public void playMediaPlayer() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    @Override
    public void pauseMediaPlayer() {
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    public void changeMediaPlayerVolume() {
        if (mediaPlayer != null) {
            float volume = (float) sharedPreferencesRepository.getBackgroundMusicVolume() / 100;
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        return tts != null && tts.isSpeaking();
    }

    @Override
    protected void onPlay() {
        if (tts != null && !isPausedManually) {
            speak();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
        setNewState(PlaybackStateCompat.STATE_PAUSED);
    }

    @Override
    protected void onStop() {
        stopMediaPlayer();
        Log.d(TAG, " player stopped");
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInit = false;
        }
        currentId = 0;
        setNewState(PlaybackStateCompat.STATE_STOPPED);
    }

    private void setNewState(@PlaybackStateCompat.State int state) {
        if (listener != null) {
            currentState = state;
            final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(getAvailableActions());
            stateBuilder.setState(currentState, 0, 1.0f, SystemClock.elapsedRealtime());
            listener.onPlaybackStateChange(stateBuilder.build());
        }
    }

    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_REWIND
                | PlaybackStateCompat.ACTION_FAST_FORWARD;
        switch (currentState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    public void setTtsSpeechRate(float speechRate) {
        if (speechRate == 0) {
            try {
                int systemRate = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE);
                speechRate = systemRate / 100.0f;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                speechRate = 1.0f;
            }
        }
        tts.setSpeechRate(speechRate);
    }

    public void setWebViewCallback(WebViewListener listener) {
        this.webViewCallback = listener;
    }

    public WebViewListener getWebViewCallback() {
        return webViewCallback;
    }

    public void showFakeLoading() {
        if (webViewCallback != null) {
            webViewCallback.showFakeLoading();
        }
    }

    public void hideFakeLoading() {
        if (webViewCallback != null) {
            webViewCallback.hideFakeLoading();
        }
    }

    public boolean ttsIsNull() {
        return tts == null;
    }

    public boolean isWebViewConnected() {
        return webViewConnected;
    }

    public void setWebViewConnected(boolean isConnected) {
        this.webViewConnected = isConnected;
    }

    public boolean isUiControlPlayback() {
        return uiControlPlayback;
    }

    public void setUiControlPlayback(boolean isUiControlPlayback) {
        this.uiControlPlayback = isUiControlPlayback;
    }

    public long getCurrentId() {
        return currentId;
    }

    public boolean isPausedManually() {
        return isPausedManually;
    }

    public void setPausedManually(boolean isPaused) {
        sharedPreferencesRepository.setIsPausedManually(isPaused);
        isPausedManually = isPaused;
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    public int getCurrentExtractProgress() {
        return currentExtractProgress;
    }
}
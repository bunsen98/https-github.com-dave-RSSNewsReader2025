package my.mmu.rssnewsreader.service.util;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import my.mmu.rssnewsreader.data.sharedpreferences.SharedPreferencesRepository;

public class TextUtil {
    public static final String TAG = TextUtil.class.getSimpleName();
    private final CompositeDisposable compositeDisposable;
    private final SharedPreferencesRepository sharedPreferencesRepository;

    @Inject
    public TextUtil(SharedPreferencesRepository sharedPreferencesRepository) {
        this.sharedPreferencesRepository = sharedPreferencesRepository;
        compositeDisposable = new CompositeDisposable();
    }

    public String extractHtmlContent(String html, String delimiter) {
        if (html == null) {
            return "";
        }

        Document doc = Jsoup.parse(html);
        StringBuilder content = new StringBuilder();

        // Using CSS selector to directly access the required elements
        String cssQuery = "h2, h3, h4, h5, h6, p, td, pre, th, li, figcaption, blockquote, section";
        Elements elements = doc.select(cssQuery);

        // Iterate over the selected elements and append them to the StringBuilder
        for (Element element : elements) {
            content.append(element.text());
            content.append(delimiter);  // Append the delimiter after each element's text
        }

        return content.toString().trim();  // Return the trimmed result to remove the last delimiter
    }

    // Translate text element by element
    // Pro: Preserves the HTML structure of the text (e.g. <h1> remains <h1>, <h2> remains <h2>, <p> remains <p>)
    // Con: Slower performance (e.g. translating a very long content (198 elements) can take up to 5 minutes.
    //        In contrast, using the translateAllAtOnce method reduces this time to 2 minutes).
    // Note: Specifying maxConcurrency in x.flatMap (tried with 10 and 100) showed no noticeable difference in performance
    //        compared to leaving it unspecified.
    public Single<String> translateHtmlLineByLine(String sourceLanguage, String targetLanguage, String html, String title, long articleId, Consumer<Integer> progressCallback) {
        Log.d(TAG, "translateHtmlLineByLine: from " + sourceLanguage + " to " + targetLanguage);
        return Single.create(emitter -> {
            try {
                // First, translate the title
                translateText(sourceLanguage, targetLanguage, title)
                        .flatMap(translatedTitle -> {
                            // Parse the HTML
                            Document document = Jsoup.parse(html);
                            // List of tags to extract text from
                            List<String> tags = Arrays.asList("h2", "h3", "h4", "h5", "h6", "p", "td", "pre", "th", "li", "figcaption", "blockquote", "section");
                            // Get all elements with the specified tags
                            Elements elements = document.select(String.join(",", tags));

                            // Check if the translated title has already been prepended
                            Element existingTitleElement = document.select("p.translated-title").first();
                            if (existingTitleElement == null) {
                                Element titleParagraph = new Element(Tag.valueOf("p"), "");
                                titleParagraph.text(translatedTitle);
                                titleParagraph.addClass("translated-title");
                                titleParagraph.attr("data-article-id", String.valueOf(articleId));
                                document.body().prependChild(titleParagraph);
                            }

                            AtomicInteger translatedElements = new AtomicInteger(0);
                            // Create a Flowable from the elements
                            return Flowable.fromIterable(elements)
                                    .flatMapMaybe(element -> {
                                        if (element.hasText()) {
                                            return translateText(sourceLanguage, targetLanguage, element.text())
                                                    .map(translatedText -> {
                                                        element.text(translatedText);
                                                        return translatedText;
                                                    })
                                                    .toMaybe();
                                        }
                                        return Maybe.empty();
                                    })
                                    .doOnNext(translatedText -> {
                                        // Emit progress update
                                        int progress = (int) (100.0 * (translatedElements.incrementAndGet()) / elements.size());
                                        progressCallback.accept(progress);
                                    })
                                    .toList()
                                    .map(ignored -> document.outerHtml());
                        })
                        .subscribe(
                                emitter::onSuccess,
                                emitter::onError
                        );
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Single<String> translateHtmlLineByLine(String sourceLanguage, String targetLanguage, String html, String title, long articleId) {
        Log.d(TAG, "translateHtmlLineByLine: from " + sourceLanguage + " to " + targetLanguage);
        return Single.create(emitter -> {
            try {
                // First, translate the title
                translateText(sourceLanguage, targetLanguage, title)
                        .flatMap(translatedTitle -> {
                            // Parse the HTML
                            Document document = Jsoup.parse(html);
                            // List of tags to extract text from
                            List<String> tags = Arrays.asList("h2", "h3", "h4", "h5", "h6", "p", "td", "pre", "th", "li", "figcaption", "blockquote", "section");
                            // Get all elements with the specified tags
                            Elements elements = document.select(String.join(",", tags));

                            Element existingTitleElement = document.select("p.translated-title").first();
                            if (existingTitleElement == null) {
                                Element titleParagraph = new Element(Tag.valueOf("p"), "");
                                titleParagraph.text(translatedTitle);
                                titleParagraph.addClass("translated-title");
                                titleParagraph.attr("data-article-id", String.valueOf(articleId));
                                document.body().prependChild(titleParagraph);
                            }

                            // Create a Flowable from the elements
                            return Flowable.fromIterable(elements)
                                    .flatMapMaybe(element -> {
                                        if (element.hasText()) {
                                            return translateText(sourceLanguage, targetLanguage, element.text()).map(translateText -> {
                                                element.text(translateText);
                                                return translateText;
                                            }).toMaybe();
                                        }
                                        return Maybe.empty();
                                    })
                                    .toList()
                                    .map(ignored -> document.outerHtml());
                        })
                        .subscribe(
                                emitter::onSuccess,
                                emitter::onError
                        );
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }


    // Translation Method: Concat texts from all the elements then translate the concatenated text
    // Pro: Faster performance. (e.g. translating very long content (198 elements) takes only 2 minutes,
    //      compared to 5 minutes with the translateLineByLine method.
    //      (However, it's worth noting that despite being faster, this method's speed is still limited due to the MLKit Model's lack of optimization for long text. The speed of translation also heavily depends on the device specifications, with devices having more memory typically performing faster due to the use of TensorFlow as the backbone.)
    // Con:
    // 1. Special tags are replaced with <p>. Attempting to retain original tags such as <h1> or <h2> often results in errors due to discrepancies in
    //    element count after splitting the translated string using a delimiter (e.g. totalTextToTranslate: 198, totalTranslatedTexts: 210).
    //    Therefore, all tags are replaced with <p>.
    // 2. Due to limitations in the MLKit API translation model, it may not accurately separate lines, resulting in some text remaining untranslated.
    // Note:
    // 1. The delimiter used for splitting can also be translated (e.g., original delimiter ===@@@=== might become ==@== or @@ after translation). Therefore, a regex is used to split the translated text.
    // 2. The choice of delimiter can affect the translation. After testing various options like <br>, &nbsp;, and other character combinations, "++++++@@@@@@++++++" gave the best results.
    // 3. The accuracy of translation can sometimes be compromised, resulting in unusual or unexpected translations.
    // 4. MLKit uses English as an intermediate language for translation. For example, when translating from Chinese to Malay, the process is actually Chinese -> English -> Malay. This indirect translation process may affect the quality of the final translation.
    public Single<String> translateHtmlAllAtOnce(String sourceLanguage, String targetLanguage, String html, String title, long articleId, Consumer<Integer> progressCallback) {
        Log.d(TAG, "translateHtmlAllAtOnce: from " + sourceLanguage + " to " + targetLanguage);
        return Single.create(emitter -> {
            try {
                // First, translate the title
                translateText(sourceLanguage, targetLanguage, title)
                        .flatMap(translatedTitle -> {
                            // Parse the HTML
                            Document document = Jsoup.parse(html);
                            // List of tags to extract text from
                            List<String> tags = Arrays.asList("h2", "h3", "h4", "h5", "h6", "p", "td", "pre", "th", "li", "figcaption", "blockquote", "section");
                            // Get all elements with the specified tags
                            Elements elements = document.select(String.join(",", tags));

                            // Check if the translated title has already been prepended
                            Element existingTitleElement = document.select("p.translated-title").first();
                            if (existingTitleElement == null) {
                                Element titleParagraph = new Element(Tag.valueOf("p"), "");
                                titleParagraph.text(translatedTitle);
                                titleParagraph.addClass("translated-title");
                                titleParagraph.attr("data-article-id", String.valueOf(articleId));
                                document.body().prependChild(titleParagraph);
                            }

                            // Unique delimiter
                            String delimiter = "++++++@@@@@@++++++";
                            // Concatenate all the text
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Element element : elements) {
                                String text = element.text();
                                stringBuilder.append(text);
                                stringBuilder.append(delimiter);
                            }
                            Log.d(TAG, "translateHtml: translating " + elements.size() + " elements");
                            if (elements.isEmpty()) {
                                emitter.onSuccess(document.outerHtml());
                                return Single.just(document.outerHtml());
                            }

                            AtomicInteger progress = new AtomicInteger(0);
                            Thread progressThread = new Thread(() -> {
                                try {
                                    while (progress.get() < 90) {
                                        Thread.sleep(300);
                                        try {
                                            progressCallback.accept(progress.incrementAndGet());
                                        } catch (Throwable callbackException) {
                                            Log.e(TAG, "Progress callback failed", callbackException);
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt(); // Restore interrupted status
                                }
                            });
                            progressThread.start();

                            String combinedText = stringBuilder.toString();
                            return translateText(sourceLanguage, targetLanguage, combinedText)
                                    .map(translatedText -> {
                                        progressThread.interrupt(); // Stop progress simulation
                                        try {
                                            progressCallback.accept(100); // Finalize progress
                                        } catch (Throwable callbackException) {
                                            Log.e(TAG, "Progress callback failed on completion", callbackException);
                                        }
                                        Log.d(TAG, "translateHtml: translatedText: " + translatedText);
                                        String[] translatedTexts = translatedText.split("((\\+ *){1,} *(@ *)* *(\\+ *){1,})|((\\+ *)* *(@ *){2,} *(\\+ *)*)");
                                        Log.d(TAG, "translateHtml: totalTextstoTranslate: " + elements.size() + ", totalTranslatedTexts: " + translatedTexts.length);
                                        // If total translatedTexts is less or equal than the total elements, replace the text then remove additional elements
                                        if (translatedTexts.length <= elements.size()) {
                                            for (int i = 0; i < translatedTexts.length; i++) {
                                                Element originalElement = elements.get(i);
                                                Element newElement = new Element(Tag.valueOf("p"), "");
                                                newElement.text(translatedTexts[i]);
                                                originalElement.replaceWith(newElement);
                                            }
                                            for (int i = translatedTexts.length; i < elements.size(); i++) {
                                                elements.remove(elements.get(i));
                                            }
                                        }
                                        // If total translatedTexts is more than the total elements, add additional elements
                                        else {
                                            for (int i = 0; i < elements.size(); i++) {
                                                Element originalElement = elements.get(i);
                                                Element newElement = new Element(Tag.valueOf("p"), "");
                                                newElement.text(translatedTexts[i]);
                                                originalElement.replaceWith(newElement);
                                            }
                                            for (int i = elements.size(); i < translatedTexts.length; i++) {
                                                Element newElement = new Element(Tag.valueOf("p"), "");
                                                newElement.text(translatedTexts[i]);
                                                elements.add(newElement);
                                            }
                                        }
                                        return document.outerHtml();
                                    });
                        })
                        .subscribe(
                                emitter::onSuccess,
                                error -> {
                                    try {
                                        progressCallback.accept(0); // Reset progress on error
                                    } catch (Throwable callbackException) {
                                        Log.e(TAG, "Progress callback failed on error reset", callbackException);
                                    }
                                    emitter.onError(error);
                });
            } catch (Exception e) {
                emitter.onError(new RuntimeException("An unexpected error occurred during translation.", e));
            }
        });
    }

    public Single<String> translateHtmlByParagraph(String sourceLanguage, String targetLanguage, String html, String title, long articleId, Consumer<Integer> progressCallback) {
        Log.d(TAG, "translateHtmlByParagraph: from " + sourceLanguage + " to " + targetLanguage);
        Log.d(TAG, "translateHtmlByParagraph CALLED");
        return Single.create(emitter -> {
            try {
                translateText(sourceLanguage, targetLanguage, title)
                        .flatMap(translatedTitle -> {
                            Document document = Jsoup.parse(html);
                            List<String> tags = Arrays.asList("p", "section", "blockquote");
                            Elements paragraphs = document.select(String.join(",", tags));
                            Log.d(TAG, "Found " + paragraphs.size() + " paragraphs for translation");

                            Element existingTitleElement = document.select("p.translated-title").first();
                            if (existingTitleElement == null) {
                                Element titleParagraph = new Element(Tag.valueOf("p"), "");
                                titleParagraph.text(translatedTitle);
                                titleParagraph.addClass("translated-title");
                                titleParagraph.attr("data-article-id", String.valueOf(articleId));
                                document.body().prependChild(titleParagraph);
                            }

                            AtomicInteger translatedCount = new AtomicInteger(0);
                            int total = paragraphs.size();

                            return Flowable.fromIterable(paragraphs)
                                    .flatMapMaybe(paragraph -> {
                                        if (paragraph.hasText()) {
                                            return translateText(sourceLanguage, targetLanguage, paragraph.text())
                                                    .map(translatedText -> {
                                                        paragraph.text(translatedText);
                                                        int progress = (int) ((translatedCount.incrementAndGet() / (float) total) * 100);
                                                        try {
                                                            progressCallback.accept(progress);
                                                        } catch (Exception e) {
                                                            Log.e(TAG, "Progress callback failed", e);
                                                        }
                                                        return translatedText;
                                                    }).toMaybe();
                                        }
                                        return Maybe.empty();
                                    })
                                    .toList()
                                    .map(ignored -> document.outerHtml());
                        })
                        .subscribe(
                                emitter::onSuccess,
                                error -> {
                                    Log.e(TAG, "Error during paragraph translation", error);
                                    emitter.onError(error);
                                }
                        );
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in translateHtmlByParagraph", e);
                emitter.onError(e);
            }
        });
    }

    public Single<String> translateText(String sourceLanguage, String targetLanguage, String text) {
        return Single.create(emitter -> {
            if (text == null || text.isEmpty()) {
                emitter.onError(new IllegalArgumentException("Invalid content for translation"));
                return;
            }

            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build();

            Translator translator = Translation.getClient(options);
            DownloadConditions conditions = new DownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(v -> translator.translate(text)
                            .addOnSuccessListener(emitter::onSuccess)
                            .addOnFailureListener(error -> {
                                Log.e(TAG, "Translation failed", error);
                                emitter.onError(error);
                            }))
                    .addOnFailureListener(error -> {
                        Log.e(TAG, "Model download failed", error);
                        emitter.onError(error);
                    });
        });
    }

    public Single<String> identifyLanguageRx(String sentence) {
        float confidenceThreshold = (float) sharedPreferencesRepository.getConfidenceThreshold() / 100;

        LanguageIdentificationOptions options = new LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(confidenceThreshold)
                .build();

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(options);

        return Single.fromCallable(() -> languageIdentifier.identifyLanguage(sentence))
                .subscribeOn(Schedulers.io())
                .map(languageCodeTask -> {
                    try {
                        String languageCode = Tasks.await(languageCodeTask);
                        if ("und".equals(languageCode)) {
                            Log.i(TAG, "Unable to identify language.");
                            return "und";
                        } else {
                            Log.i(TAG, "Identified language: " + languageCode);
                            return languageCode;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error identifying language", e);
                        return "und";
                    }
                })
                .onErrorReturnItem("und");
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
package my.mmu.rssnewsreader.data.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class SharedPreferencesRepository {

    private static final String TAG = "SharedPreferencesRepository";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final Context context;
    private static final String KEY_TOGGLE_STATE_PREFIX = "is_translated_view_";
    private static final String KEY_SCROLL_X_PREFIX = "scroll_x_";
    private static final String KEY_SCROLL_Y_PREFIX = "scroll_y_";
    private static final String KEY_WEB_VIEW_MODE = "web_view_mode_";
    private static final String KEY_CURRENT_READING_ENTRY_ID = "current_reading_entry_id";

    @Inject
    public SharedPreferencesRepository(@ApplicationContext Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public int getJobPeriodic() {
        return Integer.parseInt(sharedPreferences.getString("jobPeriodic", "0"));
    }

    public void setInitialJobPeriodic() {
        editor.putString("jobPeriodic", "360");
        editor.apply();
    }

    public void setJobPeriodic(String jobPeriodic) {
        editor.putString("jobPeriodic", jobPeriodic);
        editor.apply();
    }

    public boolean getNight() {
        return sharedPreferences.getBoolean("night", false);
    }

    public void setNight(boolean isNight) {
        editor.putBoolean("night", isNight);
        editor.apply();
    }

    public boolean getDisplaySummary() {
        return sharedPreferences.getBoolean("displaySummary", true);
    }

    public void setDisplaySummary(boolean displaySummary) {
        editor.putBoolean("displaySummary", displaySummary);
        editor.apply();
    }

    public boolean getHighlightText() {
        return sharedPreferences.getBoolean("highlightText", true);
    }

    public void setHighlightText(boolean highlightText) {
        editor.putBoolean("highlightText", highlightText);
        editor.apply();
    }

    public void setTextZoom(int textZoom) {
        editor.putInt("textZoom", textZoom);
        editor.apply();
    }

    public int getTextZoom() {
        return sharedPreferences.getInt("textZoom", 0);
    }

    public void setSortBy(String sortBy) {
        editor.putString("sortBy", sortBy);
        editor.apply();
    }

    public String getSortBy() {
        return sharedPreferences.getString("sortBy", "oldest");
    }

    public int getConfidenceThreshold() {
        return sharedPreferences.getInt("confidenceThreshold", 50);
    }

    public void setConfidenceThreshold(int confidenceThreshold) {
        editor.putInt("confidenceThreshold", confidenceThreshold);
        editor.apply();
    }

    public boolean getBackgroundMusic() {
        return sharedPreferences.getBoolean("backgroundMusic", false);
    }

    public void setBackgroundMusic(boolean backgroundMusic) {
        editor.putBoolean("backgroundMusic", backgroundMusic);
        editor.apply();
    }

    public String getBackgroundMusicFile() {
        return sharedPreferences.getString("backgroundMusicFile", "default");
    }

    public void setBackgroundMusicFile(String file) {
        editor.putString("backgroundMusicFile", file);
        editor.apply();
    }

    public int getBackgroundMusicVolume() {
        return sharedPreferences.getInt("backgroundMusicVolume", 50);
    }

    public void setBackgroundMusicVolume(int volume) {
        editor.putInt("backgroundMusicVolume", volume);
        editor.apply();
    }

    public int getEntriesLimitPerFeed() {
        return sharedPreferences.getInt("entriesLimitPerFeed", 1000);
    }

    public void setEntriesLimitPerFeed(int limit) {
        editor.putInt("entriesLimitPerFeed", limit);
        editor.apply();
    }

    public boolean getIsPausedManually() {
        return sharedPreferences.getBoolean("isPausedManually", false);
    }

    public void setIsPausedManually(boolean isPaused) {
        editor.putBoolean("isPausedManually", isPaused);
        editor.apply();
    }

    public String getDefaultTranslationLanguage() {
        return sharedPreferences.getString("defaultTranslationLanguage", "zh");
    }

    public void setDefaultTranslationLanguage(String language) {
        sharedPreferences.edit().putString("target_language", language).apply();
    }

    public String getTranslationMethod() {
        return sharedPreferences.getString("translationMethod", "allAtOnce");
    }

    public void setTranslationMethod(String method) {
        editor.putString("translationMethod", method);
        editor.apply();
    }

    public boolean getAutoTranslate() {
        return sharedPreferences.getBoolean("autoTranslate", false);
    }

    public void setAutoTranslate(boolean autoTranslate) {
        editor.putBoolean("autoTranslate", autoTranslate);
        editor.apply();
    }

    public void setIsTranslatedView(long entryId, boolean isTranslatedView) {
        sharedPreferences.edit()
                .putBoolean(KEY_TOGGLE_STATE_PREFIX + entryId, isTranslatedView)
                .apply();
    }

    public boolean getIsTranslatedView(long entryId) {
        return sharedPreferences.getBoolean(KEY_TOGGLE_STATE_PREFIX + entryId,false);
    }

    public boolean hasTranslationToggle(long entryId) {
        return sharedPreferences.contains(KEY_TOGGLE_STATE_PREFIX + entryId);
    }

    public void setScrollX(long entryId, int value) {
        sharedPreferences.edit().putInt(KEY_SCROLL_X_PREFIX + entryId, value).apply();
    }

    public void setScrollY(long entryId, int value) {
        sharedPreferences.edit().putInt(KEY_SCROLL_Y_PREFIX + entryId, value).apply();
    }

    public int getScrollX(long entryId) {
        return sharedPreferences.getInt(KEY_SCROLL_X_PREFIX + entryId, 0);
    }

    public int getScrollY(long entryId) {
        return sharedPreferences.getInt(KEY_SCROLL_Y_PREFIX + entryId, 0);
    }

    public void setWebViewMode(long entryId, boolean isWebViewMode) {
        sharedPreferences.edit().putBoolean(KEY_WEB_VIEW_MODE + entryId, isWebViewMode).apply();
    }

    public boolean getWebViewMode(long entryId) {
        return sharedPreferences.getBoolean(KEY_WEB_VIEW_MODE + entryId, false); // default to offline mode
    }

    public void setCurrentReadingEntryId(long entryId) {
        sharedPreferences.edit().putLong(KEY_CURRENT_READING_ENTRY_ID, entryId).apply();
    }

    public long getCurrentReadingEntryId() {
        return sharedPreferences.getLong(KEY_CURRENT_READING_ENTRY_ID, -1);
    }
}

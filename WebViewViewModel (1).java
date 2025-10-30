package my.mmu.rssnewsreader.ui.webview;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import my.mmu.rssnewsreader.data.entry.Entry;
import my.mmu.rssnewsreader.data.entry.EntryRepository;
import my.mmu.rssnewsreader.model.EntryInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WebViewViewModel extends ViewModel {

    private EntryRepository entryRepository;

    private final MutableLiveData<String> originalHtmlLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> translatedHtmlLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> translatedTextReady = new MutableLiveData<>();

    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();

    private final MutableLiveData<Long> entryIdTrigger = new MutableLiveData<>();

    public LiveData<Boolean> getLoadingState() {
        return loadingState;
    }

    public void setLoadingState(boolean isLoading) {
        loadingState.postValue(isLoading);
    }

    @Inject
    public WebViewViewModel(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    public void resetEntry(long id) {
        entryRepository.updateHtml(null, id);
        entryRepository.updateOriginalHtml(null, id);
        entryRepository.updateTranslatedText(null, id);
        entryRepository.updateTranslated(null, id);
        entryRepository.updateContent(null, id);
        entryRepository.updateSentCountByLink(0, id);
        entryRepository.updatePriority(1, id);
    }

    public void clearLiveEntryCache(long id) {
        Entry entry = entryRepository.getEntryById(id);
        if (entry != null) {
            entry.setTranslated(null);
            entry.setHtml(null);
            entry.setContent(null);
        }
    }

    public void updateHtml(String html, long id) {
        entryRepository.updateHtml(html, id);
        translatedHtmlLiveData.postValue(html);
    }

    public void updateContent(String content, long id) {
        entryRepository.updateContent(content, id);
    }

    public void updateBookmark(String bool, long id) {
        entryRepository.updateBookmark(bool, id);
    }

    public EntryInfo getLastVisitedEntry() {
        return entryRepository.getLastVisitedEntry();
    }

    public String getHtmlById(long id) {
        return entryRepository.getHtmlById(id);
    }

    public String getStyle() {
        return "<style>\n" +
                "    @font-face {\n" +
                "        font-family: open_sans;\n" +
                "        src: url(\"file:///android_res/font/open_sans.ttf\")\n" +
                "    }\n" +
                "    body {\n" +
                "        font-family: open_sans;\n" +
                "        text-align: justify;\n" +
                "        font-size: 0.875em;\n" +
                "    }\n" +
                "</style>";
    }

    @SuppressLint("SimpleDateFormat")
    public String getHtml(String entryTitle, String feedTitle, Date publishDate, String feedImageUrl) {
        return "<div class=\"entry-header\">" +
                "  <div style=\"display: flex; align-items: center;\">" +
                "    <img style=\"margin-right: 10px; width: 20px; height: 20px\" src=" + feedImageUrl + ">" +
                "    <p style=\"font-size: 0.75em\">" + feedTitle + "</p>" +
                "  </div>" +
                "  <p style=\"margin:0; font-size: 1.25em; font-weight:bold\">" + entryTitle + "</p>" +
                "  <p style=\"font-size: 0.75em;\">" + new SimpleDateFormat("EEE, d MMM yyyy 'at' hh:mm aaa").format(publishDate) + "</p>" +
                "</div>";
    }

    public boolean endsWithBreak(String text) {
        return text.endsWith(".") || text.endsWith("?") || text.endsWith("!") || text.endsWith("！") || text.endsWith("？") || text.endsWith("。");
    }

    public EntryInfo getEntryInfoById(long id) {
        return entryRepository.getEntryInfoById(id);
    }

    public void updateOriginalHtml(String html, long id) {
        entryRepository.updateOriginalHtml(html, id);
        originalHtmlLiveData.postValue(html);
    }

    public LiveData<String> getOriginalHtmlLiveData() {
        return originalHtmlLiveData;
    }

    public LiveData<String> getTranslatedHtmlLiveData() {
        return translatedHtmlLiveData;
    }

    public String getOriginalHtmlById(long id) {
        return entryRepository.getOriginalHtmlById(id);
    }

    public void triggerEntryRefresh(long entryId) {
        entryIdTrigger.postValue(entryId);
    }

    public LiveData<Entry> getLiveEntry() {
        return Transformations.switchMap(entryIdTrigger, id ->
                entryRepository.getEntryEntityById(id)
        );
    }

    public LiveData<Entry> getEntryEntityById(long entryId) {
        return entryRepository.getEntryEntityById(entryId);
    }

    public Entry getEntryById(long entryId) {
        return entryRepository.getEntryById(entryId);
    }

    public String getTranslatedById(long id) {
        Entry entry = getEntryById(id);
        return (entry != null) ? entry.getTranslated() : null;
    }

    public void updateTranslated(String text, long entryId) {
        entryRepository.updateTranslated(text, entryId);
    }

    public void updateEntryTranslatedField(long entryId, String translatedContent) {
        Entry entry = entryRepository.getEntryById(entryId);
        if (entry != null) {
            entry.setTranslated(translatedContent);
        }
    }

    public LiveData<String> getTranslatedTextReady() {
        return translatedTextReady;
    }

    public void setTranslatedTextReady(long id, String text) {
        if (text != null && !text.trim().isEmpty()) {
            translatedTextReady.postValue(text);
        }
    }
}

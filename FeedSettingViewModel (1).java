package my.mmu.rssnewsreader.ui.feedsetting;

import androidx.lifecycle.ViewModel;

import my.mmu.rssnewsreader.data.feed.FeedRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FeedSettingViewModel extends ViewModel {

    private FeedRepository feedRepository;

    @Inject
    public FeedSettingViewModel(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public void updateTitleDescLanguage(String title, String desc, String language, String link) {
        feedRepository.updateTitleDescLanguage(title, desc, language, link);
    }
}

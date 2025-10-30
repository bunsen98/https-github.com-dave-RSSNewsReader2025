package my.mmu.rssnewsreader.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import my.mmu.rssnewsreader.data.entry.Entry;
import my.mmu.rssnewsreader.data.entry.EntryRepository;
import my.mmu.rssnewsreader.data.feed.Feed;
import my.mmu.rssnewsreader.data.feed.FeedRepository;
import my.mmu.rssnewsreader.data.sharedpreferences.SharedPreferencesRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class MainActivityViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FeedRepository feedRepository;
    private EntryRepository entryRepository;
    private SharedPreferencesRepository sharedPreferencesRepository;

    private MutableLiveData<List<Feed>> allFeeds = new MutableLiveData<>();

    @Inject
    public MainActivityViewModel(FeedRepository feedRepository, EntryRepository entryRepository, SharedPreferencesRepository sharedPreferencesRepository) {
        this.feedRepository = feedRepository;
        this.entryRepository = entryRepository;
        this.sharedPreferencesRepository = sharedPreferencesRepository;

        Disposable disposable = feedRepository.getAllFeeds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Feed>>() {
                    @Override
                    public void accept(List<Feed> feeds) throws Throwable {
                        allFeeds.postValue(feeds);
                    }
                });

        compositeDisposable.add(disposable);
    }

    public LiveData<List<Feed>> getAllFeeds() {
        return allFeeds;
    }

    public List<Feed> getAllStaticFeeds() {
        return feedRepository.getAllStaticFeeds();
    }

    public List<Entry> getAllStaticEntries(long id) {
        return entryRepository.getStaticEntries(id);
    }

    public long getFeedIdByLink(String link) {
        return feedRepository.getFeedIdByLink(link);
    }

    public void addFeedUsingOPML(Feed feed) {
        if (!feedRepository.checkFeedExist(feed.getLink())) {
            feedRepository.insert(feed);
        }
    }

    public void addEntry(long feedId, Entry entry) {
        Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Throwable {
                        entryRepository.insert(feedId, entry);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public boolean getNight() {
        return sharedPreferencesRepository.getNight();
    }

    public void setNight(boolean isNight) {
        sharedPreferencesRepository.setNight(isNight);
    }

}

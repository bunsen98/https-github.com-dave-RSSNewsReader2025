package my.mmu.rssnewsreader.data.playlist;

import android.util.Log;

import my.mmu.rssnewsreader.data.entry.Entry;
import my.mmu.rssnewsreader.data.entry.EntryRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class PlaylistRepository {

    private static final String TAG = "PlaylistRepository";
    private PlaylistDao playlistDao;
    private EntryRepository entryRepository;

    @Inject
    public PlaylistRepository(PlaylistDao playlistDao, EntryRepository entryRepository) {
        this.playlistDao = playlistDao;
        this.entryRepository = entryRepository;
    }

    public void insert(Playlist playlist) {
        playlistDao.insert(playlist);
    }

    public void update(Playlist playlist) {
        playlistDao.update(playlist);
    }

    public void delete(Playlist playlist) {
        playlistDao.delete(playlist);
    }

    public void deleteAllPlaylists() {
        playlistDao.deleteAllPlaylists();
    };

    public Date getLatestPlaylistCreatedDate() {
        return playlistDao.getLatestPlaylistCreatedDate();
    }

    public String getLatestPlaylist() {
        return playlistDao.getLatestPlaylist();
    }

    public boolean updatePlaylistToPrevious() {
        boolean loop = true;
        List<Long> playlist = stringToLongList(playlistDao.getLatestPlaylist());
        long lastId = entryRepository.getLastVisitedEntryId();
        int index = playlist.indexOf(lastId);

        while (loop) {
            index -= 1;
            if (index >= 0) {
                long currentId = playlist.get(index);
                if (entryRepository.checkIdExist(currentId)) {
                    Date date = new Date();
                    entryRepository.updateDate(date, currentId);
                    return true;
                }
            } else {
                loop = false;
            }
        }
        return false;
    }

    public boolean updatePlayListToNext() {
        boolean loop = true;
        List<Long> playlist = stringToLongList(playlistDao.getLatestPlaylist());
        long lastId = entryRepository.getLastVisitedEntryId();
        int index = playlist.indexOf(lastId);

        while (loop) {
            index += 1;
            if (index < playlist.size()) {
                long currentId = playlist.get(index);
                if (entryRepository.checkIdExist(currentId)) {
                    Date date = new Date();
                    entryRepository.updateDate(date, currentId);
                    return true;
                }
            } else {
                loop = false;
            }
        }
        return false;
    }

    public List<Long> stringToLongList(String genreIds) {
        List<Long> list = new ArrayList<>();

        String[] array = genreIds.split(",");

        for (String s : array) {
            if (s != null && !s.isEmpty()) {
                list.add(Long.parseLong(s));
            }
        }
        return list;
    }

//    public void updateVisitedDate(int entryId) {
//        historyDao
//    }
}

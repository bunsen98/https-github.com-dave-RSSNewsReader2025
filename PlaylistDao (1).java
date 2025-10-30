package my.mmu.rssnewsreader.data.playlist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;

@Dao
public interface PlaylistDao {

    @Insert
    void insert(Playlist history);

    @Update
    void update(Playlist history);

    @Delete
    void delete(Playlist history);

    @Query("DELETE FROM playlist_table")
    void deleteAllPlaylists();

    @Query("SELECT playlist FROM playlist_table ORDER BY createdDate DESC LIMIT 1")
    String getLatestPlaylist();

    @Query("SELECT createdDate FROM playlist_table ORDER BY createdDate DESC LIMIT 1")
    Date getLatestPlaylistCreatedDate();
}

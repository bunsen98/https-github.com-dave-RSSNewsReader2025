package my.mmu.rssnewsreader.data.feed;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "feed_table")
public class Feed {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int delayTime;
    private float ttsSpeechRate;
    private String title;
    private String link;
    private String description;
    private String imageUrl;
    private String language;
    @ColumnInfo(defaultValue = "0")
    private boolean isPreloaded;

    public Feed(String title, String link, String description, String imageUrl, String language) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.imageUrl = imageUrl;
        this.language = language;
    }

    @Ignore
    public Feed(String title, String link, String description, String imageUrl, String language, int delayTime, float ttsSpeechRate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.imageUrl = imageUrl;
        this.language = language;
        this.delayTime = delayTime;
        this.ttsSpeechRate = ttsSpeechRate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public float getTtsSpeechRate() {
        return ttsSpeechRate;
    }

    public void setTtsSpeechRate(float ttsSpeechRate) {
        this.ttsSpeechRate = ttsSpeechRate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return id == feed.id && Objects.equals(title, feed.title) && Objects.equals(link, feed.link) && Objects.equals(description, feed.description) && Objects.equals(imageUrl, feed.imageUrl) && Objects.equals(language, feed.language) && Objects.equals(ttsSpeechRate, feed.ttsSpeechRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, link, description, imageUrl, language);
    }

    public boolean isPreloaded() {
        return isPreloaded;
    }

    public void setPreloaded(boolean preloaded) {
        isPreloaded = preloaded;
    }
}

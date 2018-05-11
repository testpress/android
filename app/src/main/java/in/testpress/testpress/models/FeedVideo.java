package in.testpress.testpress.models;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedVideo {
    private String title;
    private String url;
    @Id
    private Long id;
    private String embedCode;
    private Long videoContent;
    @Generated(hash = 27827847)
    public FeedVideo(String title, String url, Long id, String embedCode,
            Long videoContent) {
        this.title = title;
        this.url = url;
        this.id = id;
        this.embedCode = embedCode;
        this.videoContent = videoContent;
    }
    @Generated(hash = 715175012)
    public FeedVideo() {
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmbedCode() {
        return this.embedCode;
    }
    public void setEmbedCode(String embedCode) {
        this.embedCode = embedCode;
    }
    public Long getVideoContent() {
        return this.videoContent;
    }
    public void setVideoContent(Long videoContent) {
        this.videoContent = videoContent;
    }
}

package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class FeedHtmlContent {
    @Id
    private Long id;
    private String title;
    private String textHtml;
    private String sourceUrl;
    @Generated(hash = 958522750)
    public FeedHtmlContent(Long id, String title, String textHtml,
            String sourceUrl) {
        this.id = id;
        this.title = title;
        this.textHtml = textHtml;
        this.sourceUrl = sourceUrl;
    }
    @Generated(hash = 1079499063)
    public FeedHtmlContent() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTextHtml() {
        return this.textHtml;
    }
    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
    }
    public String getSourceUrl() {
        return this.sourceUrl;
    }
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}

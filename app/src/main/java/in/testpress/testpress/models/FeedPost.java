package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedPost {
    @Id
    private Long id;
    private String title;
    private String summary;
    private String contentHtml;
    private String url;
    private String publishedDate;
    private Long published;
    private String modified;
    private Integer institute;
    private Boolean is_active;
    private Long modifiedDate;
    private String short_web_url;
    private String short_url;
    private String web_url;
    private Integer commentsCount;
    private String commentsUrl;
    private String slug;
    private Long categoryId;
    private boolean forum;
    @Generated(hash = 1448261500)
    public FeedPost(Long id, String title, String summary, String contentHtml,
            String url, String publishedDate, Long published, String modified,
            Integer institute, Boolean is_active, Long modifiedDate,
            String short_web_url, String short_url, String web_url,
            Integer commentsCount, String commentsUrl, String slug, Long categoryId,
            boolean forum) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.contentHtml = contentHtml;
        this.url = url;
        this.publishedDate = publishedDate;
        this.published = published;
        this.modified = modified;
        this.institute = institute;
        this.is_active = is_active;
        this.modifiedDate = modifiedDate;
        this.short_web_url = short_web_url;
        this.short_url = short_url;
        this.web_url = web_url;
        this.commentsCount = commentsCount;
        this.commentsUrl = commentsUrl;
        this.slug = slug;
        this.categoryId = categoryId;
        this.forum = forum;
    }
    @Generated(hash = 1846793069)
    public FeedPost() {
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
    public String getSummary() {
        return this.summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getContentHtml() {
        return this.contentHtml;
    }
    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getPublishedDate() {
        return this.publishedDate;
    }
    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }
    public Long getPublished() {
        return this.published;
    }
    public void setPublished(Long published) {
        this.published = published;
    }
    public String getModified() {
        return this.modified;
    }
    public void setModified(String modified) {
        this.modified = modified;
    }
    public Integer getInstitute() {
        return this.institute;
    }
    public void setInstitute(Integer institute) {
        this.institute = institute;
    }
    public Boolean getIs_active() {
        return this.is_active;
    }
    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }
    public Long getModifiedDate() {
        return this.modifiedDate;
    }
    public void setModifiedDate(Long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    public String getShort_web_url() {
        return this.short_web_url;
    }
    public void setShort_web_url(String short_web_url) {
        this.short_web_url = short_web_url;
    }
    public String getShort_url() {
        return this.short_url;
    }
    public void setShort_url(String short_url) {
        this.short_url = short_url;
    }
    public String getWeb_url() {
        return this.web_url;
    }
    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }
    public Integer getCommentsCount() {
        return this.commentsCount;
    }
    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }
    public String getCommentsUrl() {
        return this.commentsUrl;
    }
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }
    public String getSlug() {
        return this.slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    public Long getCategoryId() {
        return this.categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public boolean getForum() {
        return this.forum;
    }
    public void setForum(boolean forum) {
        this.forum = forum;
    }
}

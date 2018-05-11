package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class FeedContent {
    @Id
    private Long id;
    private String attemptsUrl;
    private Integer user;
    private Integer textContent;

    @Generated(hash = 167197645)
    public FeedContent(Long id, String attemptsUrl, Integer user,
            Integer textContent) {
        this.id = id;
        this.attemptsUrl = attemptsUrl;
        this.user = user;
        this.textContent = textContent;
    }
    @Generated(hash = 1260874373)
    public FeedContent() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAttemptsUrl() {
        return this.attemptsUrl;
    }
    public void setAttemptsUrl(String attemptsUrl) {
        this.attemptsUrl = attemptsUrl;
    }
    public Integer getUser() {
        return this.user;
    }
    public void setUser(Integer user) {
        this.user = user;
    }
    public Integer getTextContent() {
        return this.textContent;
    }
    public void setTextContent(Integer textContent) {
        this.textContent = textContent;
    }
    
}

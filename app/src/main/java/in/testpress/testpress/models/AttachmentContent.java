package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AttachmentContent {
    @Id
    private Long id;
    private String title;
    private String description;
    @Generated(hash = 497945637)
    public AttachmentContent(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
    @Generated(hash = 1361534226)
    public AttachmentContent() {
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
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}

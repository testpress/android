package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedAttachment {
    @Id
    private Long id;
    private Long user;
    private Long attachmentContent;
    @Generated(hash = 947775867)
    public FeedAttachment(Long id, Long user, Long attachmentContent) {
        this.id = id;
        this.user = user;
        this.attachmentContent = attachmentContent;
    }
    @Generated(hash = 794830663)
    public FeedAttachment() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getAttachmentContent() {
        return this.attachmentContent;
    }
    public void setAttachmentContent(Long attachmentContent) {
        this.attachmentContent = attachmentContent;
    }
    public Long getUser() {
        return this.user;
    }
    public void setUser(Long user) {
        this.user = user;
    }
}

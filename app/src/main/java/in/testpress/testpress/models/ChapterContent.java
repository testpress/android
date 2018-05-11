package in.testpress.testpress.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ChapterContent {
    @Id
    private Long id;
    private int exam;

    private int htmlContent;
    private int videoContent;
    private String name;
    private int attachmentContent;
    private String description;

    public ChapterContent() {}

    @Generated(hash = 99419909)
    public ChapterContent(Long id, int exam, int htmlContent, int videoContent,
            String name, int attachmentContent, String description) {
        this.id = id;
        this.exam = exam;
        this.htmlContent = htmlContent;
        this.videoContent = videoContent;
        this.name = name;
        this.attachmentContent = attachmentContent;
        this.description = description;
    }

    public int getExam() {
        return exam;
    }

    public void setExam(int exam) {
        this.exam = exam;
    }

    public int getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(int htmlContent) {
        this.htmlContent = htmlContent;
    }

    public int getVideoContent() {
        return videoContent;
    }

    public void setVideoContent(int videoContent) {
        this.videoContent = videoContent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(int attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}

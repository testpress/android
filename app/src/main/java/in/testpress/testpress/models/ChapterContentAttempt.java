package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class ChapterContentAttempt {
    @Id
    private Long id;
    private Long chapterContent;
    @ToOne(joinProperty = "assessmentId")
    public Assessment assessment;
    @ToOne(joinProperty = "videoId")
    public FeedVideo video;
    @ToOne(joinProperty = "contentId")
    public FeedContent content;
    @ToOne(joinProperty = "attachmentId")
    public FeedAttachment attachment;
    private Long assessmentId;
    private Long videoId;
    private Long contentId;
    private Long attachmentId;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1328787605)
    private transient ChapterContentAttemptDao myDao;
    @Generated(hash = 2062014758)
    public ChapterContentAttempt(Long id, Long chapterContent, Long assessmentId,
            Long videoId, Long contentId, Long attachmentId) {
        this.id = id;
        this.chapterContent = chapterContent;
        this.assessmentId = assessmentId;
        this.videoId = videoId;
        this.contentId = contentId;
        this.attachmentId = attachmentId;
    }
    @Generated(hash = 1438628597)
    public ChapterContentAttempt() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getAssessmentId() {
        return this.assessmentId;
    }
    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }
    public Long getVideoId() {
        return this.videoId;
    }
    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
    public Long getContentId() {
        return this.contentId;
    }
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }
    public Long getAttachmentId() {
        return this.attachmentId;
    }
    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }
    @Generated(hash = 301763920)
    private transient Long assessment__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1362611958)
    public Assessment getAssessment() {
        Long __key = this.assessmentId;
        if (assessment__resolvedKey == null
                || !assessment__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AssessmentDao targetDao = daoSession.getAssessmentDao();
            Assessment assessmentNew = targetDao.load(__key);
            synchronized (this) {
                assessment = assessmentNew;
                assessment__resolvedKey = __key;
            }
        }
        return assessment;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1872311013)
    public void setAssessment(Assessment assessment) {
        synchronized (this) {
            this.assessment = assessment;
            assessmentId = assessment == null ? null : assessment.getId();
            assessment__resolvedKey = assessmentId;
        }
    }
    @Generated(hash = 1581726105)
    private transient Long video__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1931625698)
    public FeedVideo getVideo() {
        Long __key = this.videoId;
        if (video__resolvedKey == null || !video__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedVideoDao targetDao = daoSession.getFeedVideoDao();
            FeedVideo videoNew = targetDao.load(__key);
            synchronized (this) {
                video = videoNew;
                video__resolvedKey = __key;
            }
        }
        return video;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 901527943)
    public void setVideo(FeedVideo video) {
        synchronized (this) {
            this.video = video;
            videoId = video == null ? null : video.getId();
            video__resolvedKey = videoId;
        }
    }
    @Generated(hash = 791892265)
    private transient Long content__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 337986920)
    public FeedContent getContent() {
        Long __key = this.contentId;
        if (content__resolvedKey == null || !content__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedContentDao targetDao = daoSession.getFeedContentDao();
            FeedContent contentNew = targetDao.load(__key);
            synchronized (this) {
                content = contentNew;
                content__resolvedKey = __key;
            }
        }
        return content;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 212633575)
    public void setContent(FeedContent content) {
        synchronized (this) {
            this.content = content;
            contentId = content == null ? null : content.getId();
            content__resolvedKey = contentId;
        }
    }
    @Generated(hash = 1204731039)
    private transient Long attachment__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 157029231)
    public FeedAttachment getAttachment() {
        Long __key = this.attachmentId;
        if (attachment__resolvedKey == null
                || !attachment__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedAttachmentDao targetDao = daoSession.getFeedAttachmentDao();
            FeedAttachment attachmentNew = targetDao.load(__key);
            synchronized (this) {
                attachment = attachmentNew;
                attachment__resolvedKey = __key;
            }
        }
        return attachment;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 235711271)
    public void setAttachment(FeedAttachment attachment) {
        synchronized (this) {
            this.attachment = attachment;
            attachmentId = attachment == null ? null : attachment.getId();
            attachment__resolvedKey = attachmentId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    public Long getChapterContent() {
        return this.chapterContent;
    }
    public void setChapterContent(Long chapterContent) {
        this.chapterContent = chapterContent;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1263064450)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChapterContentAttemptDao() : null;
    }
}

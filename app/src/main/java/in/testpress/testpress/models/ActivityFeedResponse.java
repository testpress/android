package in.testpress.testpress.models;

import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class ActivityFeedResponse {

    @Id
    private Long id;
    @ToMany(referencedJoinProperty = "id")
    private List<Activity> activities;
    @ToMany(referencedJoinProperty = "id")
    private List<User> users;
    @ToMany(referencedJoinProperty = "id")
    private List<AttachmentContent> attachmentContents;
    @ToMany(referencedJoinProperty = "id")
    private List<Category> postcategories;
    @ToMany(referencedJoinProperty = "id")
    private List<FeedVideo> videoContents;
    @ToMany(referencedJoinProperty = "id")
    private List<FeedPost> posts;
    @ToMany(referencedJoinProperty = "id")
    private List<FeedChapter> chapters;
    @ToMany(referencedJoinProperty = "id")
    private List<ContentType> contentTypes;
    @ToMany(referencedJoinProperty = "id")
    private List<FeedHtmlContent> htmlContents;
    @ToMany(referencedJoinProperty = "id")
    private List<FeedExam> exams;
    @ToMany(referencedJoinProperty = "id")
    private List<ChapterContentAttempt> chaptercontentattempts;
    @ToMany(referencedJoinProperty = "id")
    private List<ChapterContent> chaptercontents;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 981276281)
    private transient ActivityFeedResponseDao myDao;
    @Generated(hash = 2001132108)
    public ActivityFeedResponse(Long id) {
        this.id = id;
    }
    @Generated(hash = 1934273967)
    public ActivityFeedResponse() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2041734575)
    public List<Activity> getActivities() {
        if (activities == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ActivityDao targetDao = daoSession.getActivityDao();
            List<Activity> activitiesNew = targetDao
                    ._queryActivityFeedResponse_Activities(id);
            synchronized (this) {
                if (activities == null) {
                    activities = activitiesNew;
                }
            }
        }
        return activities;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1595419912)
    public synchronized void resetActivities() {
        activities = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 900328295)
    public List<User> getUsers() {
        if (users == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            List<User> usersNew = targetDao._queryActivityFeedResponse_Users(id);
            synchronized (this) {
                if (users == null) {
                    users = usersNew;
                }
            }
        }
        return users;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1027274768)
    public synchronized void resetUsers() {
        users = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1033600454)
    public synchronized void resetAttachmentContents() {
        attachmentContents = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 271590383)
    public List<Category> getPostcategories() {
        if (postcategories == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CategoryDao targetDao = daoSession.getCategoryDao();
            List<Category> postcategoriesNew = targetDao
                    ._queryActivityFeedResponse_Postcategories(id);
            synchronized (this) {
                if (postcategories == null) {
                    postcategories = postcategoriesNew;
                }
            }
        }
        return postcategories;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 136439318)
    public synchronized void resetPostcategories() {
        postcategories = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 922648923)
    public List<FeedVideo> getVideoContents() {
        if (videoContents == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedVideoDao targetDao = daoSession.getFeedVideoDao();
            List<FeedVideo> videoContentsNew = targetDao
                    ._queryActivityFeedResponse_VideoContents(id);
            synchronized (this) {
                if (videoContents == null) {
                    videoContents = videoContentsNew;
                }
            }
        }
        return videoContents;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1539881073)
    public synchronized void resetVideoContents() {
        videoContents = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 802173039)
    public synchronized void resetPosts() {
        posts = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 461535444)
    public List<FeedChapter> getChapters() {
        if (chapters == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedChapterDao targetDao = daoSession.getFeedChapterDao();
            List<FeedChapter> chaptersNew = targetDao
                    ._queryActivityFeedResponse_Chapters(id);
            synchronized (this) {
                if (chapters == null) {
                    chapters = chaptersNew;
                }
            }
        }
        return chapters;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 936914273)
    public synchronized void resetChapters() {
        chapters = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1371299351)
    public List<ContentType> getContentTypes() {
        if (contentTypes == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ContentTypeDao targetDao = daoSession.getContentTypeDao();
            List<ContentType> contentTypesNew = targetDao
                    ._queryActivityFeedResponse_ContentTypes(id);
            synchronized (this) {
                if (contentTypes == null) {
                    contentTypes = contentTypesNew;
                }
            }
        }
        return contentTypes;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 698809311)
    public synchronized void resetContentTypes() {
        contentTypes = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 578630587)
    public List<FeedHtmlContent> getHtmlContents() {
        if (htmlContents == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedHtmlContentDao targetDao = daoSession.getFeedHtmlContentDao();
            List<FeedHtmlContent> htmlContentsNew = targetDao
                    ._queryActivityFeedResponse_HtmlContents(id);
            synchronized (this) {
                if (htmlContents == null) {
                    htmlContents = htmlContentsNew;
                }
            }
        }
        return htmlContents;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 180986877)
    public synchronized void resetHtmlContents() {
        htmlContents = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1049232032)
    public List<FeedExam> getExams() {
        if (exams == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedExamDao targetDao = daoSession.getFeedExamDao();
            List<FeedExam> examsNew = targetDao
                    ._queryActivityFeedResponse_Exams(id);
            synchronized (this) {
                if (exams == null) {
                    exams = examsNew;
                }
            }
        }
        return exams;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 841969952)
    public synchronized void resetExams() {
        exams = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 138136213)
    public List<ChapterContentAttempt> getChaptercontentattempts() {
        if (chaptercontentattempts == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChapterContentAttemptDao targetDao = daoSession
                    .getChapterContentAttemptDao();
            List<ChapterContentAttempt> chaptercontentattemptsNew = targetDao
                    ._queryActivityFeedResponse_Chaptercontentattempts(id);
            synchronized (this) {
                if (chaptercontentattempts == null) {
                    chaptercontentattempts = chaptercontentattemptsNew;
                }
            }
        }
        return chaptercontentattempts;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 359660061)
    public synchronized void resetChaptercontentattempts() {
        chaptercontentattempts = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1084701093)
    public List<ChapterContent> getChaptercontents() {
        if (chaptercontents == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChapterContentDao targetDao = daoSession.getChapterContentDao();
            List<ChapterContent> chaptercontentsNew = targetDao
                    ._queryActivityFeedResponse_Chaptercontents(id);
            synchronized (this) {
                if (chaptercontents == null) {
                    chaptercontents = chaptercontentsNew;
                }
            }
        }
        return chaptercontents;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 970627292)
    public synchronized void resetChaptercontents() {
        chaptercontents = null;
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
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 455309553)
    public List<FeedPost> getPosts() {
        if (posts == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedPostDao targetDao = daoSession.getFeedPostDao();
            List<FeedPost> postsNew = targetDao._queryActivityFeedResponse_Posts(id);
            synchronized (this) {
                if (posts == null) {
                    posts = postsNew;
                }
            }
        }
        return posts;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1860108704)
    public List<AttachmentContent> getAttachmentContents() {
        if (attachmentContents == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AttachmentContentDao targetDao = daoSession.getAttachmentContentDao();
            List<AttachmentContent> attachmentContentsNew = targetDao
                    ._queryActivityFeedResponse_AttachmentContents(id);
            synchronized (this) {
                if (attachmentContents == null) {
                    attachmentContents = attachmentContentsNew;
                }
            }
        }
        return attachmentContents;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2044649736)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getActivityFeedResponseDao() : null;
    }

}

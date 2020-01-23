package in.testpress.testpress.models.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Post;


public class DashboardResponse {
    private List<DashboardSection> dashboardSections = new ArrayList<>();
    private List<Content> chapterContents = new ArrayList<>();
    private List<CourseAttempt> chapterContentAttempts = new ArrayList<>();
    private List<Post> posts = new ArrayList<>();
    private List<Banner> bannerAds = new ArrayList<>();
    private List<LeaderboardItem> leaderboardItems = new ArrayList<>();
    private List<Chapter> chapters = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private List<UserStats> userStatuses = new ArrayList<>();
    private List<Exam> exams = new ArrayList<>();
    private List<Attempt> assessments = new ArrayList<>();
    private List<VideoAttempt> user_videos = new ArrayList<>();

    private HashMap<Long, Chapter> chapterHashMap = new HashMap<>();
    private HashMap<Long, Content> contentHashMap = new HashMap<>();
    private HashMap<Long, CourseAttempt> contentAttemptHashMap = new HashMap<>();
    private HashMap<Long, Exam> examHashMap = new HashMap<>();
    private HashMap<Long, Video> videoHashMap = new HashMap<>();

    public List<DashboardSection> getDashboardSections() {
        return dashboardSections;
    }

    public List<Content> getContents() {
        return chapterContents;
    }

    public List<CourseAttempt> getContentAttempts() {
        return chapterContentAttempts;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public List<Banner> getBanners() {
        return bannerAds;
    }

    public List<LeaderboardItem> getLeaderboardItems() {
        return leaderboardItems;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<UserStats> getUserStatuses() {
        return userStatuses;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public List<Attempt> getAssessments() {
        return assessments;
    }

    public List<VideoAttempt> getUser_videos() {
        return user_videos;
    }

    public HashMap<Long, Chapter> getChapterHashMap() {
        if (chapterHashMap.isEmpty()) {
            for(Chapter chapter : chapters) {
                chapterHashMap.put(chapter.getId(), chapter);
            }
        }
        return chapterHashMap;
    }

    public HashMap<Long, Content> getContentHashMap() {
        if (contentHashMap.isEmpty()) {
            for(Content content : chapterContents) {
                contentHashMap.put(content.getId(), content);
            }
        }
        return contentHashMap;
    }

    public HashMap<Long, CourseAttempt> getContentAttemptHashMap() {
        if (contentAttemptHashMap.isEmpty()) {
            for(CourseAttempt attempt : chapterContentAttempts) {
                contentAttemptHashMap.put(attempt.getId(), attempt);
            }
        }
        return contentAttemptHashMap;
    }

    public HashMap<Long, Exam> getExamHashMap() {
        if (examHashMap.isEmpty()) {
            for(Exam exam : exams) {
                examHashMap.put(exam.getId(), exam);
            }
        }
        return examHashMap;
    }
}
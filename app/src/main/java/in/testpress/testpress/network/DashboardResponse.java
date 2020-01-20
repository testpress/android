package in.testpress.testpress.network;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.testpress.models.Banner;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.LeaderboardItem;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.UserStats;

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

    public void setPosts(List<Post> posts) {
        this.posts = posts;
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
}

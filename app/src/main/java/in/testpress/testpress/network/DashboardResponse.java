package in.testpress.testpress.network;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.testpress.models.Banner;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.Post;

public class DashboardResponse {
    private List<DashboardSection> dashboardSections = new ArrayList<>();
    private List<Content> chapterContents = new ArrayList<>();
    private List<CourseAttempt> chapterContentAttempts = new ArrayList<>();
    private List<Post> posts = new ArrayList<>();
    private List<Banner> bannerAds = new ArrayList<>();

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
}

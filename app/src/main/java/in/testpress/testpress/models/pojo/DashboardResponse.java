package in.testpress.testpress.models.pojo;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.testpress.models.greendao.Attempt;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.Video;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Post;
import in.testpress.models.greendao.Product;


public class DashboardResponse {
    private List<DashboardSection> dashboardSections = new ArrayList<>();
    private List<DashboardSection> availableSections = new ArrayList<>();
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
    private List<Product> products = new ArrayList<>();
    private List<HtmlContent> contents = new ArrayList<>();
    private List<Video> videos = new ArrayList<>();

    private HashMap<Long, Chapter> chapterHashMap = new HashMap<>();
    private HashMap<Long, Content> contentHashMap = new HashMap<>();
    private HashMap<Long, Post> postHashMap = new HashMap<>();
    private HashMap<Long, Banner> bannerHashMap = new HashMap<>();
    private HashMap<Long, CourseAttempt> contentAttemptHashMap = new HashMap<>();
    private HashMap<Long, Exam> examHashMap = new HashMap<>();
    private HashMap<Long, Video> videoHashMap = new HashMap<>();
    private HashMap<Long, Product> productHashMap = new HashMap<>();
    private HashMap<Long, Course> courseHashMap = new HashMap<>();
    private HashMap<Long, LeaderboardItem> leaderboardItemHashMap = new HashMap<>();
    private HashMap<Long, VideoAttempt> userVideoHashMap = new HashMap<>();
    private HashMap<Long, HtmlContent> htmlContentHashMap = new HashMap<>();

    List<String> acceptedContentTypes = Arrays.asList("post", "banner_ad", "chapter_content",
            "trophy_leaderboard", "products", "chapter_content_attempt");

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


    public List<Product> getProducts() {
        return products;
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

    public HashMap<Long, Post> getPostHashMap() {
        if (postHashMap.isEmpty()) {
            for(Post post : posts) {
                postHashMap.put(post.getId(), post);
            }
        }
        return postHashMap;
    }

    public HashMap<Long, Banner> getBannerHashMap() {
        if (bannerHashMap.isEmpty()) {
            for(Banner banner : bannerAds) {
                bannerHashMap.put(banner.getId(), banner);
            }
        }
        return bannerHashMap;
    }

    public HashMap<Long, Product> getProductHashMap() {
        if (productHashMap.isEmpty()) {
            for(Product product : products) {
                productHashMap.put(product.getId().longValue(), product);
            }
        }
        return productHashMap;
    }


    public HashMap<Long, Course> getCourseHashMap() {
        if (courseHashMap.isEmpty()) {
            for(Course course : courses) {
                courseHashMap.put(course.getId(), course);
            }
        }
        return courseHashMap;
    }


    public HashMap<Long, LeaderboardItem> getLeaderboardItemHashMap() {
        if (leaderboardItemHashMap.isEmpty()) {
            for(LeaderboardItem item : leaderboardItems) {
                leaderboardItemHashMap.put(item.getId(), item);
            }
        }
        return leaderboardItemHashMap;
    }

    public HashMap<Long, VideoAttempt> getUserVideoHashMap() {
        if (userVideoHashMap.isEmpty()) {
            for(VideoAttempt userVideo : user_videos) {
                userVideoHashMap.put(userVideo.getId(), userVideo);
            }
        }
        return userVideoHashMap;
    }

    public HashMap<Long, Video> getVideoHashMap() {
        if (videoHashMap.isEmpty()) {
            for(Video video : videos) {
                videoHashMap.put(video.getId(), video);
            }
        }
        return videoHashMap;
    }

    public HashMap<Long, HtmlContent> getHtmlContentHashMap() {
        if (htmlContentHashMap.isEmpty()) {
            for(HtmlContent htmlContent : contents) {
                htmlContentHashMap.put(htmlContent.getId(), htmlContent);
            }
        }
        return htmlContentHashMap;
    }

    public List<DashboardSection> getAvailableSections() {
        if (availableSections.isEmpty()) {
            for (DashboardSection section : dashboardSections) {
                if (!section.getItems().isEmpty() && acceptedContentTypes.contains(section.getContentType())) {
                    availableSections.add(section);
                }
            }

            Collections.sort(availableSections, new Comparator<DashboardSection>() {
                @Override
                public int compare(DashboardSection section1, DashboardSection section2) {
                    return section1.getOrder() - section2.getOrder();
                }
            });
        }
        return availableSections;
    }
}
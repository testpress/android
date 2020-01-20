package in.testpress.testpress.ui.loaders;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.ChapterDao;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.models.greendao.CourseDao;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.DashboardPager;
import in.testpress.testpress.models.BannerDao;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.LeaderboardItem;
import in.testpress.testpress.models.LeaderboardItemDao;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.testpress.models.UserStatsDao;
import in.testpress.util.ThrowableLoader;


public class DashboardLoader extends ThrowableLoader<List<DashboardSection>> {
    private DashboardPager pager;
    private Context context;
    private DaoSession daoSession;

    public DashboardLoader(Context context, DashboardPager pager) {
        super(context, null);
        this.pager = pager;
        this.context = context;
        daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
    }

    @Override
    public List<DashboardSection> loadData() throws TestpressException {
        pager.getItems(1, -1);
        storeData();
        return pager.getResponse().getDashboardSections();
    }

    private void storeData() {
        storeContents();
        storeContentAttempts();
        storeBanners();
        storeLeaderboardItemsAndUsers();
        storePosts();
        storeCourses();
        storeChapters();
        storeCategories();
        storeUserStatuses();
    }

    private void storeContents() {
        ContentDao contentDao = TestpressSDKDatabase.getContentDao(context);
        contentDao.insertOrReplaceInTx(pager.getResponse().getContents());
    }

    private void storeContentAttempts() {
        CourseAttemptDao courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context);
        courseAttemptDao.insertOrReplaceInTx(pager.getResponse().getContentAttempts());
    }

    private void storePosts() {
        PostDao postDao = daoSession.getPostDao();
        postDao.insertOrReplaceInTx(pager.getResponse().getPosts());
    }

    private void storeLeaderboardItemsAndUsers() {
        LeaderboardItemDao leaderboardItemDao = daoSession.getLeaderboardItemDao();

        List<User> users = new ArrayList<>();
        List<LeaderboardItem> leaderboardItems = new ArrayList<>();
        UserDao userDao = daoSession.getUserDao();

        for (LeaderboardItem leaderboardItem:pager.getResponse().getLeaderboardItems()) {
            users.add(leaderboardItem.getRawUser());
            leaderboardItem.setUserId(leaderboardItem.getRawUser().getId());
            leaderboardItems.add(leaderboardItem);
        }
        userDao.insertOrReplaceInTx(users);
        leaderboardItemDao.insertOrReplaceInTx(leaderboardItems);

    }

    private void storeBanners() {
        BannerDao bannerDao = daoSession.getBannerDao();
        bannerDao.insertOrReplaceInTx(pager.getResponse().getBanners());
    }

    private void storeCourses() {
        CourseDao courseDao = TestpressSDKDatabase.getCourseDao(context);
        courseDao.insertOrReplaceInTx(pager.getResponse().getCourses());
    }

    private void storeChapters() {
        ChapterDao chapterDao = TestpressSDKDatabase.getChapterDao(context);
        chapterDao.insertOrReplaceInTx(pager.getResponse().getChapters());
    }

    private void storeCategories() {
        CategoryDao categoryDao = daoSession.getCategoryDao();
        categoryDao.insertOrReplaceInTx(pager.getResponse().getCategories());
    }

    private void storeUserStatuses() {
        UserStatsDao userStatsDao = daoSession.getUserStatsDao();
        userStatsDao.insertOrReplaceInTx(pager.getResponse().getUserStatuses());
    }

}
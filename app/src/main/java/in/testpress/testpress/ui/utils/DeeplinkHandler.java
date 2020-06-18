package in.testpress.testpress.ui.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.exam.TestpressExam;
import in.testpress.store.TestpressStore;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.ResetPasswordActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.ui.AccountActivateActivity;
import in.testpress.testpress.ui.DocumentsListActivity;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.PostsListActivity;
import in.testpress.testpress.ui.ProfileDetailsActivity;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.util.Assert;

import static in.testpress.exam.api.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.core.Constants.Http.CHAPTERS_PATH;
import static in.testpress.testpress.ui.PostActivity.DETAIL_URL;

public class DeeplinkHandler {
    private Activity activity;
    TestpressServiceProvider serviceProvider;

    public DeeplinkHandler(Activity activity, TestpressServiceProvider serviceProvider) {
        this.activity = activity;
        this.serviceProvider = serviceProvider;
    }

    public void handleDeepLinkUrl(Uri uri, boolean fromSplashScreen) {
        if (uri != null && uri.getPathSegments().size() > 0) {
            List<String> pathSegments = uri.getPathSegments();
            switch (pathSegments.get(0)) {
                case "p":
                    Intent intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("shortWebUrl", uri.toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(Constants.IS_DEEP_LINK, true);
                    activity.startActivity(intent);
                    activity.finish();
                    break;
                case "posts":
                    Intent postsIntent;
                    if (isPostsDetail(uri)) {
                        postsIntent = new Intent(activity, PostActivity.class);
                        postsIntent.putExtra(DETAIL_URL, uri.toString());
                    } else {
                        postsIntent = new Intent(activity, PostsListActivity.class);
                    }

                    postsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    postsIntent.putExtra(Constants.IS_DEEP_LINK, true);
                    activity.startActivity(postsIntent);
                    activity.finish();
                    break;
                case "exams":
                    authenticateUserAndOpen(uri);
                    break;
                case "user":
                case "profile":
                    if (pathSegments.size() == 1) {
                        gotoActivity(ProfileDetailsActivity.class, true);
                    } else {
                        CommonUtils.openUrlInBrowser(activity, uri);
                        activity.finish();
                    }
                    break;
                case "password":
                    gotoActivity(ResetPasswordActivity.class, false);
                    break;
                case "analytics":
                    authenticateUserAndOpen(uri);
                    break;
                case "documents":
                    gotoActivity(DocumentsListActivity.class, true);
                    break;
                case "login":
                    gotoActivity(MainActivity.class, true);
                    break;
                case "activate":
                    gotoAccountActivate(uri.getPath());
                    break;
                case "chapters":
                    authenticateUserAndOpen(uri);
                    break;
                case "courses":
                case "learn":
                case "leaderboard":
                case "dashboard":
                    gotoHome();
                    break;
                case "store":
                case "market":
                case "products":
                    authenticateUserAndOpen(uri);
                    break;
                default:
                    openBrowserOrGotoHome(uri, fromSplashScreen);
                    break;
            }
        } else {
            openBrowserOrGotoHome(uri, fromSplashScreen);
        }
    }

    private void openBrowserOrGotoHome(Uri uri, boolean fromSplashScreen) {
        if (fromSplashScreen) {
            gotoHome();
        } else {
            CommonUtils.openUrlInBrowser(activity, uri);
        }
    }

    private boolean isPostsDetail(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        return pathSegments.get(0).equals("posts") && pathSegments.size() > 1;
    }

    private void authenticateUserAndOpen(final Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        CommonUtils.getAuth(activity, serviceProvider,
                new CommonUtils.CheckAuthCallBack() {
                    @Override
                    public void onSuccess(TestpressService testpressService) {
                        TestpressSession testpressSession = TestpressSdk.getTestpressSession(activity);
                        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);

                        switch (pathSegments.get(0)) {
                            case "exams":
                                if (pathSegments.size() == 2) {
                                    if (!pathSegments.get(1).equals("available") ||
                                            !pathSegments.get(1).equals("upcoming") ||
                                            !pathSegments.get(1).equals("history")) {

                                        TestpressExam.showExamAttemptedState(
                                                activity,
                                                pathSegments.get(1),
                                                testpressSession
                                        );
                                        return;
                                    }
                                }
                                TestpressExam.show(activity, testpressSession);
                                activity.finish();
                                break;

                            case "analytics":
                                TestpressExam.showAnalytics(activity, SUBJECT_ANALYTICS_PATH,
                                        testpressSession);
                                break;
                            case "chapters":
                                deepLinkToChapter(uri, testpressSession);
                                break;
                            case "products":
                                deepLinkToProduct(uri, testpressSession);
                                break;
                        }
                    }
                });
    }

    private void deepLinkToChapter(Uri uri, TestpressSession testpressSession) {
        final List<String> pathSegments = uri.getPathSegments();
        switch (pathSegments.size()) {
            case 1:
                gotoHome();
                break;
            case 2:
                String chapterAPI = BASE_URL + CHAPTERS_PATH + uri.getLastPathSegment() + "/";
                TestpressCourse.showChapterContents(activity, chapterAPI, testpressSession);
                break;
            case 3:
                TestpressCourse.showContentDetail(activity, uri.getLastPathSegment(), testpressSession);
                break;
        }
    }

    private void deepLinkToProduct(Uri uri, TestpressSession testpressSession) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            String productSlug = uri.getLastPathSegment();
            assert productSlug != null;
            TestpressStore.showProduct(activity, productSlug, testpressSession);
        } else {
            TestpressStore.show(activity, testpressSession);
        }
    }

    private void gotoAccountActivate(String activateUrlFrag) {
        Intent intent = new Intent(activity, AccountActivateActivity.class);
        intent.putExtra(AccountActivateActivity.ACTIVATE_URL_FRAG, activateUrlFrag);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private void gotoActivity(Class activityClass, boolean requireAuthentication) {
        if (requireAuthentication && !CommonUtils.isUserAuthenticated(activity)) {
            activityClass = LoginActivity.class;
        }
        Intent intent = new Intent(activity, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.IS_DEEP_LINK, true);
        activity.startActivity(intent);
        activity.finish();
    }

    private void gotoHome() {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}

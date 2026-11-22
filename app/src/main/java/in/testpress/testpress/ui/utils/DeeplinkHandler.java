package in.testpress.testpress.ui.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.course.ui.AvailableCoursesListActivity;
import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.exam.TestpressExam;
import in.testpress.models.greendao.Product;
import in.testpress.models.greendao.ProductDao;
import in.testpress.store.TestpressStore;
import in.testpress.store.data.model.NetworkProduct;
import in.testpress.store.network.StoreApiClient;
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
import in.testpress.testpress.ui.StudentMessagingActivity;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.ui.WebViewWithSSOActivity;
import in.testpress.util.Assert;

import static in.testpress.exam.api.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.WHITE_LABELED_HOST_URL;
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
        if (uri == null) {
            gotoHome();
            return;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        String baseHost = Uri.parse(BASE_URL).getHost();
        String whiteLabeledHost = Uri.parse(WHITE_LABELED_HOST_URL).getHost();

        boolean isAppHost = host != null && ((baseHost != null && host.equalsIgnoreCase(baseHost))
                || (whiteLabeledHost != null && host.equalsIgnoreCase(whiteLabeledHost)));

        if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                && host != null
                && !isAppHost) {
            try {
                CommonUtils.openUrlInBrowser(activity, uri);
                if (fromSplashScreen) {
                    activity.finish();
                }
            } catch (ActivityNotFoundException e) {
                gotoHome();
            }
            return;
        }

        if (uri.getPathSegments().size() > 0) {
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
                    openBrowserForResetPassword(uri);
                    break;
                case "analytics":
                    authenticateUserAndOpen(uri);
                    break;
                case "documents":
                    gotoActivity(DocumentsListActivity.class, true);
                    break;
                case "messages":
                    gotoActivity(StudentMessagingActivity.class, true);
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
                    authenticateUserAndOpen(uri);
                    break;
                case "learn":
                case "leaderboard":
                case "dashboard":
                    gotoHome();
                    break;
                case "store":
                case "market":
                case "products":
                case "discussions":
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

    private void openBrowserForResetPassword(Uri uri) {
        CommonUtils.openUrlInBrowser(activity, uri);
        activity.finish();
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
                                    if (pathSegments.get(1).equals("available") ||
                                            pathSegments.get(1).equals("upcoming") ||
                                            pathSegments.get(1).equals("history")) {
                                        TestpressExam.show(activity, testpressSession);
                                        activity.finish();
                                    }
                                    else {
                                        gotoHome();
                                    }
                                }
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
                            case "store":
                            case "market":
                                openStore();
                                break;
                            case "discussions":
                                deepLinkToDiscussions(uri);
                                break;
                            case "courses":
                                deepLinkToCourse(uri, testpressSession);
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

    private void deepLinkToCourse(Uri uri, TestpressSession testpressSession) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 1) {
            TestpressCourse.show(activity, testpressSession);
        } else if (pathSegments.size() == 4 && pathSegments.get(2).equals("contents")) {
            TestpressCourse.showContentDetail(activity, uri.getLastPathSegment(), testpressSession);
        } else {
            gotoHome();
        }
    }

    private void deepLinkToProduct(Uri uri, final TestpressSession testpressSession) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            String productIdentifier = pathSegments.get(1);
            ProductDao productDao = TestpressSDKDatabase.getProductDao(activity);
            Product product = findProduct(productDao, productIdentifier);
            if (product != null) {
                openProductScreen(product.getCourseIds(), product.getSlug(), testpressSession);
                return;
            }
            if (TextUtils.isDigitsOnly(productIdentifier)) {
                try {
                    long id = Long.parseLong(productIdentifier);
                    if (id <= Integer.MAX_VALUE) {
                        int productId = (int) id;
                        TestpressSdk.setTestpressSession(activity.getApplicationContext(), testpressSession);
                        new StoreApiClient(activity).getProductDetailV3(productId)
                                .enqueue(new TestpressCallback<NetworkProduct>() {
                                    @Override
                                    public void onSuccess(NetworkProduct networkProduct) {
                                        if (activity.isFinishing() || activity.isDestroyed()) {
                                            return;
                                        }
                                        if (networkProduct != null && networkProduct.getSlug() != null) {
                                            ProductDao dao = TestpressSDKDatabase.getProductDao(activity);
                                            Product cachedProduct = findProduct(dao, networkProduct.getSlug());
                                            List<Integer> courseIds = cachedProduct != null ? cachedProduct.getCourseIds() : null;
                                            openProductScreen(courseIds, networkProduct.getSlug(), testpressSession);
                                        } else {
                                            openStore();
                                        }
                                    }

                                    @Override
                                    public void onException(TestpressException exception) {
                                        openStore();
                                    }
                                });
                        return;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            openProductScreen(null, productIdentifier, testpressSession);
        } else {
            openStore();
        }
    }

    private Product findProduct(ProductDao productDao, String identifier) {
        if (TextUtils.isDigitsOnly(identifier)) {
            try {
                long id = Long.parseLong(identifier);
                List<Product> products = productDao.queryBuilder()
                        .where(ProductDao.Properties.Id.eq(id))
                        .list();
                if (!products.isEmpty()) {
                    return products.get(0);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        List<Product> products = productDao.queryBuilder()
                .where(ProductDao.Properties.Slug.eq(identifier))
                .list();
        return products.isEmpty() ? null : products.get(0);
    }

    private void openProductScreen(List<Integer> courseIds, String productSlug, TestpressSession testpressSession) {
        if (courseIds != null && !courseIds.isEmpty()) {
            activity.startActivityForResult(
                    CoursePreviewActivity.createIntent(new ArrayList<>(courseIds), activity, productSlug),
                    TestpressStore.STORE_REQUEST_CODE
            );
        } else {
            TestpressStore.showProduct(activity, productSlug, testpressSession);
        }
    }

    private void openStore() {
        Intent intent = new Intent(activity, AvailableCoursesListActivity.class);
        activity.startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE);
    }

    private void deepLinkToDiscussions(Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 3) {
            String discussionSlug = uri.getLastPathSegment();
            assert discussionSlug != null;
            openDiscussionActivity(discussionSlug);
        } else {
            gotoHome();
        }
    }

    private void openDiscussionActivity(String discussionSlug) {
        activity.startActivityForResult(WebViewWithSSOActivity.Companion.createIntent(
                activity,
                "Discussion",
                WHITE_LABELED_HOST_URL + "/discussions/new/" + discussionSlug,
                true,
                false,
                true,
                WebViewWithSSOActivity.class
        ), 1000);
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

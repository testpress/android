package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import junit.framework.Assert;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.exam.TestpressExam;
import in.testpress.store.TestpressStore;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.ResetPasswordActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.UpdateAppDialogManager;
import in.testpress.util.ViewUtils;

import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.exam.network.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.store.TestpressStore.CONTINUE_PURCHASE;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.core.Constants.Http.CHAPTERS_PATH;

public class SplashScreenActivity extends Activity {

    @Inject
    protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.splash_image) ImageView splashImage;

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Injector.inject(this);
        ButterKnife.inject(this);
        UpdateAppDialogManager.monitor(this);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Uri uri = getIntent().getData();
                if (uri != null && uri.getPathSegments().size() > 0) {
                    List<String> pathSegments = uri.getPathSegments();
                    switch (pathSegments.get(0)) {
                        case "p":
                            Intent intent = new Intent(SplashScreenActivity.this, PostActivity.class);
                            intent.putExtra("shortWebUrl", uri.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(Constants.IS_DEEP_LINK, true);
                            startActivity(intent);
                            finish();
                            break;
                        case "posts":
                            Intent postsIntent =
                                    new Intent(SplashScreenActivity.this, PostsListActivity.class);
                            postsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            postsIntent.putExtra(Constants.IS_DEEP_LINK, true);
                            startActivity(postsIntent);
                            finish();
                            break;
                        case "exams":
                            authenticateUser(uri);
                            break;
                        case "user":
                        case "profile":
                            if (pathSegments.size() == 1) {
                                gotoActivity(ProfileDetailsActivity.class, true);
                            } else {
                                CommonUtils.openUrlInBrowser(SplashScreenActivity.this, uri);
                                finish();
                            }
                            break;
                        case "password":
                            gotoActivity(ResetPasswordActivity.class, false);
                            break;
                        case "analytics":
                            authenticateUser(uri);
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
                            authenticateUser(uri);
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
                            authenticateUser(uri);
                            break;
                        default:
                            CommonUtils.openUrlInBrowser(SplashScreenActivity.this, uri);
                            finish();
                            break;
                    }
                } else {
                    // This method will be executed once the timer is over
                    // Start app main activity
                    gotoHome();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void gotoHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void authenticateUser(final Uri uri) {
        final Activity activity = SplashScreenActivity.this;
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

                                        // If exam slug is present, directly goto the start exam screen
                                        TestpressExam.showExamAttemptedState(
                                                activity,
                                                pathSegments.get(1),
                                                testpressSession
                                        );
                                        return;
                                    }
                                }
                                // Show exams list
                                TestpressExam.show(activity, testpressSession);
                                finish();
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
                // /chapters/
                gotoHome();
                break;
            case 2:
                // Contents list url - /chapters/chapter-slug/
                String chapterAPI = BASE_URL + CHAPTERS_PATH + uri.getLastPathSegment() + "/";
                TestpressCourse.showChapterContents(this, chapterAPI, testpressSession);
                break;
            case 3:
                // Content detail url - /chapters/chapter-slug/{content-id}/
                TestpressCourse.showContentDetail(this, uri.getLastPathSegment(), testpressSession);
                break;
        }
    }

    private void deepLinkToProduct(Uri uri, TestpressSession testpressSession) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            // Product detail url - /products/product-slug/
            String productSlug = uri.getLastPathSegment();
            assert productSlug != null;
            TestpressStore.showProduct(this, productSlug, testpressSession);
        } else {
            TestpressStore.show(this, testpressSession);
        }
    }

    private void gotoAccountActivate(String activateUrlFrag) {
        Intent intent = new Intent(SplashScreenActivity.this, AccountActivateActivity.class);
        intent.putExtra(AccountActivateActivity.ACTIVATE_URL_FRAG, activateUrlFrag);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void gotoActivity(Class activityClass, boolean requireAuthentication) {
        if (requireAuthentication && !CommonUtils.isUserAuthenticated(SplashScreenActivity.this)) {
            activityClass = LoginActivity.class;
        }
        Intent intent = new Intent(SplashScreenActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.IS_DEEP_LINK, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        splashImage.setImageResource(R.drawable.splash_screen);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == STORE_REQUEST_CODE) {
                if (data != null && data.getBooleanExtra(CONTINUE_PURCHASE, false)) {
                    // User pressed continue purchase button.
                    TestpressSession session = TestpressSdk.getTestpressSession(this);
                    assert session != null;
                    TestpressStore.show(this, session);
                } else {
                    // User pressed goto home button.
                    gotoHome();
                }
            } else {
                // Result code OK will come if attempted an exam & back press
                gotoHome();
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (data != null && data.getBooleanExtra(ACTION_PRESSED_HOME, false)) {
                TestpressSession testpressSession = TestpressSdk.getTestpressSession(this);
                Assert.assertNotNull("TestpressSession must not be null.", testpressSession);
                switch (requestCode) {
                    case COURSE_CONTENT_DETAIL_REQUEST_CODE:
                    case COURSE_CONTENT_LIST_REQUEST_CODE:
                        int courseId = data.getIntExtra(COURSE_ID, 0);
                        String chapterUrl = data.getStringExtra(CHAPTER_URL);
                        if (chapterUrl != null) {
                            // Show contents list or child chapters of the chapter url given
                            TestpressCourse.showChapterContents(this, chapterUrl, testpressSession);
                            return;
                        } else if (courseId != 0) {
                            // Show grand parent chapters list on home press from sub chapters list
                            TestpressCourse.showChapters(this, null, courseId, testpressSession);
                            return;
                        }
                        break;
                }
                // Go to home if user pressed home button & no other data passed in result intent
                gotoHome();
            } else {
                finish();
            }
        }
    }

}
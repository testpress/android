package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;


import org.json.JSONObject;

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
import in.testpress.testpress.ui.utils.DeeplinkHandler;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Strings;
import in.testpress.testpress.util.UpdateAppDialogManager;
import in.testpress.util.Assert;
import in.testpress.util.ViewUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.validators.IntegrationValidator;

import static in.testpress.core.TestpressSdk.ACTION_PRESSED_HOME;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_DETAIL_REQUEST_CODE;
import static in.testpress.core.TestpressSdk.COURSE_CONTENT_LIST_REQUEST_CODE;
import static in.testpress.course.TestpressCourse.CHAPTER_URL;
import static in.testpress.course.TestpressCourse.COURSE_ID;
import static in.testpress.store.TestpressStore.CONTINUE_PURCHASE;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.core.Constants.Http.CHAPTERS_PATH;

public class SplashScreenActivity extends Activity {

    @Inject
    protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.splash_image) ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Injector.inject(this);
        ButterKnife.inject(this);
        UpdateAppDialogManager.monitor(this);
        final DeeplinkHandler deeplinkHandler = new DeeplinkHandler(this, serviceProvider);
        IntegrationValidator.validate(SplashScreenActivity.this);
    }


    private void gotoHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        splashImage.setImageResource(R.drawable.splash_screen);
    }

    private Uri getDeeplinkUriFromBranch(JSONObject branchData) {
        Object nonBranchLink = branchData.optString("+non_branch_link");
        Object deeplinkPath = branchData.optString("$deeplink_path");
        Object androidDeeplinkPath = branchData.optString("$android_deeplink_path");

        Uri uri = null;
        if (!Strings.isNullOrEmpty(androidDeeplinkPath)) {
            uri = Uri.parse(androidDeeplinkPath.toString());
        } else if (!Strings.isNullOrEmpty(deeplinkPath)) {
            uri = Uri.parse(deeplinkPath.toString());
        } else if (!Strings.isNullOrEmpty(nonBranchLink)) {
            uri = Uri.parse(nonBranchLink.toString());
        }
        return uri;
    }

    private Branch.BranchReferralInitListener branchReferralInitListener =
            new Branch.BranchReferralInitListener() {
                @Override
                public void onInitFinished(JSONObject referringParams, BranchError error) {
                    if (error == null) {
                        final DeeplinkHandler deeplinkHandler = new DeeplinkHandler(SplashScreenActivity.this, serviceProvider);
                        deeplinkHandler.handleDeepLinkUrl(getDeeplinkUriFromBranch(referringParams));
                    } else {
                        final DeeplinkHandler deeplinkHandler = new DeeplinkHandler(SplashScreenActivity.this, serviceProvider);
                        deeplinkHandler.handleDeepLinkUrl(null);
                    }
                }
            };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.setRetryCount(5);
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).withData(this.getIntent().getData()).init();
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
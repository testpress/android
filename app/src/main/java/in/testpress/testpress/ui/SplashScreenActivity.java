package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.util.CommonUtils;

import static in.testpress.exam.TestpressExam.ACTION_PRESSED_HOME;
import static in.testpress.exam.ui.CarouselFragment.TEST_TAKEN_REQUEST_CODE;

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
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(Constants.IS_DEEP_LINK, true);
                            startActivity(intent);
                            finish();
                            break;
                        case "exams":
                            deepLinkExams(uri);
                            break;
                        default:
                            gotoHome();
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

    @SuppressWarnings("ConstantConditions")
    private void deepLinkExams(Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        CommonUtils.getAuth(SplashScreenActivity.this, serviceProvider,
                new CommonUtils.CheckAuthCallBack() {
                    @Override
                    public void onSuccess(TestpressService testpressService) {
                        if (pathSegments.size() == 2) {
                            TestpressExam.startExam(SplashScreenActivity.this, pathSegments.get(1),
                                    TestpressSdk.getTestpressSession(SplashScreenActivity.this));
                        } else {
                            TestpressExam.show(SplashScreenActivity.this,
                                    TestpressSdk.getTestpressSession(SplashScreenActivity.this));
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        splashImage.setImageResource(R.drawable.splash_screen);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEST_TAKEN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                gotoHome();
            } else if (resultCode == RESULT_CANCELED) {
                if (data.getBooleanExtra(ACTION_PRESSED_HOME, false)) {
                    gotoHome();
                } else {
                    finish();
                }
            }
        }
    }

}
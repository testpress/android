package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;

public class SplashScreenActivity extends Activity {
    @InjectView(R.id.splash_image) ImageView splashImage;

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.inject(this);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = null;
                Uri uri = getIntent().getData();
                if (uri != null) {
                    List<String> pathSegments = uri.getPathSegments();
                    if (pathSegments.get(0).equals("p")) {
                        i = new Intent(SplashScreenActivity.this, PostActivity.class);
                        i.putExtra("shortWebUrl", uri.toString());
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra(Constants.IS_DEEP_LINK, true);
                    }
                } else {
                    // This method will be executed once the timer is over
                    // Start app main activity
                    i = new Intent(SplashScreenActivity.this, MainActivity.class);
                }
                startActivity(i);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        splashImage.setImageResource(R.drawable.splash_screen);
    }
}
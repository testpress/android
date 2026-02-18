package in.testpress.testpress.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;

public class DeviceNotAllowedActivity extends AppCompatActivity {

    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject
    protected TestpressService testpressService;
    @Inject
    protected LogoutService logoutService;

    public static boolean isShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isShowing = true;
        setContentView(R.layout.device_not_allowed);
        TestpressApplication.getAppComponent().inject(this);

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceProvider.logout(DeviceNotAllowedActivity.this, testpressService, serviceProvider, logoutService);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Disable back button to force user to logout
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void resetShowing() {
        isShowing = false;
    }
}

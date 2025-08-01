package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.CustomErrorEvent;
import in.testpress.testpress.events.UnAuthorizedUserErrorEvent;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.ui.UserDevicesActivity;

import static in.testpress.testpress.BuildConfig.BASE_URL;


/**
 * Base class for all Testpress Activities that need fragments.
 */
public class TestpressFragmentActivity extends AppCompatActivity {

    @Inject
    protected Bus eventBus;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    protected Toolbar mActionBarToolbar;
    protected Object busEventListener, unauthorisedUserErrorBusListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestpressApplication.getAppComponent().inject(this);

        // Directly subscribing in parent class won't work, only child class subscribers will work. https://github.com/square/otto/issues/26
        busEventListener = new Object() {
            @Subscribe
            public void onCustomErrorEvent(CustomErrorEvent customErrorEvent) {
                TestpressFragmentActivity.this.onReceiveCustomErrorEvent(customErrorEvent);
            }
        };

        unauthorisedUserErrorBusListener = new Object() {
            @Subscribe
            public void onUnAuthorizedUserErrorEvent(UnAuthorizedUserErrorEvent unAuthorizedUserErrorEvent) {
                try {
                    serviceProvider.logout(TestpressFragmentActivity.this, testpressService, serviceProvider, logoutService);
                } catch (Exception e) {
//                    Sentry.capture(e);
                }
            }
        };


        eventBus.register(busEventListener);
        eventBus.register(unauthorisedUserErrorBusListener);
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);

        Toolbar toolbar = getActionBarToolbar();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private boolean isFromDeeplink() {
        return getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false);
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (isFromDeeplink()) {
                goToHome();
            } else {
                onBackPressed();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        try {
            if (isFromDeeplink()) {
                goToHome();
            } else {
                super.onBackPressed();
            }
        } catch (IllegalStateException e) {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    protected void onReceiveCustomErrorEvent(final CustomErrorEvent customErrorEvent) {
        if (customErrorEvent.getErrorCode().equals(getString(R.string.PARALLEL_LOGIN_RESTRICTION_ERROR_CODE))) {
            Intent intent = new Intent(this, UserDevicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(intent);
            } catch (Exception ignore) {}
        } else if (customErrorEvent.getErrorCode().equals(getString(R.string.MAX_LOGIN_EXCEEDED_ERROR_CODE))) {
            DaoSession daoSession = TestpressApplication.getDaoSession();
            InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
            List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                    .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                    .list();

            String message = getString(R.string.max_login_limit_exceeded_error);

            if (instituteSettingsList.size() > 0) {
                InstituteSettings instituteSettings = instituteSettingsList.get(0);

                if (instituteSettings.getCooloffTime() != null) {
                    message += getString(R.string.account_unlock_info) + " %s hours";
                    message = String.format(message, instituteSettings.getCooloffTime());
                }
            }

            try {
                in.testpress.util.UIUtils.showAlert(TestpressFragmentActivity.this, "Account Locked", message);
            } catch (Exception e) {
//                Sentry.capture(e);
            }
        }
    }
}

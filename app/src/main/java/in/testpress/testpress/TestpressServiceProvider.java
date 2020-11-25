package in.testpress.testpress;

import android.accounts.AccountsException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.exoplayer2.offline.DownloadService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.services.VideoDownloadService;
import in.testpress.database.TestpressDatabase;
import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.UIUtils;
import retrofit.RestAdapter;

import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.DISPLAY_USERNAME_ON_VIDEO;
import static in.testpress.testpress.BuildConfig.GROWTH_HACKS_ENABLED;
import static in.testpress.testpress.BuildConfig.SHARE_MESSAGE;
import static in.testpress.testpress.util.PreferenceManager.setDashboardData;

public class TestpressServiceProvider {
    private RestAdapter.Builder restAdapter;
    private ApiKeyProvider keyProvider;
    String authToken;

    public TestpressServiceProvider(RestAdapter.Builder restAdapter, ApiKeyProvider keyProvider) {
        this.restAdapter = restAdapter;
        this.keyProvider = keyProvider;
    }

    public void invalidateAuthToken(Context context) {
        authToken = null;
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        LoginManager.getInstance().logOut();
        TestpressSdk.clearActiveSession(context);
    }
    /**
     * Get service for configured key provider
     * <p/>
     * This method gets an auth key and so it blocks and shouldn't be called on the main thread.
     *
     * @return testpress service
     * @throws java.io.IOException
     * @throws android.accounts.AccountsException
     */
    public TestpressService getService(final Activity activity)
            throws IOException, AccountsException {
        if (authToken == null) {
            // The call to keyProvider.getAuthKey(...) is what initiates the login screen. Call that now.
            authToken = keyProvider.getAuthKey(activity);
            DaoSession daoSession =
                    ((TestpressApplication) activity.getApplicationContext()).getDaoSession();
            InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
            List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                    .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                    .list();

            in.testpress.models.InstituteSettings settings;
            String appLink = "https://play.google.com/store/apps/details?id=" + activity.getPackageName();

            if (instituteSettingsList.isEmpty()) {
                settings = new in.testpress.models.InstituteSettings(BASE_URL);
                settings.setScreenshotDisabled(true);
            } else {
                InstituteSettings instituteSettings = instituteSettingsList.get(0);
                settings = new in.testpress.models.InstituteSettings(instituteSettings.getBaseUrl())
                        .setBookmarksEnabled(instituteSettings.getBookmarksEnabled())
                        .setCoursesFrontend(instituteSettings.getShowGameFrontend())
                        .setCoursesGamificationEnabled(instituteSettings.getCoursesEnableGamification())
                        .setCommentsVotingEnabled(instituteSettings.getCommentsVotingEnabled())
                        .setScreenshotDisabled(!instituteSettings.getAllowScreenshotInApp())
                        .setDisplayUserEmailOnVideo(DISPLAY_USERNAME_ON_VIDEO)
                        .setAccessCodeEnabled(false)
                        .setEnableParallelLoginRestriction(instituteSettings.getEnableParallelLoginRestriction())
                        .setMaxParallelLogins(instituteSettings.getMaxParallelLogins())
                        .setLockoutLimit(instituteSettings.getLockoutLimit())
                        .setCooloffTime(instituteSettings.getCooloffTime())
                        .setStoreLabel(instituteSettings.getStoreLabel())
                        .setAppToolbarLogo(instituteSettings.getAppToolbarLogo())
                        .setAppShareLink(instituteSettings.getAppShareLink())
                        .setServerTime(instituteSettings.serverTime());
                appLink = instituteSettings.getAppShareLink();
            }
            settings.setAppShareText(SHARE_MESSAGE + activity.getString(R.string.get_it_at) + appLink);
            settings.setGrowthHackEnabled(GROWTH_HACKS_ENABLED);
            TestpressSdk.setTestpressSession(activity, new TestpressSession(settings, authToken));
        }

        // TODO: See how that affects the testpress service.
        return new TestpressService(restAdapter, authToken);
    }

    public void logout(final Activity activity, TestpressService testpressService,
                       TestpressServiceProvider serviceProvider,
                       LogoutService logoutService) {
        final ProgressDialog progressDialog = new ProgressDialog(activity,
                R.style.AppCompatAlertDialogStyle);

        progressDialog.setTitle(R.string.label_logging_out);
        progressDialog.setMessage(activity.getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(activity, progressDialog, 4);
        progressDialog.show();
        serviceProvider.invalidateAuthToken(activity);
        SharedPreferences preferences = activity.getSharedPreferences(Constants.GCM_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        setDashboardData(activity, "{}");
        preferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
        CommonUtils.registerDevice(activity, testpressService, serviceProvider);
        TestpressApplication.clearDatabase(activity);
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if(activity.getClass() == MainActivity.class) {
                    intent = activity.getIntent();
                } else {
                    intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                progressDialog.dismiss();
                activity.finish();
                activity.startActivity(intent);
            }
        });
    }
}

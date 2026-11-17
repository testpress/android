package in.testpress.testpress;

import android.accounts.AccountsException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.exoplayer2.offline.DownloadService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.services.VideoDownloadService;
import in.testpress.database.TestpressDatabase;
import in.testpress.network.RetrofitCall;
import in.testpress.network.TestpressApiClient;
import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.util.AppChecker;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.util.UIUtils;
import retrofit.RestAdapter;

import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.DISPLAY_USERNAME_ON_VIDEO;
import static in.testpress.testpress.BuildConfig.SHOW_PDF_VERTICALLY;
import static in.testpress.testpress.BuildConfig.GROWTH_HACKS_ENABLED;
import static in.testpress.testpress.BuildConfig.SHARE_MESSAGE;
import static in.testpress.testpress.BuildConfig.ZOOM_CUSTOM_MEETING_UI_ENABLED;
import static in.testpress.testpress.util.PreferenceManager.setDashboardData;

public class TestpressServiceProvider {
    private static final String TAG = "TestpressServiceProvider";
    // Bounded timeout for server logout: 6 seconds allows in-flight server logout requests
    // to complete on mobile / cellular or throttled connections without indefinitely freezing the user interface.
    // If the network is completely offline or the server takes longer than 6 seconds, the call
    // is cancelled and local teardown proceeds so the user is never trapped in the app.
    //
    // Known limitation / architectural trade-off:
    // On high-latency or throttled networks where logout takes longer than 6 seconds, the call is
    // cancelled and local session credentials are wiped. Because offline pending logout persistence was
    // intentionally omitted to avoid storing unencrypted auth tokens in plaintext SharedPreferences, the
    // server session may remain active until natural expiry if the server did not process the cancelled call.
    // If the user immediately attempts to re-login under Parallel Login Restriction, they could encounter
    // a lockout until that session expires. This trade-off is preferred over storing plaintext credentials
    // or leaving the user trapped in a non-cancellable hanging UI.
    //
    // When local session data is cleared, GCMPreference.SENT_TOKEN_TO_SERVER is reset to false,
    // ensuring that MainActivity re-registers the push token anonymously upon next app launch.
    private static final long SERVER_LOGOUT_TIMEOUT_MS = 6000L;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private RestAdapter.Builder restAdapter;
    private ApiKeyProvider keyProvider;
    String authToken;

    public TestpressServiceProvider(RestAdapter.Builder restAdapter, ApiKeyProvider keyProvider) {
        this.restAdapter = restAdapter;
        this.keyProvider = keyProvider;
    }

    public void invalidateAuthToken(Context context) {
        authToken = null;
        if (hasFacebookAppID(context)) {
            FacebookSdk.sdkInitialize(context.getApplicationContext());
            LoginManager.getInstance().logOut();
        }
        TestpressSdk.clearActiveSession(context);
    }

    private boolean hasFacebookAppID(Context context){
        ApplicationInfo applicationInfo = getApplicationInfo(context);
        if (applicationInfo == null) return false;
        String appId = applicationInfo.metaData.getString(FacebookSdk.APPLICATION_ID_PROPERTY);
        return !TextUtils.isEmpty(appId);
    }

    private ApplicationInfo getApplicationInfo(Context context){
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
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
            String facebookAppId = null;

            if (instituteSettingsList.isEmpty()) {
                settings = new in.testpress.models.InstituteSettings(BASE_URL);
                settings.setWhiteLabeledHostUrl(BuildConfig.WHITE_LABELED_HOST_URL);
                settings.setScreenshotDisabled(true);
                settings.setVideoDownloadEnabled(true);
                settings.setShowPDFVertically(SHOW_PDF_VERTICALLY);
            } else {
                InstituteSettings instituteSettings = instituteSettingsList.get(0);
                facebookAppId = instituteSettings.getFacebookAppId();
                settings = new in.testpress.models.InstituteSettings(instituteSettings.getBaseUrl())
                        .setWhiteLabeledHostUrl(BuildConfig.WHITE_LABELED_HOST_URL)
                        .setCurrentPaymentApp(instituteSettings.getCurrentPaymentApp())
                        .setBookmarksEnabled(instituteSettings.getBookmarksEnabled())
                        .setCoursesFrontend(instituteSettings.getShowGameFrontend())
                        .setCoursesGamificationEnabled(instituteSettings.getCoursesEnableGamification())
                        .setCommentsVotingEnabled(instituteSettings.getCommentsVotingEnabled())
                        .setScreenshotDisabled(!instituteSettings.getAllowScreenshotInApp())
                        .setDisplayUserEmailOnVideo(DISPLAY_USERNAME_ON_VIDEO)
                        .setAccessCodeEnabled(false)
                        .setDeviceBindingMode(instituteSettings.getDeviceBindingMode())
                        .setEnableParallelLoginRestriction(instituteSettings.getEnableParallelLoginRestriction())
                        .setMaxParallelLogins(instituteSettings.getMaxParallelLogins())
                        .setLockoutLimit(instituteSettings.getLockoutLimit())
                        .setCooloffTime(instituteSettings.getCooloffTime())
                        .setStoreLabel(instituteSettings.getStoreLabel())
                        .setAppToolbarLogo(instituteSettings.getAppToolbarLogo())
                        .setAppShareLink(instituteSettings.getAppShareLink())
                        .setServerTime(instituteSettings.serverTime())
                        .setLeaderboardLabel(instituteSettings.getLeaderboardLabel())
                        .setVideoDownloadEnabled(instituteSettings.getIsVideoDownloadEnabled())
                        .setThreatsAndTargetsLabel(instituteSettings.getThreatsAndTargetsLabel())
                        .setShowPDFVertically(SHOW_PDF_VERTICALLY)
                        .setMaxAllowedDownloadedVideos(instituteSettings.getMaxAllowedDownloadedVideos())
                        .setEnableCustomTest(instituteSettings.getEnableCustomTest())
                        .setStoreEnabled(instituteSettings.getStoreEnabled())
                        .setAndroidSentryDns(instituteSettings.getAndroidSentryDns())
                        .setDisableImageFullscreenZoomInExam(instituteSettings.getDisableImageFullscreenZoomInExam(activity))
                        .setEnableOfflineExam(instituteSettings.getEnableOfflineExam(activity))
                        .setDisableStoreInApp(instituteSettings.getDisableStoreInApp())
                        .setVideoWatermarkType(instituteSettings.getVideoWatermarkType())
                        .setVideoWatermarkPosition(instituteSettings.getVideoWatermarkPosition())
                        .setUseNewDiscountFeat(instituteSettings.getUseNewDiscountFeat())
                        .setRequireQuestionReportDescription(Boolean.TRUE.equals(instituteSettings.getRequireQuestionReportDescription()))
                        .setQuestionReportDescriptionMinLength(instituteSettings.getQuestionReportDescriptionMinLength() != null ? instituteSettings.getQuestionReportDescriptionMinLength() : 0);
                appLink = instituteSettings.getAppShareLink();
            }
            settings.setAppShareText(SHARE_MESSAGE + activity.getString(R.string.get_it_at) + appLink);
            settings.setGrowthHackEnabled(GROWTH_HACKS_ENABLED);
            settings.setAppName(activity.getString(R.string.app_name));
            settings.setIsCustomMeetingUIEnabled(ZOOM_CUSTOM_MEETING_UI_ENABLED);
            if (facebookAppId != null && !facebookAppId.isEmpty()) {
                settings.enableFacebookEventTracking(facebookAppId, activity.getApplication());
            }

            TestpressSdk.setTestpressSession(activity, new TestpressSession(settings, authToken));
        }

        // TODO: See how that affects the testpress service.
        return new TestpressService(restAdapter, authToken);
    }

    public void logout(@NonNull final Activity activity,
                       @Nullable final TestpressService testpressService,
                       @NonNull final TestpressServiceProvider serviceProvider,
                       @NonNull final LogoutService logoutService) {
        final Context appContext = activity.getApplicationContext();
        final WeakReference<Activity> activityRef = new WeakReference<>(activity);
        final ProgressDialog progressDialog = isActivityDestroyed(activity) ? null : showProgressDialog(activity);
        final WeakReference<ProgressDialog> progressDialogRef = progressDialog != null ? new WeakReference<>(progressDialog) : null;
        if (progressDialog == null) {
            Log.w(TAG, "logout called with finishing or destroyed Activity; proceeding with teardown without dialog");
        }

        final LogoutContext logoutContext = new LogoutContext(appContext, testpressService, serviceProvider, logoutService, activityRef, progressDialogRef);

        performServerLogout(appContext, new Runnable() {
            @Override
            public void run() {
                performLocalCleanupAndAccountRemoval(logoutContext);
            }
        });
    }

    private void performServerLogout(@NonNull final Context context, @NonNull final Runnable onComplete) {
        TestpressSession session = TestpressSdk.getTestpressSession(context);
        if (session == null) {
            onComplete.run();
            return;
        }

        final AtomicBoolean hasProceeded = new AtomicBoolean(false);
        final AtomicReference<RetrofitCall<Void>> callRef = new AtomicReference<>();

        final Runnable proceedOnce = new Runnable() {
            @Override
            public void run() {
                if (hasProceeded.compareAndSet(false, true)) {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        onComplete.run();
                    } else {
                        MAIN_HANDLER.post(onComplete);
                    }
                }
            }
        };

        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "Server logout timed out after " + SERVER_LOGOUT_TIMEOUT_MS + "ms, cancelling in-flight call");
                RetrofitCall<Void> call = callRef.get();
                if (call != null) {
                    call.cancel();
                }
                proceedOnce.run();
            }
        };

        MAIN_HANDLER.postDelayed(timeoutRunnable, SERVER_LOGOUT_TIMEOUT_MS);

        try {
            RetrofitCall<Void> logoutCall = new TestpressApiClient(context, session).logout();
            callRef.set(logoutCall);
            logoutCall.enqueue(new TestpressCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    MAIN_HANDLER.removeCallbacks(timeoutRunnable);
                    proceedOnce.run();
                }

                @Override
                public void onException(TestpressException exception) {
                    Log.w(TAG, "Server logout failed with exception", exception);
                    MAIN_HANDLER.removeCallbacks(timeoutRunnable);
                    proceedOnce.run();
                }
            });
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.w(TAG, "Failed to initiate server logout", e);
            MAIN_HANDLER.removeCallbacks(timeoutRunnable);
            proceedOnce.run();
        }
    }

    private void performLocalCleanupAndAccountRemoval(@NonNull final LogoutContext params) {
        clearLocalSessionData(params.appContext, params.serviceProvider);

        params.logoutService.logout(new Runnable() {
            @Override
            public void run() {
                registerAnonymousDeviceAndNavigate(params);
            }
        });
    }

    private void registerAnonymousDeviceAndNavigate(@NonNull final LogoutContext params) {
        if (params.testpressService != null) {
            params.testpressService.invalidateAuthToken();
        }

        TestpressService serviceToRegister = params.testpressService != null
                ? params.testpressService
                : new TestpressService(params.serviceProvider.restAdapter, null);
        CommonUtils.registerDevice(params.appContext, serviceToRegister, params.serviceProvider);

        dismissDialogSafely(params.progressDialogRef);
        navigateToMainActivity(params.appContext, params.activityRef);
    }

    private void clearLocalSessionData(@NonNull final Context appContext,
                                       @NonNull final TestpressServiceProvider serviceProvider) {
        serviceProvider.invalidateAuthToken(appContext);
        SharedPreferences preferences = appContext.getSharedPreferences(Constants.GCM_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        setDashboardData(appContext, "{}");
        preferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
        TestpressApplication.clearDatabase(appContext);
    }

    private ProgressDialog showProgressDialog(@NonNull final Activity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity, R.style.AppCompatAlertDialogStyle);
        progressDialog.setTitle(R.string.label_logging_out);
        progressDialog.setMessage(activity.getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(activity, progressDialog, 4);
        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException | IllegalStateException e) {
            Log.w(TAG, "Could not show progress dialog, window token or activity state is invalid", e);
        }
        return progressDialog;
    }

    private void dismissDialogSafely(@Nullable final WeakReference<ProgressDialog> progressDialogRef) {
        if (progressDialogRef == null) {
            return;
        }
        try {
            ProgressDialog progressDialog = progressDialogRef.get();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed to dismiss progress dialog because Activity window was already detached", e);
        } finally {
            progressDialogRef.clear();
        }
    }

    private void navigateToMainActivity(@NonNull final Context appContext,
                                       @NonNull final WeakReference<Activity> activityRef) {
        Activity activity = activityRef.get();
        Intent intent;
        if (activity != null && !isActivityDestroyed(activity)) {
            if (activity.getClass() == MainActivity.class) {
                intent = activity.getIntent();
            } else {
                intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            activity.finish();
            activity.startActivity(intent);
        } else {
            intent = new Intent(appContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            appContext.startActivity(intent);
        }
    }

    private boolean isActivityDestroyed(@Nullable final Activity activity) {
        return activity == null || activity.isFinishing() || activity.isDestroyed();
    }

    private static class LogoutContext {
        @NonNull final Context appContext;
        @Nullable final TestpressService testpressService;
        @NonNull final TestpressServiceProvider serviceProvider;
        @NonNull final LogoutService logoutService;
        @NonNull final WeakReference<Activity> activityRef;
        @Nullable final WeakReference<ProgressDialog> progressDialogRef;

        LogoutContext(@NonNull final Context appContext,
                      @Nullable final TestpressService testpressService,
                      @NonNull final TestpressServiceProvider serviceProvider,
                      @NonNull final LogoutService logoutService,
                      @NonNull final WeakReference<Activity> activityRef,
                      @Nullable final WeakReference<ProgressDialog> progressDialogRef) {
            this.appContext = appContext;
            this.testpressService = testpressService;
            this.serviceProvider = serviceProvider;
            this.logoutService = logoutService;
            this.activityRef = activityRef;
            this.progressDialogRef = progressDialogRef;
        }
    }
}

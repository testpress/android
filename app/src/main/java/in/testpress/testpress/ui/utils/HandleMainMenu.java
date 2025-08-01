package in.testpress.testpress.ui.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.ui.OfflineExamListActivity;
import in.testpress.exam.TestpressExam;
import in.testpress.course.TestpressCourse;
import in.testpress.store.TestpressStore;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.ui.DoubtsActivity;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.PostsListActivity;
import in.testpress.testpress.ui.ProfileDetailsActivity;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UIUtils;
import in.testpress.ui.UserDevicesActivity;
import in.testpress.ui.WebViewWithSSOActivity;

import static in.testpress.exam.api.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.WHITE_LABELED_HOST_URL;
import static in.testpress.testpress.core.Constants.Http.URL_PRIVACY_POLICY_FLAG;
import static in.testpress.testpress.core.Constants.Http.URL_STUDENT_REPORT_FLAG;

import java.util.HashMap;

public class HandleMainMenu {
    private Activity activity;
    private TestpressServiceProvider serviceProvider;
    private InstituteSettings instituteSettings;
    Account[] account;
    boolean isUserAuthenticated;
    private final HashMap<Integer, Runnable> menuActions = new HashMap<>();

    public HandleMainMenu(Activity activity, TestpressServiceProvider serviceProvider) {
        this.activity = activity;
        this.serviceProvider = serviceProvider;

        DaoSession daoSession =
                ((TestpressApplication) activity.getApplicationContext()).getDaoSession();
        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        instituteSettings = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list().get(0);

        AccountManager manager = (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
        account = manager.getAccountsByType(APPLICATION_ID);
        isUserAuthenticated = account.length > 0;
        initializeMenuActions();
    }

    void initializeMenuActions() {
        menuActions.put(R.id.share, this::shareApp);
        menuActions.put(R.id.rate_us, this::rateApp);
        menuActions.put(R.id.privacy_policy, this::openPrivacyPolicy);
        menuActions.put(R.id.logout, () -> ((MainActivity) activity).logout());
        menuActions.put(R.id.bookmarks, () -> checkAuthenticationAndOpen(R.string.bookmarks));
        menuActions.put(R.id.posts, () -> {
            String custom_title = UIUtils.getMenuItemName(R.string.posts, instituteSettings);
            Intent intent = new Intent(activity, PostsListActivity.class);
            intent.putExtra("userAuthenticated", isUserAuthenticated);
            intent.putExtra("title", custom_title);
            activity.startActivity(intent);
        });
        menuActions.put(R.id.analytics, () -> checkAuthenticationAndOpen(R.string.analytics));
        menuActions.put(R.id.login_activity, () -> {
            Intent intent = new Intent(activity, UserDevicesActivity.class);
            activity.startActivity(intent);
        });
        menuActions.put(R.id.doubts, () -> {
            Intent intent = new Intent(activity, DoubtsActivity.class);
            activity.startActivity(intent);
        });
        menuActions.put(R.id.offline_exam_list, this::launchOfflineExamListActivity);
        menuActions.put(R.id.discussions, () -> {
            String label = instituteSettings.getForumLabel();
            label = label != null ? label : "Discussion";
            launchDiscussionActivity(label);
        });
        menuActions.put(R.id.profile, () -> {
            Intent intent = new Intent(activity, ProfileDetailsActivity.class);
            activity.startActivity(intent);
        });
        menuActions.put(R.id.login, () -> {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.putExtra(Constants.DEEP_LINK_TO, "home");
            activity.startActivity(intent);
        });
        menuActions.put(R.id.student_report, this::launchStudentReportActivity);
        menuActions.put(R.id.recorded_lessons, () -> openCakingExternalURL("Recorded Lessons", "/external_site/?endpoint=recorded_lectures"));
        menuActions.put(R.id.mocks, () -> openCakingExternalURL("Mocks", "/external_site/?endpoint=mocks"));
        menuActions.put(R.id.e_books, () -> openCakingExternalURL("E-Books", "/external_site/?endpoint=e-books"));
        menuActions.put(R.id.live_lectures_cat, () -> openCakingExternalURL("CAT", "/external_site/?endpoint=cat/40-days-challenge"));
        menuActions.put(R.id.live_lectures_non_cat, () -> openCakingExternalURL("NON-CAT", "/external_site/?endpoint=noncat/non-cat-40-days-challenge"));
        menuActions.put(R.id.live_lectures_gd_watpi, () -> openCakingExternalURL("GD WATPI", "/external_site/?endpoint=gdwatpi/todays-classes"));
    }

    public void handleMenuOptionClick(int itemId) {
        Runnable action = menuActions.get((int) itemId);
        if (action != null) {
            action.run();
        } else {
            Log.w("Menu", "No action found for menu item id: " + itemId);
        }
    }

    void checkAuthenticationAndOpen(final int clickedMenuTitleResId) {
        if (!CommonUtils.isUserAuthenticated(activity)) {
            ((MainActivity) activity).logout();
            return;
        }

        if (TestpressSdk.hasActiveSession(activity)) {
            showSDK(clickedMenuTitleResId);
        } else {
            new SafeAsyncTask<Void>() {
                @Override
                public Void call() throws Exception {
                    serviceProvider.getService(activity);
                    return null;
                }

                @Override
                protected void onSuccess(Void aVoid) throws Exception {
                    showSDK(clickedMenuTitleResId);
                }
            }.execute();
        }
    }

    void showSDK(int clickedMenuTitleResId) {
        TestpressSession session = TestpressSdk.getTestpressSession(activity);
        assert session != null;
        HashMap<Integer, Runnable> sdkActions = new HashMap<>();
        sdkActions.put(R.string.my_exams, () -> TestpressExam.showCategories(activity, true, session));
        sdkActions.put(R.string.bookmarks, () -> TestpressCourse.showBookmarks(activity, session));
        sdkActions.put(R.string.analytics, () -> TestpressExam.showAnalytics(activity, SUBJECT_ANALYTICS_PATH, session));
        sdkActions.put(R.string.store, () -> {
            String title = UIUtils.getMenuItemName(R.string.store, instituteSettings);
            Intent intent = new Intent();
            intent.putExtra("title", title);
            activity.setIntent(intent);
            TestpressStore.show(activity, session);
        });

        Runnable action = sdkActions.get(clickedMenuTitleResId);
        if (action != null) {
            action.run();
        }
    }

    void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String appLink = "https://play.google.com/store/apps/details?id=" + activity.getPackageName();
        if (!TextUtils.isEmpty(instituteSettings.getAppShareLink())) {
            appLink = instituteSettings.getAppShareLink();
        }
        share.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_message) +
                activity.getString(R.string.get_it_at) + appLink);
        activity.startActivity(Intent.createChooser(share, "Share with"));
    }

    private void launchStudentReportActivity() {
        activity.startActivity(
                WebViewWithSSOActivity.Companion.createIntent(
                        activity,
                        activity.getString(R.string.student_report),
                        WHITE_LABELED_HOST_URL + URL_STUDENT_REPORT_FLAG,
                        true,
                        false,
                        WebViewWithSSOActivity.class
                )
        );
    }

    private void openPrivacyPolicy() {
        activity.startActivity(
                WebViewWithSSOActivity.Companion.createIntent(
                        activity,
                        activity.getString(R.string.privacy_policy),
                        WHITE_LABELED_HOST_URL + URL_PRIVACY_POLICY_FLAG,
                        false,
                        false,
                        WebViewWithSSOActivity.class
                )
        );
    }

    private void launchDiscussionActivity(String title) {
        activity.startActivity(
                WebViewWithSSOActivity.Companion.createIntent(
                        activity,
                        title,
                        WHITE_LABELED_HOST_URL + "/discussions/new",
                        true,
                        false,
                        WebViewWithSSOActivity.class
                )
        );
    }

    private void launchOfflineExamListActivity() {
        activity.startActivity(new Intent(activity, OfflineExamListActivity.class));
    }

    private void openCakingExternalURL(String title, String url) {
        activity.startActivity(
                WebViewWithSSOActivity.Companion.createIntent(
                        activity,
                        title,
                        WHITE_LABELED_HOST_URL + url,
                        true,
                        true,
                        WebViewWithSSOActivity.class
                )
        );
    }
}

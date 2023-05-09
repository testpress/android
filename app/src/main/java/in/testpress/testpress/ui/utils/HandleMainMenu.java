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
import in.testpress.testpress.ui.StudentReportActivity;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UIUtils;
import in.testpress.ui.UserDevicesActivity;

import static in.testpress.exam.api.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;

public class HandleMainMenu {
    private Activity activity;
    private TestpressServiceProvider serviceProvider;
    private InstituteSettings instituteSettings;
    Account[] account;
    boolean isUserAuthenticated;

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
    }

    public void handleMenuOptionClick(int itemId) {
        Intent intent;
        switch (itemId) {
            case R.id.share:
                shareApp();
                break;
            case R.id.rate_us:
                rateApp();
                break;
            case R.id.logout:
                ((MainActivity) activity).logout();
                break;
            case R.id.bookmarks:
                checkAuthenticationAndOpen(R.string.bookmarks);
                break;
            case R.id.posts:
                String custom_title = UIUtils.getMenuItemName(R.string.posts, instituteSettings);
                intent = new Intent(activity, PostsListActivity.class);
                intent.putExtra("userAuthenticated", isUserAuthenticated);
                intent.putExtra("title", custom_title);
                activity.startActivity(intent);
                break;
            case R.id.analytics:
                checkAuthenticationAndOpen(R.string.analytics);
                break;
            case R.id.login_activity:
                intent = new Intent(activity, UserDevicesActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.doubts:
                Log.d("TAG", "handleMenuOptionClick: ");
                intent = new Intent(activity, DoubtsActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.profile:
                intent = new Intent(activity, ProfileDetailsActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.login:
                intent = new Intent(activity, LoginActivity.class);
                intent.putExtra(Constants.DEEP_LINK_TO, "home");
                activity.startActivity(intent);
                break;
            case  R.id.report:
                launchStudentReportActivity();
                break;
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
        switch (clickedMenuTitleResId) {
            case R.string.my_exams:
                TestpressExam.showCategories(activity, true, session);
                break;
            case R.string.bookmarks:
                TestpressCourse.showBookmarks(activity, session);
                break;
            case R.string.analytics:
                TestpressExam.showAnalytics(activity, SUBJECT_ANALYTICS_PATH, session);
                break;
            case R.string.store:
                String title = UIUtils.getMenuItemName(R.string.store, instituteSettings);
                Intent intent = new Intent();
                intent.putExtra("title", title);
                activity.setIntent(intent);
                TestpressStore.show(activity, session);
                break;
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
        Intent intent = new Intent(activity, StudentReportActivity.class);
        activity.startActivity(intent);
    }
}

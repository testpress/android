package in.testpress.testpress.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import in.testpress.testpress.R;
import in.testpress.testpress.models.InstituteSettings;

import static in.testpress.core.TestpressSdk.getTypeface;

public class UIUtils {
    private static Typeface latoSemiBold;
    private static final String LATO_SEMI_BOLD_FONT_PATH = "fonts/Lato-Semibold.ttf";

    /**
     * Helps determine if the app is running in a Tablet context.
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void setGone(View[] views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static void setVisible(View[] views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static AlertDialog.Builder getAlertDialog(Context context, @StringRes int title,
                                                     @StringRes int messageRes) {

        return new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(messageRes)
                .setNegativeButton(R.string.ok, null);
    }

    public static void openInBrowser(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary));
        CustomTabsIntent customTabsIntent = builder.build();
        try {
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
            int message = wrongUrl ? R.string.wrong_url : R.string.browser_not_available;
            UIUtils.getAlertDialog(context, R.string.not_supported, message).show();
        }
    }

    public static String getMenuItemName(int titleResId, InstituteSettings instituteSettings) {
        switch (titleResId) {
            case R.string.dashboard:
                return Strings.toString(instituteSettings.getDashboardLabel());
            case R.string.testpress_leaderboard:
                return Strings.toString(instituteSettings.getLeaderboardLabel());
            case R.string.bookmarks:
                return Strings.toString(instituteSettings.getBookmarksLabel());
            case R.string.documents:
                return Strings.toString(instituteSettings.getDocumentsLabel());
            case R.string.store:
                return Strings.toString(instituteSettings.getStoreLabel());
            case R.string.posts:
                return Strings.toString(instituteSettings.getPostsLabel());
            case R.string.learn:
                return Strings.toString(instituteSettings.getLearnLabel());
            case R.string.label_username:
                return Strings.toString(instituteSettings.getLoginLabel());
            case R.string.label_password:
                return Strings.toString(instituteSettings.getLoginPasswordLabel());
            default:
                return "";
        }
    }

    public static Typeface getLatoSemiBoldFont(@NonNull Context context) {
        if (latoSemiBold == null) {
            latoSemiBold = getTypeface(context, LATO_SEMI_BOLD_FONT_PATH);
        }
        return latoSemiBold;
    }
}

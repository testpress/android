package in.testpress.testpress.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.Date;

import in.testpress.testpress.R;

import static in.testpress.testpress.util.PreferenceManager.clearUpdateAppDialogPreferences;
import static in.testpress.testpress.util.PreferenceManager.getLastRemindedDate;
import static in.testpress.testpress.util.PreferenceManager.getLaunchedTimes;
import static in.testpress.testpress.util.PreferenceManager.setLastRemindedDate;
import static in.testpress.testpress.util.PreferenceManager.setLaunchedTimes;

public class UpdateAppDialogManager {

    private static final int MINIMUM_APP_LAUNCHES_NEEDED = 3;
    private static final int REMIND_INTERVAL = 3; // In hours

    public static void monitor(Context context) {
        int launchedTimes = getLaunchedTimes(context);
        if (launchedTimes != -1 && launchedTimes < MINIMUM_APP_LAUNCHES_NEEDED) {
            setLaunchedTimes(context, launchedTimes + 1);
        }
    }

    public static boolean canShowDialog(Context context, Integer days) {
        return isNeededAppLaunchesOver(context) && isRemindIntervalOver(context, days);
    }

    private static boolean isNeededAppLaunchesOver(Context context) {
        int launchedTimes = getLaunchedTimes(context);
        return launchedTimes == -1 || launchedTimes >= MINIMUM_APP_LAUNCHES_NEEDED;
    }

    private static boolean isRemindIntervalOver(Context context, Integer days) {
        long lastRemindedDate =  getLastRemindedDate(context);
        if (lastRemindedDate == -1) {
            return true;
        }
        long timeSpanCrossed = new Date().getTime() - lastRemindedDate;
        long oneHourInMillis = 60 * 60 * 1000;
        if (days != null && days < 3) {
            return timeSpanCrossed >= REMIND_INTERVAL * oneHourInMillis;
        }
        return timeSpanCrossed >= 24 * oneHourInMillis;
    }

    public static void showDialog(final Activity activity, boolean forceUpdate, String message) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .theme(Theme.LIGHT)
                .title(R.string.update_your_app)
                .content(message)
                .positiveText(R.string.testpress_update)
                .positiveColorRes(R.color.material_green)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {

                        clearUpdateAppDialogPreferences(activity);
                        dialog.cancel();
                        activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + activity.getPackageName())));

                        activity.finish();
                    }
                });

        if (forceUpdate) {
            builder.cancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    activity.finish();
                }
            });
        } else {
            builder.negativeText(R.string.remained_later);
            builder.negativeColorRes(R.color.material_green);
            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    setLastRemindedDate(activity);
                    setLaunchedTimes(activity, 0);
                }
            });
        }
        builder.show();
    }
}

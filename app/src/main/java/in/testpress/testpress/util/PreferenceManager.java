package in.testpress.testpress.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class PreferenceManager {

    private static final String UPDATE_APP_DIALOG_PREF_NAME = "updateAppDialog";

    private static final String PREF_KEY_LAST_REMINDED_DATE = "lastRemindedDateTime";

    private static final String PREF_KEY_LAUNCHED_TIMES = "launchedTimes";

    private PreferenceManager() {
    }

    private static SharedPreferences getUpdateAppDialogPreferences(Context context) {
        return context.getSharedPreferences(UPDATE_APP_DIALOG_PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getUpdateAppDialogPreferencesEditor(Context context) {
        return getUpdateAppDialogPreferences(context).edit();
    }

    static void clearUpdateAppDialogPreferences(Context context) {
        SharedPreferences.Editor editor = getUpdateAppDialogPreferencesEditor(context);
        editor.clear().apply();
    }

    static void setLastRemindedDate(Context context) {
        SharedPreferences.Editor editor = getUpdateAppDialogPreferencesEditor(context);
        editor.putLong(PREF_KEY_LAST_REMINDED_DATE, new Date().getTime());
        editor.apply();
    }

    static long getLastRemindedDate(Context context) {
        return getUpdateAppDialogPreferences(context).getLong(PREF_KEY_LAST_REMINDED_DATE, -1);
    }

    static void setLaunchedTimes(Context context, int launchedTimes) {
        SharedPreferences.Editor editor = getUpdateAppDialogPreferencesEditor(context);
        editor.putInt(PREF_KEY_LAUNCHED_TIMES, launchedTimes);
        editor.apply();
    }

    static int getLaunchedTimes(Context context) {
        return getUpdateAppDialogPreferences(context).getInt(PREF_KEY_LAUNCHED_TIMES, -1);
    }

}

package in.testpress.testpress.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Date;

import in.testpress.testpress.models.pojo.DashboardResponse;

public class PreferenceManager {

    private static final String UPDATE_APP_DIALOG_PREF_NAME = "updateAppDialog";

    private static final String PREF_KEY_LAST_REMINDED_DATE = "lastRemindedDateTime";

    private static final String PREF_KEY_LAUNCHED_TIMES = "launchedTimes";

    public static final String DASHBOARD_DATA = "dashboardData";

    private PreferenceManager() {
    }

    public static DashboardResponse getDashboardDataPreferences(Context context) {
        String json = context.getSharedPreferences(DASHBOARD_DATA, Context.MODE_PRIVATE).getString(DASHBOARD_DATA, "{}");
        Log.d("PreferenceManger", "getDashboardDataPreferences: " + json);
        return new Gson().fromJson(json, DashboardResponse.class);
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

    public static void setDashboardData(Context context, String data) {
        SharedPreferences.Editor editor = context.getSharedPreferences(DASHBOARD_DATA, Context.MODE_PRIVATE).edit();
        editor.putString(DASHBOARD_DATA, data);
        editor.apply();
    }

    static int getLaunchedTimes(Context context) {
        return getUpdateAppDialogPreferences(context).getInt(PREF_KEY_LAUNCHED_TIMES, -1);
    }

}

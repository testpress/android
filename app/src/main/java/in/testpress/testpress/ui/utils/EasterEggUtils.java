package in.testpress.testpress.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.models.InstituteSettings;
import in.testpress.testpress.core.Constants;

public class EasterEggUtils {
    public static final String EASTER_EGG = "easterEgg";
    public static final String IS_EASTER_EGG_ENABLED = "isEasterEggEnabled";

    private static SharedPreferences getEasterEggPreferences(Context context) {
        return context.getSharedPreferences(EASTER_EGG, Context.MODE_PRIVATE);
    }

    public static boolean isEasterEggEnabled(Context context) {
        return getEasterEggPreferences(context).getBoolean(IS_EASTER_EGG_ENABLED, false);
    }

    static public void enableOrDisableEasterEgg(Context context, boolean status) {
        SharedPreferences.Editor editor = getEasterEggPreferences(context).edit();
        editor.putBoolean(IS_EASTER_EGG_ENABLED, status);
        editor.apply();
    }

    public static void enableScreenShot(Context context) {
        InstituteSettings instituteSettings = TestpressSdk.getTestpressSession(context).getInstituteSettings();
        instituteSettings.setScreenshotDisabled(false);
        TestpressSession session = TestpressSdk.getTestpressSession(context);
        session.setInstituteSettings(instituteSettings);
        TestpressSdk.setTestpressSession(context,  session);
        Toast.makeText(context, "Screenshot is enabled ", Toast.LENGTH_SHORT).show();
    }
}

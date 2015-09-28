package in.testpress.testpress.util;

import android.content.Context;
import android.content.SharedPreferences;

import in.testpress.testpress.core.Constants;

public class GCMPreference {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    private static final String PREFERENCE_NAME = "KiiTest";
    private static final String PROPERTY_REG_ID = "GCMregId";

    static public String getRegistrationId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String registrationId = prefs.getString(Constants.GCM_PROPERTY_REG_ID, "");
        return registrationId;
    }

    static public void setRegistrationId(Context context, String regId) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.GCM_PROPERTY_REG_ID, regId);
        editor.commit();
    }
}

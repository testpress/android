package in.testpress.testpress.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.iid.InstanceIDListenerService;

import in.testpress.testpress.util.GCMPreference;

public class TestpressInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        //Need to fetch updated Instance ID token and notify our server
        SharedPreferences gcmPreferences = getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        gcmPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
    }
    // [END refresh_token]
}
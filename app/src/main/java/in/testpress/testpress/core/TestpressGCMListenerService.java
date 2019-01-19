package in.testpress.testpress.core;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.NotificationHelper;

import static in.testpress.testpress.core.Constants.GCM_PREFERENCE_NAME;

public class TestpressGCMListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getData().get("summary");
        String title = remoteMessage.getData().get("title");
        String url = remoteMessage.getData().get("short_url");
        Log.d(TAG, "Message: " + message);

        if (title != null && message != null && url != null) {
            NotificationHelper.addNotification(this, title, message, url);
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences gcmPreferences = getSharedPreferences(GCM_PREFERENCE_NAME, MODE_PRIVATE);
        gcmPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
    }

}

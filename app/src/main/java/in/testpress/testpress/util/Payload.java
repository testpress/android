package in.testpress.testpress.util;

import java.util.concurrent.TimeUnit;

import android.util.Base64;

public class Payload {
    public static String getPayloadByUsername(String username) {
        // Query string that needs to be encoded
        String query_string = "username=" + username + "&time=" + getEpochTime();
        // Constructing the payload by encoding the query string using Base64
        return Base64.encodeToString(query_string.getBytes(), Base64.NO_WRAP);
    }

    public static String getPayloadByEmail(String email) {
        // Query string that needs to be encoded
        String query_string = "email=" + email + "&time=" + getEpochTime();
        // Constructing the payload by encoding the query string using Base64
        return Base64.encodeToString(query_string.getBytes(), Base64.NO_WRAP);
    }

    private static String getEpochTime() {
        // Epoch time the number of milliseconds since January 1, 1970, 00:00:00 GMT.
        int epoch_time = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        return Integer.toString(epoch_time);
    }
}

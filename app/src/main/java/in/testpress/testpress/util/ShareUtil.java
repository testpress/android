package in.testpress.testpress.util;

import android.app.Activity;
import android.content.Intent;

public class ShareUtil {

    public static void shareUrl(Activity activity, String text, String url) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        if (text == null || text.isEmpty()) {
            share.putExtra(Intent.EXTRA_TEXT,  getContent("Check out this", url));
        } else {
            share.putExtra(Intent.EXTRA_TEXT, getContent(text, url));
        }
        activity.startActivity(Intent.createChooser(share, "Share with"));
    }

    public static String getContent(String text, String url) {
        if ((text.length() + url.length()) < 140) {
            return text + "\n" + url;
        } else {
            text = text.substring(0, (137 - url.length()));
            return text + "...\n" + url;
        }
    }
}

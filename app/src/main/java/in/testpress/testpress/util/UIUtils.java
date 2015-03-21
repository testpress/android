package in.testpress.testpress.util;

import android.content.Context;
import android.content.res.Configuration;

public class UIUtils {

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
}

package in.testpress.testpress.core;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.Strings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Class that builds a User-Agent that is set on all HTTP calls.
 *
 * The user agent will change depending on the version of Android that
 * the user is running, the device their running and the version of the
 * app that they're running. This will allow your remote API to perform
 * User-Agent inspection to provide different logic routes or analytics
 * based upon the User-Agent.
 *
 * Example of what is generated when running the Genymotion Nexus 4 Emulator:
 *
 *      testpress/1.0 (Android 4.2.2; Genymotion Vbox86p / Generic Galaxy Nexus - 4.2.2 - API 17 - 720x1280; )[preload=false;locale=en_US;clientidbase=]
 *
 * The value "preload" means that the app has been preloaded by the manufacturer.
 * Instances of when this might happen is if you partner with a telecom company
 * to ship your app with their new device.
 *
 * If clientidbase is available you "should" be getting the telecom that is operating
 * the device. This is not reliable, but is still useful. 
 */
public class UserAgentProvider implements Provider<String> {

    private static final String APP_NAME = "testpress";

    @Inject protected ApplicationInfo appInfo;
    @Inject protected PackageInfo info;
    @Inject protected TelephonyManager telephonyManager;
    @Inject protected ClassLoader classLoader;

    protected String userAgent;

    @Override
    public String get() {
        if (userAgent == null) {
            synchronized (UserAgentProvider.class) {
                if (userAgent == null) {
                    userAgent = String.format("%s/%s (Android %s; %s %s / %s %s; %s)",
                            APP_NAME,
                            info.versionName,
                            Build.VERSION.RELEASE,
                            Strings.capitalize(Build.MANUFACTURER),
                            Strings.capitalize(Build.DEVICE),
                            Strings.capitalize(Build.BRAND),
                            Strings.capitalize(Build.MODEL),
                            Strings.capitalize(telephonyManager == null ? "not-found" : telephonyManager.getSimOperatorName())
                    );

                    final ArrayList<String> params = new ArrayList<String>();
                    params.add("preload=" + ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)); // Determine if this app was a preloaded app
                    params.add("locale=" + Locale.getDefault());


                    // http://stackoverflow.com/questions/2641111/where-is-android-os-systemproperties
                    try {
                        final Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
                        final Method get = SystemProperties.getMethod("get", String.class);
                        params.add("clientidbase=" + get.invoke(SystemProperties, "ro.com.google.clientidbase"));
                    } catch (Exception ignored) {
                        Ln.d(ignored);
                    }


                    if (params.size() > 0)
                        userAgent += "[" + Strings.join(";", params) + "]";

                }
            }
        }

        return userAgent;
    }
}

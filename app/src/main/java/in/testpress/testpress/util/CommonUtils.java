package in.testpress.testpress.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.content.Loader;

import java.util.List;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.ui.ThrowableLoader;

import static android.content.Context.ACCOUNT_SERVICE;

public class CommonUtils {

    public static void getAuth(final Activity activity,
                               final TestpressServiceProvider serviceProvider,
                               final CheckAuthCallBack checkAuthCallBack) {

        new SafeAsyncTask<TestpressService>() {
            @Override
            public TestpressService call() throws Exception {
                return serviceProvider.getService(activity);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    activity.finish();
                }
            }

            @Override
            protected void onSuccess(final TestpressService testpressService) throws Exception {
                super.onSuccess(testpressService);
                checkAuthCallBack.onSuccess(testpressService);
            }
        }.execute();
    }

    public static abstract class CheckAuthCallBack {
        public abstract void onSuccess(TestpressService testpressService);
    }

    public static boolean isUserAuthenticated(final Activity activity) {
        if (activity == null) {
            return false;
        }
        AccountManager manager = (AccountManager) activity.getSystemService(ACCOUNT_SERVICE);
        Account[] account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        return account.length > 0;
    }

    public static void registerDevice(final Activity activity, TestpressService testpressService,
                               TestpressServiceProvider serviceProvider) {
        if (isUserAuthenticated(activity)) {
            getAuth(activity, serviceProvider, new CheckAuthCallBack() {
                @Override
                public void onSuccess(TestpressService testpressService) {
                    registerDevice(activity, testpressService);
                }
            });
        } else {
            registerDevice(activity, testpressService);
        }
    }

    public static void registerDevice(final Activity activity,
                                      final TestpressService testpressService) {
        new SafeAsyncTask<Device>() {
            @SuppressLint("HardwareIds")
            @Override
            public Device call() throws Exception {
                String token = GCMPreference.getRegistrationId(activity.getApplicationContext());
                return testpressService.register(token, Settings.Secure.getString(
                        activity.getContentResolver(), Settings.Secure.ANDROID_ID));
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
            }

            @Override
            protected void onSuccess(final Device device) throws Exception {
                SharedPreferences preferences = activity.getSharedPreferences(
                        Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }.execute();
    }

    public static <T> Exception getException(Loader<List<T>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<T>>) loader).clearException();
        } else {
            return null;
        }
    }

}

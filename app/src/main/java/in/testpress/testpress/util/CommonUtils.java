package in.testpress.testpress.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.List;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.ui.SplashScreenActivity;
import in.testpress.testpress.ui.ThrowableLoader;

import static android.content.Context.ACCOUNT_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.Settings.Secure.ANDROID_ID;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.core.Constants.GCM_PREFERENCE_NAME;

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
        AccountManager manager = (AccountManager) activity.getSystemService(ACCOUNT_SERVICE);
        Account[] account = manager.getAccountsByType(APPLICATION_ID);
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
                                      final String token,
                                      final TestpressService testpressService) {

        final SharedPreferences preferences =
                activity.getSharedPreferences(GCM_PREFERENCE_NAME, MODE_PRIVATE);

        new SafeAsyncTask<Device>() {
            @SuppressLint("HardwareIds")
            @Override
            public Device call() throws Exception {
                return testpressService.register(token, Settings.Secure.getString(
                        activity.getContentResolver(), ANDROID_ID));
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                preferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
            }

            @Override
            protected void onSuccess(final Device device) throws Exception {
                preferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }.execute();
    }

    public static void registerDevice(final Activity activity,
                                      final TestpressService testpressService) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("registerDevice", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        //noinspection ConstantConditions
                        String token = task.getResult().getToken();
                        registerDevice(activity, token, testpressService);
                    }
                });
    }

    public static <T> Exception getException(Loader<List<T>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<T>>) loader).clearException();
        } else {
            return null;
        }
    }

    public static void openUrlInBrowser(Activity activity, Uri uri) {
        setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, activity);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        activity.startActivity(intent);
        setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, activity);
    }

    private static void setDeepLinkingState(int state, Context context) {
        ComponentName componentName = new ComponentName(context.getPackageName(),
                SplashScreenActivity.class.getName());

        context.getApplicationContext().getPackageManager().setComponentEnabledSetting(
                componentName,
                state,
                PackageManager.DONT_KILL_APP);
    }

}

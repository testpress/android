package in.testpress.testpress.util;

import android.accounts.OperationCanceledException;
import android.app.Activity;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;

public class CommonUtils {

    public static void checkAuth(final Activity activity,
                                 final TestpressServiceProvider serviceProvider,
                                 final CheckAuthCallBack checkAuthCallBack) {

        new SafeAsyncTask<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                TestpressService service = serviceProvider.getService(activity);
                return service != null;
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
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                checkAuthCallBack.onSuccess(hasAuthenticated);
            }
        }.execute();
    }

    public static abstract class CheckAuthCallBack {
        public abstract void onSuccess(Boolean hasAuthenticated);
    }

}

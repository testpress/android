package in.testpress.testpress;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.IOException;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.util.GCMPreference;
import retrofit.RestAdapter;

public class TestpressServiceProvider {
    private RestAdapter.Builder restAdapter;
    private ApiKeyProvider keyProvider;
    String authToken;

    public TestpressServiceProvider(RestAdapter.Builder restAdapter, ApiKeyProvider keyProvider) {
        this.restAdapter = restAdapter;
        this.keyProvider = keyProvider;
    }

    public void invalidateAuthToken(Context context) {
        authToken = null;
        TestpressSdk.clearActiveSession(context);
    }
    /**
     * Get service for configured key provider
     * <p/>
     * This method gets an auth key and so it blocks and shouldn't be called on the main thread.
     *
     * @return testpress service
     * @throws java.io.IOException
     * @throws android.accounts.AccountsException
     */
    public TestpressService getService(final Activity activity)
            throws IOException, AccountsException {
        if (authToken == null) {
            // The call to keyProvider.getAuthKey(...) is what initiates the login screen. Call that now.
            authToken = keyProvider.getAuthKey(activity);
            TestpressSdk.setTestpressSession(activity,
                    new TestpressSession(Constants.Http.URL_BASE, authToken));
        }

        // TODO: See how that affects the testpress service.
        return new TestpressService(restAdapter, authToken);
    }

    public void handleForbidden(final Activity activity, TestpressServiceProvider serviceProvider, LogoutService logoutService) {
        serviceProvider.invalidateAuthToken(activity);
        DaoSession daoSession = ((TestpressApplication) activity.getApplicationContext()).getDaoSession();
        PostDao postDao = daoSession.getPostDao();
        postDao.deleteAll();
        daoSession.clear();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if(activity.getClass() == MainActivity.class) {
                    intent = activity.getIntent();
                } else {
                    intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                activity.startActivity(intent);
                activity.finish();
            }
        });
    }
}

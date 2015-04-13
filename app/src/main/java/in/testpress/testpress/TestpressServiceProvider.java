package in.testpress.testpress;

import android.accounts.AccountsException;
import android.app.Activity;

import java.io.IOException;

import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.core.TestpressService;
import retrofit.RestAdapter;

public class TestpressServiceProvider {
    private RestAdapter restAdapter;
    private ApiKeyProvider keyProvider;
    String authToken;

    public TestpressServiceProvider(RestAdapter restAdapter, ApiKeyProvider keyProvider) {
        this.restAdapter = restAdapter;
        this.keyProvider = keyProvider;
    }

    public void invalidateAuthToken() {
        authToken = null;
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
        }

        // TODO: See how that affects the testpress service.
        return new TestpressService(restAdapter, authToken);
    }
}

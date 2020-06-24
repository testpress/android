package in.testpress.testpress.core;

import android.util.Log;

import io.sentry.Sentry;
import retrofit.RequestInterceptor;

public class RestAdapterRequestInterceptor implements RequestInterceptor {

    private UserAgentProvider userAgentProvider;

    public RestAdapterRequestInterceptor(UserAgentProvider userAgentProvider) {
        this.userAgentProvider = userAgentProvider;
    }

    @Override
    public void intercept(RequestFacade request) {

        // Add header to set content type of JSON
        request.addHeader("Content-Type", "application/json");

        // Add the user agent to the request.
        request.addHeader("User-Agent", userAgentProvider.get());
    }
}

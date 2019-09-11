package in.testpress.testpress.core;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import in.testpress.testpress.R;
import in.testpress.testpress.events.CustomErrorEvent;
import in.testpress.testpress.events.NetworkErrorEvent;
import in.testpress.testpress.events.RestAdapterErrorEvent;
import in.testpress.testpress.events.UnAuthorizedErrorEvent;
import com.squareup.otto.Bus;


import in.testpress.testpress.models.TestpressApiErrorResponse;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

public class RestErrorHandler implements ErrorHandler {

    public static final int HTTP_NOT_FOUND = 404;
    public static final int INVALID_LOGIN_PARAMETERS = 101;

    private Bus bus;

    public RestErrorHandler(Bus bus) {
        this.bus = bus;
    }

    @Override
    public Throwable handleError(RetrofitError cause) {
        TestpressApiErrorResponse errorResponse = (TestpressApiErrorResponse) cause.getBodyAs(TestpressApiErrorResponse.class);

        if (errorResponse.getErrorCode() != null) {
            bus.post(new CustomErrorEvent(errorResponse.getErrorCode(), errorResponse.getDetail()));
        }

        if(cause != null) {
            if (cause.isNetworkError()) {
                bus.post(new NetworkErrorEvent(cause));
            } else if(isUnAuthorized(cause)) {
                bus.post(new UnAuthorizedErrorEvent(cause));
            } else {
                bus.post(new RestAdapterErrorEvent(cause));
            }
        }

        // Example of how you'd check for a unauthorized result
        // if (cause != null && cause.getStatus() == 401) {
        //     return new UnauthorizedException(cause);
        // }

        // You could also put some generic error handling in here so you can start
        // getting analytics on error rates/etc. Perhaps ship your logs off to
        // Splunk, Loggly, etc

        return cause;
    }

    /**
     * If a user passes an incorrect username/password combo in we could
     * get a unauthorized error back from the API. On parse.com this means
     * we get back a HTTP 404 with an error as JSON in the body as such:
     *
     *  {
     *     code: 101,
     *     error: "invalid login parameters"
     *  }
     *
     *  }
     *
     * Therefore we need to check for the 101 and the 404.
     *
     * @param cause The initial error.
     * @return
     */
    private boolean isUnAuthorized(RetrofitError cause) {
        boolean authFailed = false;

        if(cause.getResponse().getStatus() == HTTP_NOT_FOUND) {
            final ApiError err = (ApiError) cause.getBodyAs(ApiError.class);
            if(err != null && err.getCode() == INVALID_LOGIN_PARAMETERS) {
                authFailed = true;
            }
        }

        return authFailed;
    }
}

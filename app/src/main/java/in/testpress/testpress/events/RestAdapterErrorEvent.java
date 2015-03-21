package in.testpress.testpress.events;

import retrofit.RetrofitError;

/**
 * Error that is posted when a non-network error event occurs in the {@link retrofit.RestAdapter}
 */
public class RestAdapterErrorEvent {
    private RetrofitError cause;

    public RestAdapterErrorEvent(RetrofitError cause) {
        this.cause = cause;
    }

    public RetrofitError getCause() {
        return cause;
    }
}

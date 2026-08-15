package in.testpress.testpress.core;

import android.content.Context;

import in.testpress.util.DeviceIdentifier;
import retrofit.RequestInterceptor;

public class RestAdapterRequestInterceptor implements RequestInterceptor {

    private UserAgentProvider userAgentProvider;
    private Context context;

    public RestAdapterRequestInterceptor(UserAgentProvider userAgentProvider, Context context) {
        this.userAgentProvider = userAgentProvider;
        this.context = context;
    }

    @Override
    public void intercept(RequestFacade request) {

        // Add header to set content type of JSON
        request.addHeader("Content-Type", "application/json");

        // Add the user agent to the request.
        request.addHeader("User-Agent", userAgentProvider.get());

        // Add device identifiers
        String deviceId = DeviceIdentifier.INSTANCE.get(context);
        request.addHeader(DeviceIdentifier.HEADER_DEVICE_UID, deviceId);
        request.addHeader(DeviceIdentifier.HEADER_DEVICE_TYPE, DeviceIdentifier.DEVICE_TYPE_MOBILE);
    }
}

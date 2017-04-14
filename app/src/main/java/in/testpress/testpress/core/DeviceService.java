package in.testpress.testpress.core;


import java.util.HashMap;

import in.testpress.testpress.models.Device;
import in.testpress.testpress.models.InstituteSettings;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface DeviceService {

    @POST(Constants.Http.URL_DEVICES_REGISTER_FRAG)
    Device register(@Body HashMap<String, String> arguments);

    @GET(Constants.Http.URL_INSTITUTE_SETTINGS_FRAG)
    InstituteSettings getInstituteSettings();

}

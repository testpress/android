package in.testpress.testpress.core;

import java.util.HashMap;

import in.testpress.testpress.models.ResetPassword;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by shashank on 19/1/16.
 */
public interface ResetPasswordService {
    @POST(Constants.Http.RESET_PASSWORD_URL)
    ResetPassword status(@Body HashMap<String, String> arguments);
}
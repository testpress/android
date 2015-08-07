package in.testpress.testpress.core;

import java.util.HashMap;

import in.testpress.testpress.models.AuthToken;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.Update;
import retrofit.http.Body;
import retrofit.http.POST;

public interface AuthenticationService {

    /**
     * The {@link retrofit.http.Query} values will be transform into query string parameters
     * via Retrofit
     *
     * @param arguments Hashmap of username and password
     * @return A login response.
     */
    @POST(Constants.Http.URL_AUTH_FRAG)
    AuthToken authenticate(@Body HashMap<String, String> arguments);

    @POST(Constants.Http.URL_REGISTER_FRAG)
    RegistrationSuccessResponse register(@Body HashMap<String, String> arguments);

    @POST(Constants.Http.URL_VERIFY_FRAG)
    RegistrationSuccessResponse verifyCode(@Body HashMap<String, String> arguments);

    @POST(Constants.Http.CHECK_UPDATE_URL_Frag)
    Update checkUpdate(@Body HashMap<String, String> arguments);
}

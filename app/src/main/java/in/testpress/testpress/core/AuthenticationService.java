package in.testpress.testpress.core;

import java.util.HashMap;

import in.testpress.testpress.models.AuthToken;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.Update;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

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

    @GET(Constants.Http.URL_PROFILE_DETAILS_FRAG)
    ProfileDetails getProfileDetails(@Header("Authorization") String authorization);

    @PUT("/{updateUserUrlFrag}")
    ProfileDetails updateUser(@EncodedPath("updateUserUrlFrag") String updateUserUrlFrag, @Body HashMap<String, Object> arguments, @Header("Authorization") String authorization);
}

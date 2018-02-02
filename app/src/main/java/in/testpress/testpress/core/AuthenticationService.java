package in.testpress.testpress.core;

import java.util.HashMap;

import in.testpress.testpress.models.AuthToken;
import in.testpress.testpress.models.Form;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.Update;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;

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
    ProfileDetails getProfileDetails();

    @PUT("/{updateUserUrlFrag}")
    ProfileDetails updateUser(@EncodedPath("updateUserUrlFrag") String updateUserUrlFrag,
                              @Body HashMap<String, Object> arguments);

    @GET("/{account_activate_url}")
    Response activateAccount(
            @Path(value = "account_activate_url", encode = false) String accountActivateUrl);

    @GET("/students1617/cgi_api_testpress.php?getforms=testpressforms_2018")
    HashMap<Integer, Form> getForms();

    @POST("/students1617/cgi_api_testpress.php?putforms=testpressforms_2018")
    Integer requestForm(@QueryMap HashMap<String, Object> arguments);
}

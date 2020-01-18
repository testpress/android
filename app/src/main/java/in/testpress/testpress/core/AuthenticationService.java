package in.testpress.testpress.core;

import java.util.HashMap;

import in.testpress.network.RetrofitCall;
import in.testpress.testpress.models.AuthToken;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.network.DashboardResponse;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

import static in.testpress.network.TestpressApiClient.DASHBOARD_PATH;

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

    @GET("/api/v2.4/dashboard/")
    DashboardResponse getDashboardData();
}

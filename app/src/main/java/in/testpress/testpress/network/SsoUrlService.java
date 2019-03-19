package in.testpress.testpress.network;

import java.util.HashMap;

import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.SsoLink;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit2.Call;

public interface SsoUrlService {
    @POST(Constants.Http.PRESIGNED_SSO_URL)
    SsoLink getSsoUrl();

}

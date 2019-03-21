package in.testpress.testpress.network;

import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.SsoUrl;
import retrofit.http.POST;

public interface SsoUrlService {
    @POST(Constants.Http.URL_GENERATE_SSO_LINK)
    SsoUrl getSsoUrl();
}

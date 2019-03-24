package in.testpress.testpress.network;

import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.CheckPermission;
import retrofit.http.GET;

public interface CheckPermissionService {
    @GET(Constants.Http.CHECK_PERMISSION_URL)
    CheckPermission getPermission();

}

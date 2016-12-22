package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Subject;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface AnalyticsService {

    @GET("/{questions_count_url}")
    TestpressApiResponse<Subject> getSubjects(
            @Path("questions_count_url") String urlFrag, @QueryMap Map<String, String> options);

}



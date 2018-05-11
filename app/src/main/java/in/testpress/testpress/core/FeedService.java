package in.testpress.testpress.core;


import java.util.Map;

import in.testpress.testpress.models.ActivityFeedResponse;
import in.testpress.testpress.models.TestpressDataApiResponse;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface FeedService {

    @GET(Constants.Http.URL_ACTIVITY_FEED_FRAG)
    TestpressDataApiResponse<ActivityFeedResponse> getActivityFeed(
            @QueryMap Map<String, String> options);
}
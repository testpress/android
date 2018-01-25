package in.testpress.testpress.core;


import in.testpress.testpress.models.ActivityFeedResponse;
import in.testpress.testpress.models.TestpressDataApiResponse;
import retrofit.http.GET;

public interface FeedService {

    @GET(Constants.Http.URL_ACTIVITY_FEED_FRAG)
    TestpressDataApiResponse<ActivityFeedResponse> getActivityFeed();
}
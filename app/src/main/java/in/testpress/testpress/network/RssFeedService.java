package in.testpress.testpress.network;


import in.testpress.testpress.models.RssFeed;
import retrofit.http.GET;
import retrofit.http.Path;

public interface RssFeedService {

    @GET("/")
    RssFeed getRssFeed();

}

package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.QueryMap;

public interface PostService {

    @GET("/{posts_url}")
    TestpressApiResponse<Post> getPosts(@EncodedPath("posts_url") String postUrl, @QueryMap Map<String, String> options, @Header("Authorization") String authorization);

    @GET("/{post_url}")
    Post getPostDetails(@EncodedPath("post_url") String postUrl, @Header("Authorization") String authorization);

}
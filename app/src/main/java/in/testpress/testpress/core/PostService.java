package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.QueryMap;

public interface PostService {

    @GET("/{posts_url}")
    TestpressApiResponse<Post> getPosts(
            @EncodedPath("posts_url") String postUrl,
            @QueryMap Map<String, String> options,
            @Header("Authorization") String authorization,
            @Header("If-Modified-Since") String latestModifiedDate
    );

    @GET("/{post_url}")
    Post getPostDetails(@EncodedPath("post_url") String postUrl);

    @GET("/{categories_url}")
    TestpressApiResponse<Category> getCategories(@EncodedPath("categories_url") String categoriesUrl, @QueryMap Map<String, String> options, @Header("Authorization") String authorization);

}
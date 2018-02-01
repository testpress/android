package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.Map;

import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Comment;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface PostService {

    @GET("/{posts_url}")
    TestpressApiResponse<Post> getPosts(
            @EncodedPath("posts_url") String postUrl,
            @QueryMap Map<String, String> options,
            @Header("If-Modified-Since") String latestModifiedDate
    );

    @GET("/" + Constants.Http.URL_POSTS_FRAG + "{post_url}")
    Post getPostDetails(@EncodedPath("post_url") String postUrl, @QueryMap Map<String, Boolean> options);

    @GET("/{categories_url}")
    TestpressApiResponse<Category> getCategories(@EncodedPath("categories_url") String categoriesUrl,
                                                 @QueryMap Map<String, String> options);

    @GET("/" + Constants.Http.URL_POSTS_FRAG + "{post_id}" + Constants.Http.URL_COMMENTS_FRAG)
    TestpressApiResponse<Comment> getComments(
            @Path(value = "post_id", encode = false) long postId,
            @QueryMap Map<String, String> options);

    @POST("/" + Constants.Http.URL_POSTS_FRAG + "{post_id}" + Constants.Http.URL_COMMENTS_FRAG)
    Comment sendComments(
            @Path(value = "post_id", encode = false) long postId,
            @Body HashMap<String, String> arguments);
}
package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.Map;

import in.testpress.exam.models.Vote;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Comment;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import retrofit2.http.Url;

public interface PostService {

    @GET("/{posts_url}")
    TestpressApiResponse<Post> getPosts(
            @EncodedPath("posts_url") String postUrl,
            @QueryMap Map<String, String> options,
            @Header("If-Modified-Since") String latestModifiedDate
    );

    @GET("/{forums_url}")
    TestpressApiResponse<Forum> getForums(
            @EncodedPath("forums_url") String postUrl,
            @QueryMap Map<String, String> options,
            @Header("If-Modified-Since") String latestModifiedDate
    );

    @POST("/" + Constants.Http.URL_FORUMS_FRAG)
    Forum postForum(@Body Map<String, String> options);

    @GET("/" + Constants.Http.URL_POSTS_FRAG + "{post_url}")
    Post getPostDetails(@EncodedPath("post_url") String postUrl, @QueryMap Map<String, Boolean> options);

    @GET("/" + Constants.Http.URL_FORUMS_FRAG + "{forum_url}")
    Forum getForumDetails(@EncodedPath("forum_url") String forumUrl, @QueryMap Map<String, Boolean> options);

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

    @POST("/api/v2.3/votes/")
    Vote<Forum> castVote(@Body HashMap<String, Object> params);


    @DELETE("/api/v2.3/votes/" + "{vote_id}/")
    String deleteCommentVote(@Path(value = "vote_id") long id);

    @PUT("/api/v2.3/votes/" + "{vote_id}/")
    Vote<Forum> updateCommentVote(
            @Path(value = "vote_id") long id,
            @Body HashMap<String, Object> params);
}
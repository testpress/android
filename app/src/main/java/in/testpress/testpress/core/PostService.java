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
    @GET("/{product_url}")
    Post getPostDetails(@EncodedPath("product_url") String productUrlFrag, @Header("Authorization") String authorization);

}
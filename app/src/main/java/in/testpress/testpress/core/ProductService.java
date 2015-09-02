package in.testpress.testpress.core;

import java.util.Map;

import in.testpress.testpress.models.Product;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.TestpressApiResponse;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.QueryMap;

public interface ProductService {

    @GET("/{products_url}")
    TestpressApiResponse<Product> getProducts(@EncodedPath("products_url") String productsUrl, @QueryMap Map<String, String> options, @Header("Authorization") String authorization);

    @GET("/{product_url}")
    ProductDetails getProductDetails(@EncodedPath("product_url") String productUrlFrag, @Header("Authorization") String authorization);

}



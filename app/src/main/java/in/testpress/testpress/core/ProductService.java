package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.Map;

import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface ProductService {

    @GET("/{products_url}")
    TestpressApiResponse<Product> getProducts(@EncodedPath("products_url") String productsUrl,
                                              @QueryMap Map<String, String> options);

    @GET("/" + Constants.Http.URL_PRODUCTS_FRAG + "{product_slug}")
    ProductDetails getProductDetails(
            @Path(value = "product_slug", encode = false) String productUrlFrag);

    @POST(Constants.Http.URL_ORDERS_FRAG)
    Order order(@Body HashMap<String, Object> arguments);

    @PUT("/{confirmUrlFrag}")
    Order orderConfirm(@EncodedPath("confirmUrlFrag") String confirmUrlFrag,
                       @Body HashMap<String, Object> argumentsn);

    @GET("/{orders_url}")
    TestpressApiResponse<Order> getOrders(@EncodedPath("orders_url") String ordersUrl);

}



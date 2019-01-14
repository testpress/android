package in.testpress.testpress.core;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.exam.models.Vote;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Comment;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.Notes;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.OrderItem;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.ResetPassword;
import in.testpress.testpress.models.RssFeed;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.network.RssConverterFactory;
import in.testpress.testpress.network.RssFeedService;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class TestpressService {
    private RestAdapter.Builder restAdapter;
    private String authToken;

    public TestpressService() {
    }

    /**
     * Create testpress service
     *
     * @param restAdapter The RestAdapter that allows HTTP Communication.
     */
    public TestpressService(RestAdapter.Builder restAdapter) {
        this.restAdapter = restAdapter;
    }

    public TestpressService(RestAdapter.Builder restAdapter, String authToken) {
        this.restAdapter = restAdapter;
        this.authToken = authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void invalidateAuthToken() {
        authToken = null;
    }

    private RestAdapter getRestAdapter() {
        if (authToken != null) {
            OkHttpClient client = new OkHttpClient();
            Interceptor interceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder header = chain.request().newBuilder();
                    header.addHeader("Authorization", getAuthToken());
                    return chain.proceed(header.build());
                }
            };
            client.networkInterceptors().add(interceptor);
            restAdapter.setClient(new OkClient(client));
        }
        return restAdapter.build();
    }

    private AuthenticationService getAuthenticationService() {
        return getRestAdapter().create(AuthenticationService.class);
    }

    private ProductService getProductsService() { return getRestAdapter().create(ProductService.class); }

    private DocumentsService getDocumentsService() { return getRestAdapter().create(DocumentsService.class); }

    private PostService getPostService() { return getRestAdapter().create(PostService.class); }

    private DeviceService getDevicesService() { return getRestAdapter().create(DeviceService.class); }

    private ResetPasswordService getResetPasswordService(){return getRestAdapter().create(ResetPasswordService.class);}

    private RssFeedService getRssFeedService(String url) {
        restAdapter.setEndpoint(url);
        restAdapter.setConverter(RssConverterFactory.create());
        return restAdapter.build().create(RssFeedService.class);
    }

    private String getAuthToken() {
        return "JWT " + authToken;
    }

    public ResetPassword resetPassword(String email){
        HashMap<String,String> emailcode = new HashMap<String,String>();
        emailcode.put("email",email);
        return getResetPasswordService().resetPassword(emailcode);
    }

    public Update checkUpdate(String version) {
        HashMap<String, String> versioncode = new HashMap<String, String>();
        versioncode.put("version_code", version);
        return getAuthenticationService().checkUpdate(versioncode);
    }

    public String authenticate(String username, String password) {
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        authToken = getAuthenticationService().authenticate(credentials).getToken();
        return authToken;
    }


    public Device register(String registrationId, String deviceId) {
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("registration_id", registrationId);
        credentials.put("device_id", deviceId);
        return getDevicesService().register(credentials);
    }

    public TestpressApiResponse<Post> getPosts(String urlFrag, Map<String, String> queryParams,
                                               String latestModifiedDate) {

        return getPostService().getPosts(urlFrag, queryParams, latestModifiedDate);
    }

    public TestpressApiResponse<Forum> getForums(String urlFrag, Map<String, String> queryParams,
                                                 String latestModifiedDate) {

        return getPostService().getForums(urlFrag, queryParams, latestModifiedDate);
    }

    public TestpressApiResponse<Category> getCategories(String urlFrag,
                                                        Map<String, String> queryParams) {

        return getPostService().getCategories(urlFrag, queryParams);
    }

    public Post getPostDetail(String url, Map<String, Boolean> queryParams) {
        return getPostService().getPostDetails(url, queryParams);
    }

    public Forum getForumDetail(String url, Map<String, Boolean> queryParams) {
        return getPostService().getForumDetails(url, queryParams);
    }

    public TestpressApiResponse<Comment> getComments(long postId, Map<String, String> queryParams) {
        return getPostService().getComments(postId, queryParams);
    }

    public Comment sendComments(long postId, String comment) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("comment", comment);
        return getPostService().sendComments(postId, params);
    }

    public RegistrationSuccessResponse register(String username,String email, String password, String phone, String countryCode){
        HashMap<String, String> userDetails = new HashMap<String, String>();
        userDetails.put("username", username);
        userDetails.put("email", email);
        userDetails.put("password", password);
        if (!phone.trim().isEmpty()) {
            userDetails.put("phone", phone);
            userDetails.put("country_code", countryCode);
        }
        return getAuthenticationService().register(userDetails);
    }

    public RegistrationSuccessResponse verifyCode(String username, String code){
        RegistrationSuccessResponse verificationResponse;
        HashMap<String, String> codeVerificationParameters = new HashMap<String, String>();
        codeVerificationParameters.put("username", username);
        codeVerificationParameters.put("code", code);
        verificationResponse = getAuthenticationService().verifyCode(codeVerificationParameters);
        return verificationResponse;
    }

    public TestpressApiResponse<Product> getProducts(String urlFrag, Map<String, String> queryParams) {
        return getProductsService().getProducts(urlFrag, queryParams);
    }

    public ProductDetails getProductDetail(String productSlug) {
        return getProductsService().getProductDetails(productSlug);
    }

    public TestpressApiResponse<Notes> getDocumentsList(String urlFrag, Map<String, String> queryParams) {
        return getDocumentsService().getDocumentsList(urlFrag, queryParams);
    }

    public Notes getDownloadUrl(String slug) {
        return getDocumentsService().getDownloadUrl(slug);
    }

    public Order order(List<OrderItem> orderItems) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("order_items", orderItems);
        return getProductsService().order(orderParameters);
    }

    public Order orderConfirm(String confirmUrlFrag, String address, String zip, String phone, String landmark, String user, List<OrderItem> orderItems) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("user", user);
        orderParameters.put("order_items", orderItems);
        orderParameters.put("shipping_address", address);
        orderParameters.put("zip", zip);
        orderParameters.put("phone", phone);
        orderParameters.put("land_mark", landmark);
        return getProductsService().orderConfirm(confirmUrlFrag, orderParameters);
    }

    public TestpressApiResponse<Order> getOrders(String urlFrag) {
        return getProductsService().getOrders(urlFrag);
    }

    public ProfileDetails getProfileDetails() {
        return getAuthenticationService().getProfileDetails();
    }

    public ProfileDetails updateUserDetails(String url, String email, String firstName, String lastName, String phone, int gender, String birthDate, String address, String city, int state, String zip) {
        HashMap<String, Object> userParameters = new HashMap<String, Object>();
        userParameters.put("email", email);
        userParameters.put("first_name", firstName);
        userParameters.put("last_name", lastName);
        userParameters.put("phone", phone);
        if(gender == -1) { //if option is --select-- then send ""
            userParameters.put("gender", "");
        } else {
            userParameters.put("gender", gender);
        }
        userParameters.put("birth_date", birthDate);
        userParameters.put("address1", address);
        userParameters.put("city", city);
        if(state == -1) {
            userParameters.put("state_choices", "");
        } else {
            userParameters.put("state_choices", state);
        }
        userParameters.put("zip", zip);
        return getAuthenticationService().updateUser(url, userParameters);
    }

    public ProfileDetails updateProfileImage(String url, String image, int[] cropDetails) {
        HashMap<String, Object> userParameters = new HashMap<String, Object>();
        userParameters.put("photo", image);
        if(cropDetails != null) {
            userParameters.put("x_offset", cropDetails[0]);
            userParameters.put("y_offset", cropDetails[1]);
            userParameters.put("crop_width", cropDetails[2]);
            userParameters.put("crop_height", cropDetails[3]);
        }
        return getAuthenticationService().updateUser(url, userParameters);
    }

    public InstituteSettings getInstituteSettings() {
        return getDevicesService().getInstituteSettings();
    }

    public retrofit.client.Response activateAccount(String urlFrag) {
        return getAuthenticationService().activateAccount(urlFrag);
    }

    public RssFeed getRssFeed(String url) {
        return getRssFeedService(url).getRssFeed();
    }

    public Forum postForum(String title, String content, String category) {
        HashMap<String, String> postParameters = new HashMap<String, String>();
        postParameters.put("title", title);
        postParameters.put("content_html", content);
        if (category != null) {
            postParameters.put("category", category);
        }
        return getPostService().postForum(postParameters);
    }

    public Vote<Forum> castVote(Forum forum, int typeOfVote) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("content_object", forum);
        params.put("type_of_vote", typeOfVote);
        return getPostService().castVote(params);
    }

    public String deleteCommentVote(Forum forum) {
        return getPostService().deleteCommentVote(forum.getVoteId());
    }

    public Vote<Forum> updateCommentVote(Forum forum, int typeOfVote) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("content_object", forum);
        params.put("type_of_vote", typeOfVote);
        return getPostService().updateCommentVote(forum.getVoteId(), params);
    }

}

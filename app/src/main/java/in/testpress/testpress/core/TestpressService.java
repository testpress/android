package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.OrderItem;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.models.Update;
import retrofit.RestAdapter;

public class TestpressService {
    private RestAdapter restAdapter;
    private String authToken;

    public TestpressService() {
    }

    /**
     * Create testpress service
     *
     * @param restAdapter The RestAdapter that allows HTTP Communication.
     */
    public TestpressService(RestAdapter restAdapter) {
        this.restAdapter = restAdapter;
    }

    public TestpressService(RestAdapter restAdapter, String authToken) {
        this.restAdapter = restAdapter;
        this.authToken = authToken;
    }

    private RestAdapter getRestAdapter() {
        return restAdapter;
    }

    private AuthenticationService getAuthenticationService() {
        return getRestAdapter().create(AuthenticationService.class);
    }

    private ExamService getExamsService() { return getRestAdapter().create(ExamService.class); }

    private ProductService getProductsService() { return getRestAdapter().create(ProductService.class); }

    private PostService getPostService() { return getRestAdapter().create(PostService.class); }

    private DeviceService getDevicesService() { return getRestAdapter().create(DeviceService.class); }

    private String getAuthToken() {
        return "JWT " + authToken;
    }

    public TestpressApiResponse<Exam> getExams(String urlFrag, Map<String, String> queryParams) {
        return getExamsService().getExams(urlFrag, queryParams, getAuthToken());
    }

    public TestpressApiResponse<Attempt> getAttempts(String urlFrag) {
        return getExamsService().getAttempts(urlFrag, getAuthToken());
    }

    public TestpressApiResponse<ReviewItem> getReviewItems(String urlFrag) {
        return getExamsService().getReviewItems(urlFrag, getAuthToken());
    }

    public Attempt createAttempt(String attemptsUrlFrag) {
        return getExamsService().createAttempt(attemptsUrlFrag, getAuthToken());
    }

    public Attempt startAttempt(String startAttemptUrlFrag) {
        return getExamsService().startAttempt(startAttemptUrlFrag, getAuthToken());
    }

    public Attempt endAttempt(String endAttemptUrlFrag) {
        return getExamsService().endExam(endAttemptUrlFrag, getAuthToken());
    }

    public TestpressApiResponse<AttemptItem> getQuestions(String questionsUrlFrag) {
        return getExamsService().getQuestions(questionsUrlFrag, getAuthToken());
    }

    public AttemptItem postAnswer(String answerUrlFrag, List<Integer> savedAnswers, Boolean review) {
        HashMap<String, Object> answer = new HashMap<String, Object>();
        answer.put("selected_answers", savedAnswers);
        answer.put("review", review);
        return getExamsService().postAnswer(answerUrlFrag, getAuthToken(), answer);
    }

    public Attempt heartbeat(String heartbeatUrlFrag) {
        return getExamsService().heartbeat(heartbeatUrlFrag, getAuthToken());
    }

    public Attempt endExam(String endExamUrlFrag) {
        return getExamsService().endExam(endExamUrlFrag, getAuthToken());
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
        Device device;
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("registration_id", registrationId);
        credentials.put("device_id", deviceId);
        device = getDevicesService().register(credentials, getAuthToken());
        return device;
    }

    public TestpressApiResponse<Post> getPosts(String urlFrag, Map<String, String> queryParams) {
        return getPostService().getPosts(urlFrag, queryParams, getAuthToken());
    }

    public Post getPostDetail(String url) {
        return getPostService().getPostDetails(url, getAuthToken());
    }

    public RegistrationSuccessResponse register(String username,String email, String password, String phone){
        RegistrationSuccessResponse registrationSuccessResponseResponse;
        HashMap<String, String> userDetails = new HashMap<String, String>();
        userDetails.put("username", username);
        userDetails.put("email", email);
        userDetails.put("password", password);
        userDetails.put("phone", phone);
        registrationSuccessResponseResponse = getAuthenticationService().register(userDetails);
        return registrationSuccessResponseResponse;
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
        return getProductsService().getProducts(urlFrag, queryParams, getAuthToken());
    }

    public ProductDetails getProductDetail(String productUrlFrag) {
        return getProductsService().getProductDetails(productUrlFrag, getAuthToken());
    }

    public Order order(String user, List<OrderItem> orderItems) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("user", user);
        orderParameters.put("order_items", orderItems);
        return getProductsService().order(orderParameters, getAuthToken());
    }

    public Order orderConfirm(String confirmUrlFrag, String address, String zip, String phone, String landmark, String user, List<OrderItem> orderItems) {
        HashMap<String, Object> orderParameters = new HashMap<String, Object>();
        orderParameters.put("user", user);
        orderParameters.put("order_items", orderItems);
        orderParameters.put("shipping_address", address);
        orderParameters.put("zip", zip);
        orderParameters.put("phone", phone);
        orderParameters.put("land_mark", landmark);
        return getProductsService().orderConfirm(confirmUrlFrag, orderParameters, getAuthToken());
    }

    public TestpressApiResponse<Order> getOrders(String urlFrag) {
        return getProductsService().getOrders(urlFrag, getAuthToken());
    }

    public ProfileDetails getProfileDetails() {
        return getAuthenticationService().getProfileDetails(getAuthToken());
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
        return getAuthenticationService().updateUser(url, userParameters, getAuthToken());
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
        return getAuthenticationService().updateUser(url, userParameters, getAuthToken());
    }
}

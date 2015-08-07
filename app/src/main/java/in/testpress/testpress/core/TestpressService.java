package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.models.Exam;
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

    public RegistrationSuccessResponse register(String username,String email, String password, String phone){
        RegistrationSuccessResponse registrationSuccessResponseResponse;
        HashMap<String, String> userDetails = new HashMap<String, String>();
        userDetails.put("username", username);
        userDetails.put("email", email);
        userDetails.put("password", password);
        userDetails.put("phone", phone);
        registrationSuccessResponseResponse =getAuthenticationService().register(userDetails);
        return registrationSuccessResponseResponse;
    }
    public RegistrationSuccessResponse verifyCode(String username, String code){
        RegistrationSuccessResponse verificationResponse;
        HashMap<String, String> codeVerificationParameters = new HashMap<String, String>();
        codeVerificationParameters.put("username", username);
        codeVerificationParameters.put("code", code);
        verificationResponse=getAuthenticationService().verifyCode(codeVerificationParameters);
        return verificationResponse;
    }
}

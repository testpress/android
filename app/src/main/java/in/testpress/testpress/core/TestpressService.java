package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;


import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.models.UserExam;
import in.testpress.testpress.models.Questions;
import in.testpress.testpress.models.Exam;
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

    public List<Exam> getAvailableExams() {
        return getExamsService().getAvailableExams(getAuthToken()).getResults();
    }

    public List<Exam> getUpcomingExams() {
        return getExamsService().getUpcomingExams(getAuthToken()).getResults();
    }

    public List<Exam> getHistoryExams() {
        return getExamsService().getHistoryExams(getAuthToken()).getResults();
    }

    public UserExam getUserExam(String id) {
        HashMap<String, String> Id = new HashMap<String, String>();
        Id.put("id", id);
        return getExamsService().getUserExam(getAuthToken(), Id);
    }

    public TestpressApiResponse<Questions> getQuestions(String questionsUrlFrag) {
        return getExamsService().getQuestions(questionsUrlFrag, getAuthToken());

    }

    public String authenticate(String username, String password) {
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        authToken = getAuthenticationService().authenticate(credentials).getToken();
        return authToken;
    }
}

package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;


import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.TestpressApiResponse;
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

    public TestpressApiResponse<Exam> getExams(String urlFrag) {
        return getExamsService().getExams(urlFrag, getAuthToken());
    }

    public TestpressApiResponse<Attempt> getAttempts(String urlFrag) {
        return getExamsService().getAttempts(urlFrag, getAuthToken());
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

    public AttemptItem postAnswer(String answerUrlFrag, List<Integer> savedAnswers) {
        HashMap<String, List<Integer>> answer = new HashMap<String, List<Integer>>();
        answer.put("selected_answers", savedAnswers);
        return getExamsService().postAnswer(answerUrlFrag, getAuthToken(), answer);
    }

    public Attempt heartbeat(String heartbeatUrlFrag) {
        return getExamsService().heartbeat(heartbeatUrlFrag, getAuthToken());
    }

    public Attempt endExam(String endExamUrlFrag) {
        return getExamsService().endExam(endExamUrlFrag, getAuthToken());
    }

    public String authenticate(String username, String password) {
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        authToken = getAuthenticationService().authenticate(credentials).getToken();
        return authToken;
    }
}

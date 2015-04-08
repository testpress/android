package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;

import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptQuestion;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;

public interface ExamService {

    @GET(Constants.Http.URL_AVAILABLE_EXAMS_FRAG)
    TestpressApiResponse<Exam> getAvailableExams(@Header("Authorization") String authorization);

    @GET(Constants.Http.URL_UPCOMING_EXAMS_FRAG)
    TestpressApiResponse<Exam> getUpcomingExams(@Header("Authorization") String authorization);

    @GET(Constants.Http.URL_HISTORY_EXAMS_FRAG)
    TestpressApiResponse<Exam> getHistoryExams(@Header("Authorization") String authorization);

    @GET("/{questions_url}")
    TestpressApiResponse<AttemptQuestion> getQuestions(@EncodedPath("questions_url") String questionsUrlFrag, @Header("Authorization") String authorization);

    @POST("/{attempts_url}")
    Attempt createAttempt(@EncodedPath("attempts_url") String attemptsUrlFrag, @Header("Authorization") String authorization);

    @PUT("/{start_attempt_url}")
    Attempt startAttempt(@EncodedPath("start_attempt_url") String startAttemptUrlFrag, @Header("Authorization") String authorization);

    @PUT("/{answer_url}")
    AttemptQuestion postAnswer(@EncodedPath("answer_url") String answerUrlFrag, @Header("Authorization") String authorization, @Body HashMap<String, List<Integer>> arguments);

    @PUT("/{heartbeat_url}")
    Attempt heartbeat(@EncodedPath("answer_url") String heartbeatUrlFrag, @Header("Authorization") String authorization);

    @POST("/{end_exam_url}")
    Attempt endExam(@EncodedPath("end_exam_url") String endExamUrlFrag, @Header("Authorization") String authorization);
}



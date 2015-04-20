package in.testpress.testpress.core;

import java.util.HashMap;
import java.util.List;

import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;

public interface ExamService {

    @GET("/{exams_url}")
    TestpressApiResponse<Exam> getExams(@EncodedPath("exams_url") String examsUrl, @Header("Authorization") String authorization);

    @GET("/{questions_url}")
    TestpressApiResponse<AttemptItem> getQuestions(@EncodedPath("questions_url") String questionsUrlFrag, @Header("Authorization") String authorization);

    @GET("/{attempts_url}")
    TestpressApiResponse<Attempt> getAttempts(@EncodedPath("attempts_url") String attemptsUrlFrag, @Header("Authorization") String authorization);

    @GET("/{review_url}")
    TestpressApiResponse<ReviewItem> getReviewItems(@EncodedPath("review_url") String reviewUrlFrag, @Header("Authorization") String authorization);

    @POST("/{attempts_url}")
    Attempt createAttempt(@EncodedPath("attempts_url") String attemptsUrlFrag, @Header("Authorization") String authorization);

    @PUT("/{start_attempt_url}")
    Attempt startAttempt(@EncodedPath("start_attempt_url") String startAttemptUrlFrag, @Header("Authorization") String authorization);

    @PUT("/{answer_url}")
    AttemptItem postAnswer(@EncodedPath("answer_url") String answerUrlFrag, @Header("Authorization") String authorization, @Body HashMap<String, List<Integer>> arguments);

    @PUT("/{heartbeat_url}")
    Attempt heartbeat(@EncodedPath("answer_url") String heartbeatUrlFrag, @Header("Authorization") String authorization);

    @PUT("/{end_exam_url}")
    Attempt endExam(@EncodedPath("end_exam_url") String endExamUrlFrag, @Header("Authorization") String authorization);
}



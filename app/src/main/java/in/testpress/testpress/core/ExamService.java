package in.testpress.testpress.core;

import retrofit.http.GET;
import retrofit.http.Header;

public interface ExamService {

    @GET(Constants.Http.URL_AVAILABLE_EXAMS_FRAG)
    TestpressApiResponse<Exam> getAvailableExams(@Header("Authorization") String authorization);

    @GET(Constants.Http.URL_UPCOMING_EXAMS_FRAG)
    TestpressApiResponse<Exam> getUpcomingExams(@Header("Authorization") String authorization);

    @GET(Constants.Http.URL_HISTORY_EXAMS_FRAG)
    TestpressApiResponse<Exam> getHistoryExams(@Header("Authorization") String authorization);
}



package `in`.testpress.testpress.repository

import `in`.testpress.testpress.models.pojo.ExamResultResponse
import `in`.testpress.testpress.network.EXAM_RESULT_API_TOKEN
import `in`.testpress.testpress.network.ExamResultApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


sealed class ExamResultState {
    object Loading : ExamResultState()
    data class Success(val response: ExamResultResponse) : ExamResultState()
    data class Error(val message: String) : ExamResultState()
}

class ExamResultRepository {

    companion object {
        const val FALLBACK_LIMIT = 10
    }

    /**
     * Fetches a page of exam results for the given student and exam type.
     *
     * @param studentNo The student's login username.
     * @param examType  Either [in.testpress.testpress.network.ExamType.MODEL] or
     *                  [in.testpress.testpress.network.ExamType.WEEKLY].
     * @param pageNo    1-based page number.
     * @param limit     Number of items per page (calculated from screen size by the Fragment).
     * @param onResult  Callback invoked on the main thread with the resulting state.
     */
    fun fetchResults(
        studentNo: String,
        examType: String,
        pageNo: Int,
        limit: Int,
        onResult: (ExamResultState) -> Unit
    ) {
        onResult(ExamResultState.Loading)

        ExamResultApiClient.service.fetchExamResults(
            token = EXAM_RESULT_API_TOKEN,
            studentNo = studentNo,
            pageNo = pageNo,
            limit = limit,
            examType = examType
        ).enqueue(object : Callback<ExamResultResponse> {

            override fun onResponse(
                call: Call<ExamResultResponse>,
                response: Response<ExamResultResponse>
            ) {
                val body = response.body()
                when {
                    !response.isSuccessful -> {
                        onResult(ExamResultState.Error("Server error: ${response.code()}"))
                    }
                    body == null -> {
                        onResult(ExamResultState.Error("Empty response from server"))
                    }
                    !body.isSuccess -> {
                        onResult(ExamResultState.Error(body.message ?: "Request failed"))
                    }
                    else -> {
                        onResult(ExamResultState.Success(body))
                    }
                }
            }

            override fun onFailure(call: Call<ExamResultResponse>, t: Throwable) {
                onResult(ExamResultState.Error(t.message ?: "Network error"))
            }
        })
    }
}

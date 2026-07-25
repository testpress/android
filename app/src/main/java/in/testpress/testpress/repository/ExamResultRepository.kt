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

    private var currentCall: Call<ExamResultResponse>? = null

    /**
     * Fetches a page of exam results for the given student and exam type.
     * Cancels any ongoing call before starting a new request.
     */
    fun fetchResults(
        studentNo: String,
        examType: String,
        pageNo: Int,
        limit: Int,
        onResult: (ExamResultState) -> Unit
    ) {
        cancelPendingRequests()

        onResult(ExamResultState.Loading)

        val call = ExamResultApiClient.service.fetchExamResults(
            token = EXAM_RESULT_API_TOKEN,
            studentNo = studentNo,
            pageNo = pageNo,
            limit = limit,
            examType = examType
        )
        currentCall = call

        call.enqueue(object : Callback<ExamResultResponse> {

            override fun onResponse(
                call: Call<ExamResultResponse>,
                response: Response<ExamResultResponse>
            ) {
                if (call.isCanceled) return

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
                if (call.isCanceled) return
                onResult(ExamResultState.Error(t.message ?: "Network error"))
            }
        })
    }

    fun cancelPendingRequests() {
        currentCall?.cancel()
        currentCall = null
    }
}

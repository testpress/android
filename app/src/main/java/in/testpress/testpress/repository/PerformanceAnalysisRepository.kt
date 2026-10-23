package `in`.testpress.testpress.repository

import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.Gson
import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.PerformanceApiErrorResponse
import `in`.testpress.testpress.models.pojo.PerformanceApiResponse
import `in`.testpress.testpress.models.pojo.PerformanceDashboardData
import `in`.testpress.testpress.models.pojo.PerformanceDataRequest
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFiltersRequest
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceOverviewData
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import `in`.testpress.testpress.network.PerformanceAnalysisApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ConcurrentHashMap

enum class PerformanceErrorType {
    NETWORK,
    HTTP,
    EMPTY_RESPONSE,
    EMPTY_DATA,
    MISSING_API_KEY,
    STUDENT_NOT_FOUND,
    UNAUTHORIZED,
    FORBIDDEN,
    RATE_LIMITED,
    NOT_CONFIGURED,
    UNKNOWN
}

sealed class PerformanceApiState<out T> {
    object Loading : PerformanceApiState<Nothing>()
    data class Success<T>(val data: T) : PerformanceApiState<T>()
    data class Error(
        val errorType: PerformanceErrorType = PerformanceErrorType.UNKNOWN,
        @get:StringRes val messageRes: Int? = null,
        val message: String? = null,
        val code: String? = null,
        val httpCode: Int? = null,
        val retryAfterSeconds: Long? = null,
        val cause: Throwable? = null
    ) : PerformanceApiState<Nothing>() {
        fun getDisplayMessage(context: Context): String {
            return messageRes?.let { context.getString(it) }
                ?: message
                ?: context.getString(R.string.pa_error_generic)
        }
    }
}

enum class PerformanceEndpoint {
    FILTERS,
    OVERVIEW,
    DASHBOARD,
    LEADERBOARD,
    SUBJECTBOARD,
    HISTORY
}

class PerformanceAnalysisRepository(
    private val service: PerformanceAnalysisApiService,
    private val apiKey: String,
    private val gson: Gson
) {

    private val pendingCalls = ConcurrentHashMap<PerformanceEndpoint, Call<*>>()

    fun fetchFilters(
        request: PerformanceFiltersRequest,
        onResult: (PerformanceApiState<PerformanceFiltersData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.FILTERS, onResult) {
            service.fetchFilters(request)
        }
    }

    fun fetchOverview(
        request: PerformanceDataRequest,
        onResult: (PerformanceApiState<PerformanceOverviewData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.OVERVIEW, onResult) {
            service.fetchOverview(request)
        }
    }

    fun fetchDashboard(
        request: PerformanceDataRequest,
        onResult: (PerformanceApiState<PerformanceDashboardData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.DASHBOARD, onResult) {
            service.fetchDashboard(request)
        }
    }

    fun fetchLeaderboard(
        request: PerformanceDataRequest,
        onResult: (PerformanceApiState<PerformanceLeaderboardData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.LEADERBOARD, onResult) {
            service.fetchLeaderboard(request)
        }
    }

    fun fetchSubjectBoard(
        request: PerformanceDataRequest,
        onResult: (PerformanceApiState<PerformanceSubjectBoardData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.SUBJECTBOARD, onResult) {
            service.fetchSubjectBoard(request)
        }
    }

    fun fetchHistory(
        request: PerformanceDataRequest,
        onResult: (PerformanceApiState<PerformanceHistoryData>) -> Unit
    ) {
        enqueue(PerformanceEndpoint.HISTORY, onResult) {
            service.fetchHistory(request)
        }
    }

    fun cancel(endpoint: PerformanceEndpoint) {
        pendingCalls.remove(endpoint)?.cancel()
    }

    fun cancelAll() {
        pendingCalls.values.toList().forEach { it.cancel() }
        pendingCalls.clear()
    }

    private fun <T> enqueue(
        endpoint: PerformanceEndpoint,
        onResult: (PerformanceApiState<T>) -> Unit,
        requestFactory: () -> Call<PerformanceApiResponse<T>>
    ) {
        if (apiKey.isBlank()) {
            onResult(
                PerformanceApiState.Error(
                    errorType = PerformanceErrorType.MISSING_API_KEY,
                    messageRes = R.string.pa_error_missing_api_key,
                    code = "missing_api_key"
                )
            )
            return
        }

        pendingCalls.remove(endpoint)?.cancel()
        onResult(PerformanceApiState.Loading)

        val call = requestFactory()
        pendingCalls[endpoint] = call

        call.enqueue(object : Callback<PerformanceApiResponse<T>> {

            override fun onResponse(
                call: Call<PerformanceApiResponse<T>>,
                response: Response<PerformanceApiResponse<T>>
            ) {
                pendingCalls.remove(endpoint, call)
                if (call.isCanceled) return

                if (!response.isSuccessful) {
                    onResult(parseHttpError(response))
                    return
                }

                val body = response.body()
                when {
                    body == null -> onResult(
                        PerformanceApiState.Error(
                            errorType = PerformanceErrorType.EMPTY_RESPONSE,
                            messageRes = R.string.pa_error_empty_response,
                            httpCode = response.code()
                        )
                    )
                    !body.isSuccess -> onResult(
                        PerformanceApiState.Error(
                            errorType = PerformanceErrorType.HTTP,
                            message = body.message,
                            code = body.code,
                            httpCode = response.code()
                        )
                    )
                    body.data == null -> onResult(
                        PerformanceApiState.Error(
                            errorType = PerformanceErrorType.EMPTY_DATA,
                            messageRes = R.string.pa_error_no_data,
                            httpCode = response.code()
                        )
                    )
                    else -> onResult(PerformanceApiState.Success(body.data))
                }
            }

            override fun onFailure(call: Call<PerformanceApiResponse<T>>, t: Throwable) {
                pendingCalls.remove(endpoint, call)
                if (call.isCanceled) return
                onResult(
                    PerformanceApiState.Error(
                        errorType = PerformanceErrorType.NETWORK,
                        messageRes = R.string.pa_error_network,
                        cause = t
                    )
                )
            }
        })
    }

    private fun <T> parseHttpError(
        response: Response<PerformanceApiResponse<T>>
    ): PerformanceApiState.Error {
        val errorBody = response.errorBody()?.string()
        val error = errorBody?.let {
            runCatching { gson.fromJson(it, PerformanceApiErrorResponse::class.java) }.getOrNull()
        }
        val code = error?.code ?: defaultErrorCode(response.code())
        val errorType = when (code) {
            "student_not_found" -> PerformanceErrorType.STUDENT_NOT_FOUND
            "unauthorized" -> PerformanceErrorType.UNAUTHORIZED
            "forbidden" -> PerformanceErrorType.FORBIDDEN
            "rate_limited" -> PerformanceErrorType.RATE_LIMITED
            "not_configured" -> PerformanceErrorType.NOT_CONFIGURED
            else -> PerformanceErrorType.HTTP
        }
        val messageRes = when (errorType) {
            PerformanceErrorType.STUDENT_NOT_FOUND -> R.string.pa_error_student_not_found
            PerformanceErrorType.UNAUTHORIZED -> R.string.pa_error_unauthorized
            PerformanceErrorType.FORBIDDEN -> R.string.pa_error_forbidden
            PerformanceErrorType.RATE_LIMITED -> R.string.pa_error_rate_limited
            PerformanceErrorType.NOT_CONFIGURED -> R.string.pa_error_not_configured
            else -> if (error?.message.isNullOrBlank()) R.string.pa_error_server else null
        }
        return PerformanceApiState.Error(
            errorType = errorType,
            messageRes = messageRes,
            message = error?.message,
            code = code,
            httpCode = response.code(),
            retryAfterSeconds = response.headers()["Retry-After"]?.toLongOrNull()
        )
    }

    private fun defaultErrorCode(httpCode: Int): String? = when (httpCode) {
        400 -> "invalid_request"
        401 -> "unauthorized"
        403 -> "forbidden"
        404 -> "not_found"
        405 -> "method_not_allowed"
        413 -> "payload_too_large"
        429 -> "rate_limited"
        500 -> "server_error"
        503 -> "not_configured"
        else -> null
    }
}

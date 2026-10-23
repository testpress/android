package `in`.testpress.testpress.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import `in`.testpress.testpress.models.pojo.PerformanceApiResponse
import `in`.testpress.testpress.models.pojo.PerformanceDashboardData
import `in`.testpress.testpress.models.pojo.PerformanceDataRequest
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFiltersRequest
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceOverviewData
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import `in`.testpress.testpress.models.pojo.PerformanceValue
import `in`.testpress.testpress.models.pojo.PerformanceValueDeserializer
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

const val PERFORMANCE_ANALYSIS_BASE_URL = "https://admissions.brilliantpala.org/performanceapi/"
const val PERFORMANCE_ANALYSIS_API_KEY = "f1ce81d8b0bb678116516f2b389e6d413a1ed3a1427d9f06"
const val PERFORMANCE_ANALYSIS_API_KEY_HEADER = "X-Api-Key"

interface PerformanceAnalysisApiService {

    @POST("filters")
    fun fetchFilters(
        @Body request: PerformanceFiltersRequest
    ): Call<PerformanceApiResponse<PerformanceFiltersData>>

    @POST("overview")
    fun fetchOverview(
        @Body request: PerformanceDataRequest
    ): Call<PerformanceApiResponse<PerformanceOverviewData>>

    @POST("dashboard")
    fun fetchDashboard(
        @Body request: PerformanceDataRequest
    ): Call<PerformanceApiResponse<PerformanceDashboardData>>

    @POST("leaderboard")
    fun fetchLeaderboard(
        @Body request: PerformanceDataRequest
    ): Call<PerformanceApiResponse<PerformanceLeaderboardData>>

    @POST("subjectboard")
    fun fetchSubjectBoard(
        @Body request: PerformanceDataRequest
    ): Call<PerformanceApiResponse<PerformanceSubjectBoardData>>

    @POST("history")
    fun fetchHistory(
        @Body request: PerformanceDataRequest
    ): Call<PerformanceApiResponse<PerformanceHistoryData>>
}

object PerformanceAnalysisApiClient {

    val apiKey: String
        get() = PERFORMANCE_ANALYSIS_API_KEY

    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(PerformanceValue::class.java, PerformanceValueDeserializer())
            .create()
    }

    val service: PerformanceAnalysisApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header(PERFORMANCE_ANALYSIS_API_KEY_HEADER, apiKey)
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(PERFORMANCE_ANALYSIS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PerformanceAnalysisApiService::class.java)
    }
}

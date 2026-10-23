package `in`.testpress.testpress.network

import `in`.testpress.testpress.models.pojo.PerformanceApiResponse
import `in`.testpress.testpress.models.pojo.PerformanceDashboardData
import `in`.testpress.testpress.models.pojo.PerformanceDataRequest
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFiltersRequest
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceOverviewData
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

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

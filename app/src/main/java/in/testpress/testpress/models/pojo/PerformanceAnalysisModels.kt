package `in`.testpress.testpress.models.pojo

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class PerformanceApiResponse<T>(
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

data class PerformanceApiErrorResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null
)

data class PerformanceValue(
    val raw: String?,
    val numeric: Double?
) {
    val isText: Boolean get() = raw != null && numeric == null
}

class PerformanceValueDeserializer : JsonDeserializer<PerformanceValue?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PerformanceValue? {
        if (json.isJsonNull) return null
        if (!json.isJsonPrimitive) return PerformanceValue(json.toString(), null)

        val primitive = json.asJsonPrimitive
        return when {
            primitive.isNumber -> PerformanceValue(primitive.asString, primitive.asDouble)
            else -> PerformanceValue(primitive.asString, null)
        }
    }
}

data class PerformanceFiltersRequest(
    @SerializedName("studentno") val studentNo: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: JsonElement? = null
) {
    companion object {
        fun forYear(studentNo: String, type: String?, year: Int): PerformanceFiltersRequest =
            PerformanceFiltersRequest(studentNo, type, JsonPrimitive(year))

        fun allYears(studentNo: String, type: String?): PerformanceFiltersRequest =
            PerformanceFiltersRequest(studentNo, type, JsonPrimitive("all"))
    }
}

data class PerformanceDataRequest(
    @SerializedName("studentno") val studentNo: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("length") val length: Int? = null
)

data class PerformanceStudent(
    @SerializedName("studentno") val studentNo: String? = null,
    @SerializedName("name") val name: String? = null
)

data class PerformanceFiltersData(
    @SerializedName("student") val student: PerformanceStudent? = null,
    @SerializedName("types") val types: List<String> = emptyList(),
    @SerializedName("type") val type: String? = null,
    @SerializedName("years") val years: List<Int> = emptyList(),
    @SerializedName("year") val year: PerformanceValue? = null,
    @SerializedName("subjects") val subjects: Map<String, String> = emptyMap(),
    @SerializedName("months") val months: List<Int> = emptyList(),
    @SerializedName("page_size") val pageSize: Int = 0
)

data class PerformanceOverviewData(
    @SerializedName("dashboard") val dashboard: PerformanceDashboardData? = null,
    @SerializedName("leaderboard") val leaderboard: PerformanceLeaderboardData? = null,
    @SerializedName("subjectboard") val subjectboard: PerformanceSubjectBoardData? = null,
    @SerializedName("history") val history: PerformanceHistoryData? = null
)

data class PerformanceDashboardData(
    @SerializedName("summary") val summary: PerformanceSummary? = null,
    @SerializedName("trend") val trend: PerformanceTrend? = null,
    @SerializedName("rank") val rank: PerformanceRank? = null,
    @SerializedName("focus") val focus: List<PerformanceFocus> = emptyList()
)

data class PerformanceSummary(
    @SerializedName("total_exams") val totalExams: Int = 0,
    @SerializedName("best_score") val bestScore: Double? = null,
    @SerializedName("best_max") val bestMax: Double? = null,
    @SerializedName("best_date") val bestDate: String? = null,
    @SerializedName("avg_score") val avgScore: Double? = null,
    @SerializedName("avg_percent") val avgPercent: Double? = null,
    @SerializedName("best_rank") val bestRank: Int? = null,
    @SerializedName("best_rank_outof") val bestRankOutOf: Int? = null
)

data class PerformanceTrend(
    @SerializedName("labels") val labels: List<String> = emptyList(),
    @SerializedName("student") val student: List<Double?> = emptyList(),
    @SerializedName("topper") val topper: List<Double?> = emptyList(),
    @SerializedName("meta") val meta: List<PerformanceTrendMeta> = emptyList()
)

data class PerformanceTrendMeta(
    @SerializedName("examname") val examName: String? = null,
    @SerializedName("mark") val mark: Double? = null,
    @SerializedName("topper") val topper: Double? = null,
    @SerializedName("max") val max: Double? = null
)

data class PerformanceRank(
    @SerializedName("best_rank") val bestRank: Int? = null,
    @SerializedName("appeared") val appeared: Int? = null,
    @SerializedName("percentile") val percentile: Double? = null
)

data class PerformanceFocus(
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("subject_name") val subjectName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("key") val key: String? = null,
    @SerializedName("percent") val percent: Double? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("prioritylabel") val priorityLabel: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("avg") val avg: Double? = null,
    @SerializedName("avg_max") val avgMax: Double? = null,
    @SerializedName("topper_avg") val topperAvg: Double? = null,
    @SerializedName("topper_percent") val topperPercent: Double? = null,
    @SerializedName("points") val points: List<PerformanceFocusPoint> = emptyList()
) {
    val displayName: String
        get() = subjectName ?: subject ?: name ?: key ?: ""
}

data class PerformanceFocusPoint(
    @SerializedName("label") val label: String? = null,
    @SerializedName("value") val value: Double? = null,
    @SerializedName("topper") val topper: Double? = null,
    @SerializedName("mark") val mark: Double? = null,
    @SerializedName("max") val max: Double? = null
)

data class PerformanceLeaderboardData(
    @SerializedName("data") val data: List<PerformanceLeaderboardRow> = emptyList(),
    @SerializedName("comparison_subject") val comparisonSubject: String? = null,
    @SerializedName("comparison_subject_label") val comparisonSubjectLabel: String? = null,
    @SerializedName("avg_gap") val avgGap: Double? = null
)

data class PerformanceLeaderboardRow(
    @SerializedName("date") val date: String? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("topper") val topper: Double? = null,
    @SerializedName("outof") val outOf: Double? = null,
    @SerializedName("gap") val gap: Double? = null,
    @SerializedName("gap_percent") val gapPercent: Double? = null,
    @SerializedName("progress") val progress: Double? = null,
    @SerializedName("physics") val physics: PerformanceValue? = null,
    @SerializedName("chemistry") val chemistry: PerformanceValue? = null,
    @SerializedName("biology") val biology: PerformanceValue? = null,
    @SerializedName("maths") val maths: PerformanceValue? = null,
    @SerializedName("comparison_subject") val comparisonSubject: PerformanceValue? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("appeared") val appeared: Int? = null
)

data class PerformanceSubjectBoardData(
    @SerializedName("columns") val columns: List<PerformanceSubjectColumn> = emptyList(),
    @SerializedName("data") val data: List<PerformanceSubjectBoardRow> = emptyList()
)

data class PerformanceSubjectColumn(
    @SerializedName("key") val key: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("max") val max: Double? = null
)

data class PerformanceSubjectBoardRow(
    @SerializedName("parts") val parts: Map<String, PerformanceSubjectPart> = emptyMap(),
    @SerializedName("score") val score: Double? = null,
    @SerializedName("maxmarks") val maxMarks: Double? = null,
    @SerializedName("topper") val topper: Double? = null,
    @SerializedName("gap") val gap: Double? = null,
    @SerializedName("gap_max") val gapMax: Double? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("appeared") val appeared: Int? = null
)

data class PerformanceSubjectPart(
    @SerializedName("your") val your: PerformanceValue? = null,
    @SerializedName("topper") val topper: PerformanceValue? = null
)

data class PerformanceHistoryData(
    @SerializedName("data") val data: List<PerformanceHistoryRow> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pages") val pages: Int = 1,
    @SerializedName("length") val length: Int = 0,
    @SerializedName("from") val from: Int? = null,
    @SerializedName("to") val to: Int? = null
)

data class PerformanceHistoryRow(
    @SerializedName("examid") val examId: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("examname") val examName: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("physics") val physics: PerformanceValue? = null,
    @SerializedName("chemistry") val chemistry: PerformanceValue? = null,
    @SerializedName("biology") val biology: PerformanceValue? = null,
    @SerializedName("maths") val maths: PerformanceValue? = null,
    @SerializedName("aptitude") val aptitude: PerformanceValue? = null,
    @SerializedName("drawing") val drawing: PerformanceValue? = null,
    @SerializedName("p1") val p1: PerformanceValue? = null,
    @SerializedName("p2") val p2: PerformanceValue? = null,
    @SerializedName("total") val total: Double? = null,
    @SerializedName("maxmarks") val maxMarks: Double? = null,
    @SerializedName("highest") val highest: Double? = null,
    @SerializedName("percent") val percent: Double? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("appeared") val appeared: Int? = null,
    @SerializedName("omr") val omr: String? = null,
    @SerializedName("omr_url") val omrUrl: String? = null,
    @SerializedName("eright") val eRight: String? = null,
    @SerializedName("ewrong") val eWrong: String? = null,
    @SerializedName("eanswer") val eAnswer: String? = null,
    @SerializedName("eunanswer") val eUnanswered: String? = null,
    @SerializedName("ecancel") val eCancelled: String? = null,
    @SerializedName("eincorrect") val eIncorrect: String? = null,
    @SerializedName("rollno") val rollNo: String? = null,
    @SerializedName("outof") val outOf: String? = null,
    @SerializedName("estatus") val eStatus: String? = null,
    @SerializedName("scholarearn") val scholarEarn: String? = null,
    @SerializedName("remarks") val remarks: String? = null
)

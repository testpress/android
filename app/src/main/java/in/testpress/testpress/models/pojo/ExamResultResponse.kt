package `in`.testpress.testpress.models.pojo

import com.google.gson.annotations.SerializedName

/**
 * Wraps the paginated API response from the student exam results API.
 * The 'status' field is "success" on success or "error" on failure.
 */
data class ExamResultResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("data") val data: List<Map<String, String?>>? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("exam_types") val examTypes: List<String>? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

package `in`.testpress.testpress.models.pojo

import com.google.gson.annotations.SerializedName

/**
 * Represents a single exam result record returned from the student exam results API.
 * Numeric fields (physics, chemistry, etc.) are returned as strings from the API.
 */
data class ExamResult(
    @SerializedName("date") val date: String? = null,
    @SerializedName("examname") val examName: String? = null,
    @SerializedName("physics") val physics: String? = null,
    @SerializedName("chemistry") val chemistry: String? = null,
    @SerializedName("biology") val biology: String? = null,
    @SerializedName("maths") val maths: String? = null,
    @SerializedName("p1") val p1: String? = null,
    @SerializedName("p2") val p2: String? = null,
    @SerializedName("totalmarks") val totalMarks: String? = null,
    @SerializedName("maxmarks") val maxMarks: String? = null,
    @SerializedName("highestmarks") val highestMarks: String? = null,
    @SerializedName("percent") val percent: String? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("rank") val rank: String? = null,
    @SerializedName("stu_appeared") val stuAppeared: String? = null,
    @SerializedName("aptitude") val aptitude: String? = null,
    @SerializedName("drawing") val drawing: String? = null,
    @SerializedName("show_graph") val showGraph: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("omrsheet") val omrSheet: String? = null
)

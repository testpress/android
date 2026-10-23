package `in`.testpress.testpress.ui.utils

import android.content.Context
import android.graphics.Color
import `in`.testpress.testpress.R
import java.text.NumberFormat
import java.util.Locale

data class FocusStyle(
    val priorityColor: Int,
    val badgeBgColor: Int,
    val badgeTextColor: Int,
    val noteBgColor: Int,
    val noteBorderColor: Int,
    val noteTextColor: Int,
    val priorityIcon: String,
    val defaultPriorityLabelRes: Int
)

object PerformanceFormatter {

    const val DEFAULT_SUBJECT_KEY = "totalmarks"

    fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }

    fun formatDecimal(value: Double): String =
        String.format(Locale.getDefault(), "%.1f", value)

    fun formatIntegerWithCommas(value: Int): String =
        NumberFormat.getNumberInstance(Locale.getDefault()).format(value)

    fun getMonthName(context: Context, month: Int): String {
        return when (month) {
            1 -> context.getString(R.string.month_jan)
            2 -> context.getString(R.string.month_feb)
            3 -> context.getString(R.string.month_mar)
            4 -> context.getString(R.string.month_apr)
            5 -> context.getString(R.string.month_may)
            6 -> context.getString(R.string.month_jun)
            7 -> context.getString(R.string.month_jul)
            8 -> context.getString(R.string.month_aug)
            9 -> context.getString(R.string.month_sep)
            10 -> context.getString(R.string.month_oct)
            11 -> context.getString(R.string.month_nov)
            12 -> context.getString(R.string.month_dec)
            else -> month.toString()
        }
    }

    fun formatFocusScore(context: Context, avg: Double?, avgMax: Double?, percent: Double?): String {
        return if (avg != null && avgMax != null) {
            context.getString(
                R.string.pa_focus_score_format,
                formatNumber(avg),
                formatNumber(avgMax),
                formatDecimal(percent ?: 0.0)
            )
        } else if (percent != null) {
            context.getString(R.string.pa_focus_score_percent_only, formatDecimal(percent))
        } else {
            ""
        }
    }

    fun formatFocusTopperScoreHtml(
        context: Context,
        topperAvg: Double?,
        avgMax: Double?,
        topperPercent: Double?
    ): String {
        val topperPercentStr = formatDecimal(topperPercent ?: 0.0)
        return if (topperAvg != null && avgMax != null) {
            context.getString(
                R.string.pa_focus_topper_score_format,
                formatNumber(topperAvg),
                formatNumber(avgMax),
                topperPercentStr
            )
        } else if (topperPercent != null) {
            context.getString(R.string.pa_focus_topper_score_percent_only, topperPercentStr)
        } else {
            ""
        }
    }

    fun formatFocusTrendLabel(context: Context, examCount: Int): String {
        return context.getString(R.string.pa_focus_score_trend_exams_format, examCount)
    }

    fun getFocusStyle(priority: String?): FocusStyle {
        return when (priority?.lowercase(Locale.ROOT)) {
            "high" -> FocusStyle(
                priorityColor = Color.parseColor("#EF4444"),
                badgeBgColor = Color.parseColor("#FEE2E2"),
                badgeTextColor = Color.parseColor("#DC2626"),
                noteBgColor = Color.parseColor("#FEF2F2"),
                noteBorderColor = Color.parseColor("#FECACA"),
                noteTextColor = Color.parseColor("#B91C1C"),
                priorityIcon = "⚠",
                defaultPriorityLabelRes = R.string.pa_focus_priority_high
            )
            "low" -> FocusStyle(
                priorityColor = Color.parseColor("#10B981"),
                badgeBgColor = Color.parseColor("#DCFCE7"),
                badgeTextColor = Color.parseColor("#166534"),
                noteBgColor = Color.parseColor("#F0FDF4"),
                noteBorderColor = Color.parseColor("#BBF7D0"),
                noteTextColor = Color.parseColor("#15803D"),
                priorityIcon = "✔",
                defaultPriorityLabelRes = R.string.pa_focus_priority_low
            )
            else -> FocusStyle( // medium
                priorityColor = Color.parseColor("#F59E0B"),
                badgeBgColor = Color.parseColor("#FEF3C7"),
                badgeTextColor = Color.parseColor("#D97706"),
                noteBgColor = Color.parseColor("#FFFBEB"),
                noteBorderColor = Color.parseColor("#FDE68A"),
                noteTextColor = Color.parseColor("#B45309"),
                priorityIcon = "⚠",
                defaultPriorityLabelRes = R.string.pa_focus_priority_medium
            )
        }
    }
}

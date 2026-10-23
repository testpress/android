package `in`.testpress.testpress.ui.utils

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardRow
import `in`.testpress.testpress.models.pojo.PerformanceSubjectColumn

object PerformanceSubjectTableHelper {

    fun renderSubjectTableHeader(
        context: Context,
        tableLayout: TableLayout,
        columns: List<PerformanceSubjectColumn>
    ) {
        val subjectCols = if (columns.isNotEmpty()) columns else listOf(PerformanceSubjectColumn(key = "-", label = "-"))
        val colSpan = subjectCols.size
        val res = context.resources

        val dateWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_date_width)
        val typeWidth = res.getDimensionPixelSize(R.dimen.pa_col_subject_type_width)
        val subWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_sub_width)
        val rankWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_rank_width)
        val appearedWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_appeared_width)
        val gradeWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_grade_width)
        val gapWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_gap_width)
        val cellPaddingH = res.getDimensionPixelSize(R.dimen.pa_table_cell_padding_h)
        val cellPaddingV = res.getDimensionPixelSize(R.dimen.pa_table_cell_padding_v)
        val headerBg = ContextCompat.getColor(context, R.color.pa_table_header_bg)
        val headerTextColor = ContextCompat.getColor(context, R.color.pa_table_header_text)

        // Header Row 1: Group headers
        val rowHead1 = TableRow(context).apply {
            setBackgroundColor(headerBg)
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = res.getDimensionPixelSize(R.dimen.pa_table_header_sub_min_height)
        }

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(dateWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(typeWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        val tvYourGroup = TextView(context).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
                span = colSpan
            }
            gravity = Gravity.CENTER
            text = context.getString(R.string.pa_subject_th_your_marks)
            setTextColor(headerTextColor)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        rowHead1.addView(tvYourGroup)

        val tvTopperGroup = TextView(context).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
                span = colSpan
            }
            gravity = Gravity.CENTER
            text = context.getString(R.string.pa_subject_th_topper_marks)
            setTextColor(headerTextColor)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        rowHead1.addView(tvTopperGroup)

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(rankWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(appearedWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(gradeWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(context).apply {
            layoutParams = TableRow.LayoutParams(gapWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        tableLayout.addView(rowHead1)

        // Header Row 2: Sub-headers
        val rowHead2 = TableRow(context).apply {
            setBackgroundColor(headerBg)
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = res.getDimensionPixelSize(R.dimen.pa_table_header_main_min_height)
        }

        fun createHeaderCell(width: Int, title: String, isStartGravity: Boolean = false): TextView {
            return TextView(context).apply {
                layoutParams = TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT)
                gravity = if (isStartGravity) Gravity.CENTER_VERTICAL else Gravity.CENTER
                text = title
                setTextColor(headerTextColor)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
        }

        rowHead2.addView(createHeaderCell(dateWidth, context.getString(R.string.pa_subject_th_exam_date), true))
        rowHead2.addView(createHeaderCell(typeWidth, context.getString(R.string.pa_subject_th_exam_type)))

        subjectCols.forEach { col ->
            rowHead2.addView(createHeaderCell(subWidth, col.label ?: col.key ?: context.getString(R.string.performance_results_empty_value)))
        }

        subjectCols.forEach { col ->
            rowHead2.addView(createHeaderCell(subWidth, col.label ?: col.key ?: context.getString(R.string.performance_results_empty_value)))
        }

        rowHead2.addView(createHeaderCell(rankWidth, context.getString(R.string.pa_subject_th_your_rank)))
        rowHead2.addView(createHeaderCell(appearedWidth, context.getString(R.string.pa_subject_th_total_appeared)))
        rowHead2.addView(createHeaderCell(gradeWidth, context.getString(R.string.pa_subject_th_grade)))
        rowHead2.addView(createHeaderCell(gapWidth, context.getString(R.string.pa_subject_th_gap)))

        tableLayout.addView(rowHead2)
    }

    fun createSubjectDataRow(
        context: Context,
        row: PerformanceSubjectBoardRow,
        columns: List<PerformanceSubjectColumn>,
        bgColor: Int
    ): TableRow {
        val subjectCols = if (columns.isNotEmpty()) columns else listOf(PerformanceSubjectColumn(key = "-", label = "-"))
        val emptyDash = context.getString(R.string.performance_results_empty_value)
        val res = context.resources

        val dateWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_date_width)
        val typeWidth = res.getDimensionPixelSize(R.dimen.pa_col_subject_type_width)
        val subWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_sub_width)
        val rankWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_rank_width)
        val appearedWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_appeared_width)
        val gradeWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_grade_width)
        val gapWidth = res.getDimensionPixelSize(R.dimen.pa_col_topper_gap_width)
        val cellPaddingH = res.getDimensionPixelSize(R.dimen.pa_table_cell_padding_h)
        val cellPaddingV = res.getDimensionPixelSize(R.dimen.pa_table_cell_padding_v)

        val cellTextColor = ContextCompat.getColor(context, R.color.pa_table_cell_text)
        val cellStrongColor = ContextCompat.getColor(context, R.color.pa_table_cell_strong)

        val rowView = TableRow(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = res.getDimensionPixelSize(R.dimen.pa_table_row_min_height)
            setBackgroundColor(bgColor)
        }

        fun createTextCell(width: Int, textValue: String, isStrong: Boolean = false, isStartGravity: Boolean = false): TextView {
            return TextView(context).apply {
                layoutParams = TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT)
                gravity = if (isStartGravity) Gravity.CENTER_VERTICAL else Gravity.CENTER
                text = textValue
                setTextColor(if (isStrong) cellStrongColor else cellTextColor)
                if (isStrong) typeface = Typeface.DEFAULT_BOLD
                textSize = 12f
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
        }

        // 1. Date
        rowView.addView(createTextCell(dateWidth, row.date?.takeIf { it.isNotBlank() } ?: emptyDash, isStrong = false, isStartGravity = true))

        // 2. Type (Green Badge)
        val examType = row.type?.takeIf { it.isNotBlank() && it != "-" }
        if (examType != null) {
            val typeFrame = FrameLayout(context).apply {
                layoutParams = TableRow.LayoutParams(typeWidth, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
            val typeBadge = TextView(context).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
                text = examType
                setBackgroundResource(R.drawable.pa_badge_success_bg)
                setTextColor(ContextCompat.getColor(context, R.color.pa_badge_success_text))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                val badgePadH = res.getDimensionPixelSize(R.dimen.pa_badge_padding_h)
                val badgePadV = res.getDimensionPixelSize(R.dimen.pa_badge_padding_v)
                setPadding(badgePadH, badgePadV, badgePadH, badgePadV)
            }
            typeFrame.addView(typeBadge)
            rowView.addView(typeFrame)
        } else {
            rowView.addView(createTextCell(typeWidth, emptyDash))
        }

        // 3. Your Marks
        subjectCols.forEach { col ->
            val part = col.key?.let { row.parts[it] }
            val value = part?.your?.raw?.takeIf { it.isNotBlank() }
                ?: part?.your?.numeric?.let { PerformanceFormatter.formatNumber(it) }
                ?: emptyDash
            rowView.addView(createTextCell(subWidth, value, isStrong = true))
        }

        // 4. Topper Marks
        subjectCols.forEach { col ->
            val part = col.key?.let { row.parts[it] }
            val value = part?.topper?.raw?.takeIf { it.isNotBlank() }
                ?: part?.topper?.numeric?.let { PerformanceFormatter.formatNumber(it) }
                ?: emptyDash
            rowView.addView(createTextCell(subWidth, value))
        }

        // 5. Your Rank
        val rankText = row.rank?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash
        rowView.addView(createTextCell(rankWidth, rankText))

        // 6. Total Appeared
        val appearedText = row.appeared?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash
        rowView.addView(createTextCell(appearedWidth, appearedText))

        // 7. Grade (Badge)
        val gradeFrame = FrameLayout(context).apply {
            layoutParams = TableRow.LayoutParams(gradeWidth, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        val gradeBadge = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            val grade = row.grade?.takeIf { it.isNotBlank() }
            text = if (grade != null && grade != "-") grade else emptyDash
            setBackgroundResource(R.drawable.pa_badge_success_bg)
            setTextColor(ContextCompat.getColor(context, R.color.pa_badge_success_text))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val badgePadH = res.getDimensionPixelSize(R.dimen.pa_badge_padding_h)
            val badgePadV = res.getDimensionPixelSize(R.dimen.pa_badge_padding_v)
            setPadding(badgePadH, badgePadV, badgePadH, badgePadV)
        }
        gradeFrame.addView(gradeBadge)
        rowView.addView(gradeFrame)

        // 8. Gap (Badge)
        val gapFrame = FrameLayout(context).apply {
            layoutParams = TableRow.LayoutParams(gapWidth, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        val gapBadge = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            val gap = row.gap
            if (gap != null) {
                text = PerformanceFormatter.formatNumber(gap)
                if (gap > 0) {
                    setBackgroundResource(R.drawable.pa_gap_high_bg)
                    setTextColor(ContextCompat.getColor(context, R.color.pa_gap_high_text))
                } else {
                    setBackgroundResource(R.drawable.pa_gap_low_bg)
                    setTextColor(ContextCompat.getColor(context, R.color.pa_gap_low_text))
                }
            } else {
                text = emptyDash
                setBackgroundResource(R.drawable.pa_badge_neutral_bg)
                setTextColor(ContextCompat.getColor(context, R.color.pa_badge_neutral_text))
            }
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val gapPadH = (6 * res.displayMetrics.density).toInt()
            val gapPadV = (2 * res.displayMetrics.density).toInt()
            setPadding(gapPadH, gapPadV, gapPadH, gapPadV)
        }
        gapFrame.addView(gapBadge)
        rowView.addView(gapFrame)

        return rowView
    }
}

package `in`.testpress.testpress.ui.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.github.testpress.mikephil.charting.charts.LineChart
import com.github.testpress.mikephil.charting.components.AxisBase
import com.github.testpress.mikephil.charting.components.XAxis
import com.github.testpress.mikephil.charting.data.Entry
import com.github.testpress.mikephil.charting.data.LineData
import com.github.testpress.mikephil.charting.data.LineDataSet
import com.github.testpress.mikephil.charting.formatter.AxisValueFormatter
import com.github.testpress.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.card.MaterialCardView
import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.PerformanceFocus
import `in`.testpress.testpress.ui.view.DonutProgressView

object PerformanceFocusCardHelper {

    fun createFocusCardView(context: Context, focus: PerformanceFocus): View {
        val density = context.resources.displayMetrics.density
        val emptyDash = context.getString(R.string.performance_results_empty_value)
        val style = PerformanceFormatter.getFocusStyle(context, focus.priority)

        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (14 * density).toInt()
            }
            radius = 12 * density
            cardElevation = 0f
            strokeWidth = (1 * density).toInt()
            strokeColor = ContextCompat.getColor(context, R.color.pa_stat_card_border)
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // ── 1. pa-focus-top ──────────────────────────────────
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvIcon = TextView(context).apply {
            text = style.priorityIcon
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(style.priorityColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (8 * density).toInt()
                topMargin = (1 * density).toInt()
            }
        }
        nameCol.addView(tvIcon)

        val titleCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvSubject = TextView(context).apply {
            text = focus.displayName.takeIf { it.isNotBlank() } ?: emptyDash
            setTextColor(ContextCompat.getColor(context, R.color.pa_stat_val_text))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        titleCol.addView(tvSubject)

        val scoreStr = PerformanceFormatter.formatFocusScore(context, focus.avg, focus.avgMax, focus.percent)
        if (scoreStr.isNotBlank()) {
            val tvScore = TextView(context).apply {
                text = scoreStr
                setTextColor(ContextCompat.getColor(context, R.color.pa_stat_top_label))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (2 * density).toInt()
                }
            }
            titleCol.addView(tvScore)
        }

        val topperScoreHtml = PerformanceFormatter.formatFocusTopperScoreHtml(context, focus.topperAvg, focus.avgMax, focus.topperPercent)
        if (topperScoreHtml.isNotBlank()) {
            val tvTopper = TextView(context).apply {
                text = HtmlCompat.fromHtml(topperScoreHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                setTextColor(ContextCompat.getColor(context, R.color.pa_stat_top_label))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (2 * density).toInt()
                }
            }
            titleCol.addView(tvTopper)
        }

        nameCol.addView(titleCol)
        topRow.addView(nameCol)

        // Right: Priority Badge
        val priorityBadge = TextView(context).apply {
            val label = focus.priorityLabel?.takeIf { it.isNotBlank() }
                ?: context.getString(style.defaultPriorityLabelRes)
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(style.badgeTextColor)
            val padH = (10 * density).toInt()
            val padV = (4 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(style.badgeBgColor)
                cornerRadius = 12 * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (8 * density).toInt()
            }
        }
        topRow.addView(priorityBadge)
        container.addView(topRow)

        // ── 2. pa-focus-mid ──────────────────────────────────
        val midRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (14 * density).toInt()
            }
        }

        val noteText = focus.note?.takeIf { it.isNotBlank() }
        val tvNote = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = noteText ?: ""
            setTextColor(style.noteTextColor)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            val padH = (12 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(style.noteBgColor)
                setStroke((1 * density).toInt(), style.noteBorderColor)
                cornerRadius = 8 * density
            }
        }
        midRow.addView(tvNote)

        val donutView = DonutProgressView(context).apply {
            val size = (64 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = (12 * density).toInt()
                marginEnd = (4 * density).toInt()
            }
            progress = (focus.percent ?: 0.0).toFloat()
            progressColor = style.priorityColor
            text = "${PerformanceFormatter.formatDecimal(focus.percent ?: 0.0)}%"
        }
        midRow.addView(donutView)
        container.addView(midRow)

        // ── 3. pa-focus-chartlbl & pa-focus-chart ────────────
        if (focus.points.isNotEmpty()) {
            val tvChartLbl = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (14 * density).toInt()
                    bottomMargin = (8 * density).toInt()
                }
                text = PerformanceFormatter.formatFocusTrendLabel(context, focus.points.size)
                setTextColor(ContextCompat.getColor(context, R.color.pa_stat_top_label))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
            }
            container.addView(tvChartLbl)

            val miniChart = LineChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (74 * density).toInt()
                )
                setDescription("")
                legend.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setViewPortOffsets(0f, (6 * density), 0f, (16 * density))

                val gridColor = ContextCompat.getColor(context, R.color.pa_chart_grid)
                val labelColor = ContextCompat.getColor(context, R.color.pa_chart_label)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawAxisLine(false)
                    setTextColor(labelColor)
                    textSize = 9f
                    isGranularityEnabled = true
                    granularity = 1f
                    setAvoidFirstLastClipping(true)
                    setValueFormatter(object : AxisValueFormatter {
                        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                            val idx = Math.round(value)
                            return if (idx in focus.points.indices) focus.points[idx].label ?: "" else ""
                        }
                        override fun getDecimalDigits(): Int = 0
                    })
                    setLabelCount(focus.points.size.coerceAtLeast(1), false)
                }

                axisLeft.apply {
                    setAxisMinValue(0f)
                    setAxisMaxValue(100f)
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawAxisLine(false)
                    setTextColor(labelColor)
                    textSize = 9f
                    setLabelCount(3, false)
                }

                axisRight.isEnabled = false
            }

            val studentEntries = focus.points.mapIndexedNotNull { i, pt ->
                pt.value?.let { Entry(i.toFloat(), it.toFloat()) }
            }
            val topperEntries = focus.points.mapIndexedNotNull { i, pt ->
                pt.topper?.let { Entry(i.toFloat(), it.toFloat()) }
            }

            val dataSets = mutableListOf<ILineDataSet>()

            if (topperEntries.isNotEmpty()) {
                val topperDs = LineDataSet(topperEntries, context.getString(R.string.pa_focus_topper_series)).apply {
                    val grayColor = ContextCompat.getColor(context, R.color.pa_chart_topper_gray)
                    color = grayColor
                    setCircleColor(grayColor)
                    circleRadius = 2.5f
                    lineWidth = 1.5f
                    enableDashedLine(8f, 6f, 0f)
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.15f
                    setDrawFilled(false)
                }
                dataSets.add(topperDs)
            }

            if (studentEntries.isNotEmpty()) {
                val studentDs = LineDataSet(studentEntries, context.getString(R.string.pa_focus_you_series)).apply {
                    color = style.priorityColor
                    setCircleColor(style.priorityColor)
                    circleRadius = 3f
                    lineWidth = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.15f
                    setDrawFilled(true)
                    fillColor = style.priorityColor
                    fillAlpha = 20
                }
                dataSets.add(studentDs)
            }

            if (dataSets.isNotEmpty()) {
                miniChart.data = LineData(dataSets)
                miniChart.invalidate()
            }

            container.addView(miniChart)
        }

        card.addView(container)
        return card
    }
}

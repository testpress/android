package `in`.testpress.testpress.ui.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.utils.ExamResultTableHelper

class ExamResultAdapter : ListAdapter<Map<String, String?>, ExamResultAdapter.ResultViewHolder>(DIFF_CALLBACK) {

    private var activeColumns: List<String> = emptyList()

    fun updateColumnsAndList(columns: List<String>, list: List<Map<String, String?>>) {
        this.activeColumns = columns
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_result_row, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position), activeColumns, position)
    }

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(result: Map<String, String?>, activeColumns: List<String>, position: Int) {
            itemView.setBackgroundColor(
                if (position % 2 == 0) {
                    itemView.context.getColor(android.R.color.white)
                } else {
                    itemView.context.getColor(R.color.exam_result_row_alt)
                }
            )

            val container = itemView as LinearLayout

            // Rebuild cells if the child count doesn't match activeColumns count
            if (container.childCount != activeColumns.size) {
                container.removeAllViews()
                activeColumns.forEach { col ->
                    val textView = createCellTextView(container.context, col)
                    container.addView(textView)
                }
            }

            // Update content for each active cell
            for (i in activeColumns.indices) {
                val key = activeColumns[i]
                val textView = container.getChildAt(i) as TextView
                val rawValue = result[key]

                textView.text = formatValue(key, rawValue)
                textView.setOnClickListener(null)
                textView.isClickable = false
            }
        }

        private fun createCellTextView(context: Context, key: String): TextView {
            return TextView(context).apply {
                textSize = 13f
                includeFontPadding = false
                
                val widthPx = ExamResultTableHelper.getColumnWidth(context, key)
                layoutParams = LinearLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)

                val horizontalPadding = (6 * context.resources.displayMetrics.density).toInt()
                setPadding(horizontalPadding, 0, horizontalPadding, 0)

                when (key) {
                    "date", "examname" -> {
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        if (key == "date") {
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setTextColor(ContextCompat.getColor(context, R.color.primary))
                        } else {
                            setTextColor(ContextCompat.getColor(context, R.color.exam_result_cell_text))
                        }
                    }
                    "grade" -> {
                        gravity = android.view.Gravity.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        setTextColor(ContextCompat.getColor(context, R.color.primary))
                    }
                    "rank" -> {
                        gravity = android.view.Gravity.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        setTextColor(ContextCompat.getColor(context, R.color.exam_result_cell_text))
                    }
                    "omrsheet" -> {
                        gravity = android.view.Gravity.CENTER
                        setTextColor(ContextCompat.getColor(context, R.color.primary))
                    }
                    "remarks" -> {
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        setTextColor(ContextCompat.getColor(context, R.color.exam_result_cell_text))
                    }
                    else -> {
                        gravity = android.view.Gravity.CENTER
                        setTextColor(ContextCompat.getColor(context, R.color.exam_result_cell_text))
                    }
                }
            }
        }

        private fun formatValue(key: String, value: String?): String {
            if (value.isNullOrBlank()) return "-"
            val trimmed = value.trim()
            if (trimmed == "-") return "-"

            return when (key) {
                "physics", "chemistry", "biology", "maths", "aptitude", "drawing", "p1", "p2", "totalmarks", "maxmarks", "highestmarks", "outof" -> {
                    try {
                        val d = trimmed.toDouble()
                        if (d == d.toLong().toDouble()) d.toLong().toString() else trimmed
                    } catch (e: NumberFormatException) {
                        trimmed
                    }
                }
                "percent" -> {
                    try {
                        val d = trimmed.toDouble()
                        val suffix = if (d == d.toLong().toDouble()) d.toLong().toString() else trimmed
                        "$suffix%"
                    } catch (e: NumberFormatException) {
                        "$trimmed%"
                    }
                }
                "omrsheet" -> {
                    if (trimmed.isNotBlank()) "📄" else "-"
                }
                else -> trimmed
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Map<String, String?>>() {
            override fun areItemsTheSame(old: Map<String, String?>, new: Map<String, String?>): Boolean =
                old["date"] == new["date"] && old["examname"] == new["examname"]

            override fun areContentsTheSame(old: Map<String, String?>, new: Map<String, String?>): Boolean =
                old == new
        }
    }
}

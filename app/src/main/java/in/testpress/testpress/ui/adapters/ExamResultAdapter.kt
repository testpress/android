package `in`.testpress.testpress.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.ExamResult


class ExamResultAdapter : ListAdapter<ExamResult, ExamResultAdapter.ResultViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_result_row, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvExamName: TextView = itemView.findViewById(R.id.tv_exam_name)
        private val tvPhysics: TextView = itemView.findViewById(R.id.tv_physics)
        private val tvChemistry: TextView = itemView.findViewById(R.id.tv_chemistry)
        private val tvBiology: TextView = itemView.findViewById(R.id.tv_biology)
        private val tvMaths: TextView = itemView.findViewById(R.id.tv_maths)
        private val tvAptitude: TextView = itemView.findViewById(R.id.tv_aptitude)
        private val tvDrawing: TextView = itemView.findViewById(R.id.tv_drawing)
        private val tvP1: TextView = itemView.findViewById(R.id.tv_p1)
        private val tvP2: TextView = itemView.findViewById(R.id.tv_p2)
        private val tvTotal: TextView = itemView.findViewById(R.id.tv_total)
        private val tvMax: TextView = itemView.findViewById(R.id.tv_max)
        private val tvHighest: TextView = itemView.findViewById(R.id.tv_highest)
        private val tvPercent: TextView = itemView.findViewById(R.id.tv_percent)
        private val tvGrade: TextView = itemView.findViewById(R.id.tv_grade)
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val tvAppeared: TextView = itemView.findViewById(R.id.tv_appeared)
        private val tvOmr: TextView = itemView.findViewById(R.id.tv_omr)

        fun bind(result: ExamResult, position: Int) {
            itemView.setBackgroundColor(
                if (position % 2 == 0) {
                    itemView.context.getColor(android.R.color.white)
                } else {
                    itemView.context.getColor(R.color.exam_result_row_alt)
                }
            )

            tvDate.text = result.date.orDash()
            tvExamName.text = result.examName.orDash()
            tvPhysics.text = result.physics.formatScore()
            tvChemistry.text = result.chemistry.formatScore()
            tvBiology.text = result.biology.formatScore()
            tvMaths.text = result.maths.formatScore()
            tvAptitude.text = result.aptitude.formatScore()
            tvDrawing.text = result.drawing.formatScore()
            tvP1.text = result.p1.formatScore()
            tvP2.text = result.p2.formatScore()
            tvTotal.text = result.totalMarks.formatScore()
            tvMax.text = result.maxMarks.formatScore()
            tvHighest.text = result.highestMarks.formatScore()
            tvPercent.text = result.percent.formatPercent()
            tvGrade.text = result.grade.orDash()
            tvRank.text = result.rank.orDash()
            tvAppeared.text = result.stuAppeared.orDash()
            tvOmr.text = if (!result.omrSheet.isNullOrBlank()) "📄" else "-"
        }

        private fun String?.formatScore(): String {
            if (isNullOrBlank()) return "-"
            return try {
                val d = toDouble()
                if (d == d.toLong().toDouble()) d.toLong().toString() else this
            } catch (e: NumberFormatException) {
                this
            }
        }

        private fun String?.formatPercent(): String {
            if (isNullOrBlank()) return "-"
            return try {
                val d = toDouble()
                if (d == d.toLong().toDouble()) "${d.toLong()}%" else "$this%"
            } catch (e: NumberFormatException) {
                "$this%"
            }
        }

        private fun String?.orDash(): String = if (isNullOrBlank()) "-" else this
    }


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ExamResult>() {
            override fun areItemsTheSame(old: ExamResult, new: ExamResult): Boolean =
                old.date == new.date && old.examName == new.examName

            override fun areContentsTheSame(old: ExamResult, new: ExamResult): Boolean =
                old == new
        }
    }
}

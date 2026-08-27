package `in`.testpress.testpress.ui.utils

import android.content.Context
import `in`.testpress.testpress.R

object ExamResultTableHelper {

    val allPossibleColumns = listOf(
        "date", "examname", "rollno", "physics", "chemistry", "biology", "maths", 
        "aptitude", "drawing", "p1", "p2", "totalmarks", "maxmarks", "highestmarks", 
        "percent", "grade", "rank", "stu_appeared", "outof", "estatus", "scholarearn", 
        "remarks", "omrsheet"
    )

    fun getColumnWidth(context: Context, key: String): Int {
        val res = context.resources
        val dimenId = when (key) {
            "date" -> R.dimen.col_date
            "examname" -> R.dimen.col_exam
            "physics", "chemistry", "biology", "maths", "aptitude", "drawing", "p1", "p2" -> R.dimen.col_subject
            "totalmarks", "maxmarks", "highestmarks", "outof" -> R.dimen.col_score
            "percent" -> R.dimen.col_percent
            "grade" -> R.dimen.col_grade
            "rank" -> R.dimen.col_rank
            "stu_appeared" -> R.dimen.col_appeared
            "omrsheet" -> R.dimen.col_omr
            else -> -1
        }
        
        if (dimenId != -1) {
            return res.getDimensionPixelSize(dimenId)
        }
        
        val dpVal = when (key) {
            "rollno" -> 90
            "estatus" -> 100
            "scholarearn" -> 140
            "remarks" -> 320
            else -> 100
        }
        
        val density = res.displayMetrics.density
        return (dpVal * density).toInt()
    }

    fun getColumnDisplayName(key: String): String {
        return when (key) {
            "date" -> "Date"
            "examname" -> "Exam Name"
            "rollno" -> "Roll No"
            "physics" -> "PHY"
            "chemistry" -> "CHE"
            "biology" -> "BIO"
            "maths" -> "MAT"
            "aptitude" -> "APT"
            "drawing" -> "DRW"
            "p1" -> "P-I"
            "p2" -> "P-II"
            "totalmarks" -> "Total"
            "maxmarks" -> "Max"
            "highestmarks" -> "Highest"
            "percent" -> "%"
            "grade" -> "Grade"
            "rank" -> "Rank"
            "stu_appeared" -> "Appeared"
            "outof" -> "Out Of"
            "estatus" -> "Status"
            "scholarearn" -> "Scholarship"
            "remarks" -> "Remarks"
            "omrsheet" -> "OMR"
            else -> key.replaceFirstChar { it.uppercase() }
        }
    }

    fun determineActiveColumns(results: List<Map<String, String?>>): List<String> {
        if (results.isEmpty()) return emptyList()
        
        val activeKeys = mutableSetOf<String>()
        // Core columns should always be visible to maintain structure and match previous design
        activeKeys.addAll(listOf("date", "examname", "totalmarks", "maxmarks", "highestmarks", "percent", "grade", "rank", "stu_appeared", "omrsheet"))

        for (result in results) {
            for (key in result.keys) {
                if (key != "show_graph" && key != "type") {
                    activeKeys.add(key)
                }
            }
        }
        
        val orderedActive = mutableListOf<String>()
        if (activeKeys.contains("date")) orderedActive.add("date")
        if (activeKeys.contains("examname")) orderedActive.add("examname")
        
        for (col in allPossibleColumns) {
            if (col != "date" && col != "examname" && activeKeys.contains(col)) {
                orderedActive.add(col)
            }
        }
        
        for (key in activeKeys) {
            if (!orderedActive.contains(key) && key != "show_graph" && key != "type") {
                orderedActive.add(key)
            }
        }
        
        return orderedActive
    }
}

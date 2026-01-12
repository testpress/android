package `in`.testpress.testpress.ui.compose.dashboard

/**
 * Data models for the new dashboard implementation
 * Following clean architecture principles with immutable data classes
 */

// Alert Types
enum class AlertType {
    PAYMENT,
    CLASS,
    GENERAL
}

// Alert Model
data class Alert(
    val type: AlertType,
    val message: String
)

// Hero Banner Model
data class HeroBanner(
    val id: String,
    val imageUrl: String,
    val title: String,
    val link: String
)

// Action Types for Contextual Hero Card
enum class ActionType {
    JOIN_CLASS,
    WATCH_RECORDING,
    TAKE_TEST,
    PREPARE_TEST,
    CONTINUE_STUDY,
    EXPLORE_COURSES,
    START_TRIAL,
    UPGRADE_PROMPT
}

// Contextual Action Model
data class ContextualAction(
    val type: ActionType,
    val title: String,
    val subject: String,
    val metadata: String,
    val timeInfo: String,
    val testDuration: String? = null,
    val countdown: String? = null,
    val progress: Int? = null,
    val isLocked: Boolean = false
)

// Class Status
enum class ClassStatus {
    COMPLETED,
    LIVE,
    UPCOMING
}

// Today's Class Model
data class TodayClass(
    val id: String,
    val subject: String,
    val topic: String,
    val time: String,
    val faculty: String,
    val status: ClassStatus
)

// Assignment Status
enum class AssignmentStatus {
    OVERDUE,
    PENDING,
    SUBMITTED
}

// Assignment Model
data class Assignment(
    val id: String,
    val title: String,
    val subject: String,
    val dueTime: String,
    val status: AssignmentStatus,
    val description: String,
    val progress: Int? = null
)

// Test Type
enum class TestType {
    MOCK,
    CHAPTER,
    PRACTICE
}

// Test Model
data class Test(
    val id: String,
    val title: String,
    val time: String,
    val duration: String,
    val type: TestType,
    val isImportant: Boolean = false
)

// Progress Data Model
data class ProgressData(
    val weeklyCompletion: Int,
    val averageScore: Int,
    val studyStreak: Int,
    val weeklyGoal: Int,
    val completedHours: Int
)

// Week Day Activity Model
data class WeekDayActivity(
    val date: String,
    val dayLabel: String,
    val hasActivity: Boolean,
    val minutes: Int
)

// Latest Activity Model
data class LatestActivity(
    val title: String,
    val timeAgo: String
)

// Study Momentum Model
data class StudyMomentum(
    val weekDays: List<WeekDayActivity>,
    val weeklyHours: Double,
    val currentStreak: Int,
    val latestActivity: LatestActivity
)

// Promotional Banner Model
data class PromotionalBanner(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val tag: String? = null,
    val bgColor: String,
    val textColor: String
)

// Shortcut Icon Type
enum class ShortcutIcon {
    VIDEO,
    PRACTICE,
    TESTS,
    NOTES,
    DOUBTS,
    SCHEDULE
}

// Quick Access Shortcut Model
data class QuickAccessShortcut(
    val id: String,
    val icon: ShortcutIcon,
    val label: String
)

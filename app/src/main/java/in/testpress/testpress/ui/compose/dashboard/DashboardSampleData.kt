package `in`.testpress.testpress.ui.compose.dashboard

/**
 * Sample data for testing the new dashboard
 * In production, this data would come from API responses
 */

object DashboardSampleData {
    
    // Sample Alerts
    val sampleAlerts = listOf(
        Alert(
            type = AlertType.PAYMENT,
            message = "Your subscription expires in 5 days. Renew now to continue uninterrupted access."
        )
    )
    
    // Sample Hero Banners
    val sampleHeroBanners = listOf(
        HeroBanner(
            id = "1",
            imageUrl = "https://images.unsplash.com/photo-1577036421869-7c8d388d2123?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdHVkZW50JTIwc3R1ZHlpbmclMjBzdWNjZXNzfGVufDF8fHx8MTc2NjY1NjAyOHww&ixlib=rb-4.1.0&q=80&w=1080",
            title = "JEE 2026 Crash Course",
            link = "https://example.com/jee-crash-course"
        ),
        HeroBanner(
            id = "2",
            imageUrl = "https://images.unsplash.com/photo-1759922378123-a1f4f1e39bae?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjbGFzc3Jvb20lMjBsZWFybmluZyUyMGVkdWNhdGlvbnxlbnwxfHx8fDE3NjY3MzEzNzJ8MA&ixlib=rb-4.1.0&q=80&w=1080",
            title = "Expert Faculty Announcement",
            link = "https://example.com/new-faculty"
        ),
        HeroBanner(
            id = "3",
            imageUrl = "https://images.unsplash.com/photo-1762330917056-e69b34329ddf?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvbmxpbmUlMjBjbGFzcyUyMGRpZ2l0YWwlMjBsZWFybmluZ3xlbnwxfHx8fDE3NjY3NDgwODJ8MA&ixlib=rb-4.1.0&q=80&w=1080",
            title = "Online Test Series Live",
            link = "https://example.com/test-series"
        )
    )
    
    // Sample Enrolled Student Actions
    val enrolledActions = listOf(
        ContextualAction(
            type = ActionType.JOIN_CLASS,
            title = "Organic Chemistry - Reaction Mechanisms",
            subject = "Chemistry",
            metadata = "Dr. Rajesh Kumar",
            timeInfo = "3:00 PM - 5:00 PM"
        ),
        ContextualAction(
            type = ActionType.WATCH_RECORDING,
            title = "Newton's Laws & Applications",
            subject = "Physics",
            metadata = "Prof. Anita Sharma",
            timeInfo = "Recorded at 10:00 AM"
        ),
        ContextualAction(
            type = ActionType.TAKE_TEST,
            title = "Weekly Mock Test - Mathematics",
            subject = "JEE Advanced Pattern",
            metadata = "3 hours",
            timeInfo = "5 hours left to attempt",
            testDuration = "180 minutes"
        ),
        ContextualAction(
            type = ActionType.PREPARE_TEST,
            title = "Full-Length Mock Test #12",
            subject = "JEE Main 2026",
            metadata = "All subjects covered",
            countdown = "Starts in 18 hours",
            testDuration = "3 hours",
            timeInfo = ""
        ),
        ContextualAction(
            type = ActionType.CONTINUE_STUDY,
            title = "Calculus II - Integration Techniques",
            subject = "Mathematics",
            metadata = "Chapter 8 - Advanced Methods",
            timeInfo = "Last accessed 2 hours ago",
            progress = 67
        )
    )
    
    // Sample Trial User Actions
    val trialActions = listOf(
        ContextualAction(
            type = ActionType.EXPLORE_COURSES,
            title = "Discover Our Expert-Led Courses",
            subject = "JEE, NEET & Foundation Programs",
            metadata = "Browse 500+ hours of content",
            timeInfo = "Free demo classes available"
        ),
        ContextualAction(
            type = ActionType.START_TRIAL,
            title = "Start Your 7-Day Free Trial",
            subject = "Full Access to All Features",
            metadata = "No credit card required",
            timeInfo = "Cancel anytime"
        ),
        ContextualAction(
            type = ActionType.UPGRADE_PROMPT,
            title = "Unlock Live Classes & Mock Tests",
            subject = "Premium Features",
            metadata = "Join 50,000+ successful students",
            timeInfo = "Limited time offer",
            isLocked = true
        )
    )
    
    // Sample Today's Classes
    val todayClasses = listOf(
        TodayClass(
            id = "1",
            subject = "Physics - Thermodynamics",
            topic = "Laws of Thermodynamics & Heat Engines",
            time = "10:00 AM - 12:00 PM",
            faculty = "Prof. Anita Sharma",
            status = ClassStatus.COMPLETED
        ),
        TodayClass(
            id = "2",
            subject = "Chemistry - Organic Chemistry",
            topic = "Reaction Mechanisms",
            time = "3:00 PM - 5:00 PM",
            faculty = "Dr. Rajesh Kumar",
            status = ClassStatus.LIVE
        ),
        TodayClass(
            id = "3",
            subject = "Mathematics - Calculus II",
            topic = "Integration Techniques",
            time = "5:30 PM - 7:30 PM",
            faculty = "Dr. Vikram Singh",
            status = ClassStatus.UPCOMING
        ),
        TodayClass(
            id = "4",
            subject = "English - Communication Skills",
            topic = "Essay Writing & Comprehension",
            time = "8:00 PM - 9:00 PM",
            faculty = "Ms. Priya Verma",
            status = ClassStatus.UPCOMING
        )
    )
    
    // Sample Assignments
    val assignments = listOf(
        Assignment(
            id = "1",
            title = "Problem Set - Differentiation",
            subject = "Mathematics",
            dueTime = "11:59 PM",
            status = AssignmentStatus.OVERDUE,
            description = "Chapter 5 - Problems 1-20"
        ),
        Assignment(
            id = "2",
            title = "Thermodynamics Numericals",
            subject = "Physics",
            dueTime = "6:00 PM",
            status = AssignmentStatus.PENDING,
            description = "Heat transfer & entropy problems",
            progress = 45
        ),
        Assignment(
            id = "3",
            title = "Organic Reaction Worksheet",
            subject = "Chemistry",
            dueTime = "9:00 PM",
            status = AssignmentStatus.PENDING,
            description = "25 reactions with mechanisms",
            progress = 80
        ),
        Assignment(
            id = "4",
            title = "Comprehension Exercises",
            subject = "English",
            dueTime = "11:00 PM",
            status = AssignmentStatus.SUBMITTED,
            description = "Reading passages & analysis"
        )
    )
    
    // Sample Tests
    val tests = listOf(
        Test(
            id = "1",
            title = "Weekly Mock Test - Physics",
            time = "Tomorrow, 9:00 AM",
            duration = "3 hours",
            type = TestType.MOCK,
            isImportant = true
        ),
        Test(
            id = "2",
            title = "Chapter Test - Organic Chemistry",
            time = "Jan 5, 2:00 PM",
            duration = "1.5 hours",
            type = TestType.CHAPTER
        ),
        Test(
            id = "3",
            title = "Calculus Practice Test",
            time = "Jan 6, 10:00 AM",
            duration = "2 hours",
            type = TestType.PRACTICE
        )
    )
    
    // Sample Progress Data
    val progressData = ProgressData(
        weeklyCompletion = 68,
        averageScore = 82,
        studyStreak = 12,
        weeklyGoal = 25,
        completedHours = 17
    )
    
    // Sample Study Momentum
    val studyMomentum = StudyMomentum(
        weekDays = listOf(
            WeekDayActivity(date = "2026-01-01", dayLabel = "M", hasActivity = true, minutes = 145), // High (Dark)
            WeekDayActivity(date = "2026-01-02", dayLabel = "T", hasActivity = true, minutes = 120), // High (Dark)
            WeekDayActivity(date = "2026-01-03", dayLabel = "W", hasActivity = true, minutes = 20),  // Low  (Light)
            WeekDayActivity(date = "2026-01-04", dayLabel = "T", hasActivity = true, minutes = 160), // High (Dark)
            WeekDayActivity(date = "2026-01-05", dayLabel = "F", hasActivity = false, minutes = 0),  // Empty
            WeekDayActivity(date = "2026-01-06", dayLabel = "S", hasActivity = true, minutes = 180), // High (Dark)
            WeekDayActivity(date = "2026-01-07", dayLabel = "S", hasActivity = true, minutes = 25)   // Low  (Light)
        ),
        weeklyHours = 17.5,
        currentStreak = 5,
        latestActivity = LatestActivity(
            title = "Calculus - Integration",
            timeAgo = "2h ago"
        )
    )
    
    // Sample Promotional Banners
    val promotionalBanners = listOf(
        PromotionalBanner(
            id = "1",
            title = "New Test Series Available",
            description = "JEE Advanced 2026 - Full-length mock tests now live",
            tag = "NEW",
            bgColor = "#EFF6FF",
            textColor = "#1E3A8A"
        ),
        PromotionalBanner(
            id = "2",
            title = "Results Announced",
            description = "Congratulations to our top performers in the December mock test",
            tag = "RESULTS",
            bgColor = "#F0FDF4",
            textColor = "#14532D"
        ),
        PromotionalBanner(
            id = "3",
            title = "Special Doubt Session",
            description = "Live doubt clearing session this Sunday at 4 PM",
            bgColor = "#FAF5FF",
            textColor = "#581C87"
        )
    )
    
    // Sample Quick Access Shortcuts
    val quickAccessShortcuts = listOf(
        QuickAccessShortcut(id = "1", icon = ShortcutIcon.VIDEO, label = "Recordings"),
        QuickAccessShortcut(id = "2", icon = ShortcutIcon.PRACTICE, label = "Practice"),
        QuickAccessShortcut(id = "3", icon = ShortcutIcon.TESTS, label = "Tests"),
        QuickAccessShortcut(id = "4", icon = ShortcutIcon.NOTES, label = "Notes"),
        QuickAccessShortcut(id = "5", icon = ShortcutIcon.DOUBTS, label = "Ask Doubt"),
        QuickAccessShortcut(id = "6", icon = ShortcutIcon.SCHEDULE, label = "Schedule")
    )
}

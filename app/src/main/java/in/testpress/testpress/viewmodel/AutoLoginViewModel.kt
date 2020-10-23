package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.TestPressSessionRepository
import androidx.lifecycle.ViewModel

class AutoLoginViewModel: ViewModel() {

    private val testPressSessionRepository = TestPressSessionRepository()

    var result = testPressSessionRepository.result

    fun initializeTestPressSession(activity: RegisterActivity,instituteSettings: InstituteSettings, username: String, password: String) {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
                .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
                .setCoursesFrontend(instituteSettings.showGameFrontend)
                .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
                .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
                .setAccessCodeEnabled(false)

        testPressSessionRepository.initialize(activity,settings,username, password)
    }
}

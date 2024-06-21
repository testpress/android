package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.TestPressSessionRepository
import android.content.Context
import androidx.lifecycle.ViewModel

class AutoLoginViewModel: ViewModel() {

    private val testPressSessionRepository = TestPressSessionRepository()

    var result = testPressSessionRepository.result

    fun initializeTestPressSession(context: Context,instituteSettings: InstituteSettings, username: String, password: String, testPressService: TestpressService) {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
                .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
                .setCoursesFrontend(instituteSettings.showGameFrontend)
                .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
                .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
                .setAndroidSentryDns(instituteSettings.androidSentryDns)
                .setAccessCodeEnabled(false)

        testPressSessionRepository.initialize(context,settings,username, password, testPressService)
    }
}

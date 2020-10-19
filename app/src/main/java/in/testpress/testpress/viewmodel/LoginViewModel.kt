package `in`.testpress.testpress.viewmodel

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.LoginRepository
import android.accounts.Account
import android.accounts.AccountManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {

    var result = MutableLiveData<Resource<Boolean>>()

    val loginRepository = LoginRepository()

    fun initializeTestPressSession(activity: RegisterActivity,instituteSettings: InstituteSettings, username: String, password: String) {
        val settings = `in`.testpress.models.InstituteSettings(instituteSettings.baseUrl)
                .setBookmarksEnabled(instituteSettings.bookmarksEnabled)
                .setCoursesFrontend(instituteSettings.showGameFrontend)
                .setCoursesGamificationEnabled(instituteSettings.coursesEnableGamification)
                .setCommentsVotingEnabled(instituteSettings.commentsVotingEnabled)
                .setAccessCodeEnabled(false)

        TestpressSdk.initialize(activity, settings, username, password,
                TestpressSdk.Provider.TESTPRESS,
                object : TestpressCallback<TestpressSession>() {
                    override fun onSuccess(response: TestpressSession) {
                        setAuthToken(activity, response, username, password)
                        loginRepository.updateDevice(activity)
                        result.postValue(Resource.success(true))
                    }
                    override fun onException(exception: TestpressException) {
                        result.postValue(Resource.error(exception, false))
                    }
                })
    }

    private fun setAuthToken(activity: RegisterActivity, response: TestpressSession, username: String, password: String) {
        val authToken = response.token
        activity.testPressService.setAuthToken(authToken)
        val accountManager = AccountManager.get(activity)
        val account = Account(username, BuildConfig.APPLICATION_ID)
        accountManager.addAccountExplicitly(account,password, null)
        accountManager.setAuthToken(account, BuildConfig.APPLICATION_ID, authToken)
    }
}

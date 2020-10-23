package `in`.testpress.testpress.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.models.InstituteSettings
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.models.Device
import `in`.testpress.testpress.util.GCMPreference
import `in`.testpress.testpress.util.SafeAsyncTask
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.lifecycle.MutableLiveData

class TestPressSessionRepository {

    val result = MutableLiveData<Resource<Boolean>>()

    fun initialize(activity: RegisterActivity, settings: InstituteSettings, username: String, password: String) {
        TestpressSdk.initialize(activity, settings, username, password,
                TestpressSdk.Provider.TESTPRESS,
                object : TestpressCallback<TestpressSession>() {
                    override fun onSuccess(response: TestpressSession) {
                        setAuthToken(activity, response, username, password)
                        registerDevice(activity)
                        result.value = Resource.success(true)
                    }
                    override fun onException(exception: TestpressException) {
                        result.value = Resource.error(exception, false)
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

    private fun registerDevice(activity: RegisterActivity) {

        val sharedPreferences: SharedPreferences = activity.getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply()

        val registrationId: String = GCMPreference.getRegistrationId(activity.applicationContext)
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)

        object : SafeAsyncTask<Device>() {
            override fun call(): Device {
               return activity.testPressService.registerDevice(registrationId, deviceId)
            }

            override fun onException(e: Exception) {
                sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply()
            }

            override fun onSuccess(device: Device) {
                sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply()
            }
        }.execute()
    }
}

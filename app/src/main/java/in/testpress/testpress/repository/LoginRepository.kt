package `in`.testpress.testpress.repository

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.models.Device
import `in`.testpress.testpress.util.GCMPreference
import `in`.testpress.testpress.util.SafeAsyncTask
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.facebook.FacebookSdk

class LoginRepository {

    fun updateDevice(activity: RegisterActivity) {
        val sharedPreferences: SharedPreferences = activity.getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply()
        object : SafeAsyncTask<Device>() {
            override fun call(): Device {
                val token: String = GCMPreference.getRegistrationId(FacebookSdk.getApplicationContext())
                return activity.testPressService.register(token, Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID))
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

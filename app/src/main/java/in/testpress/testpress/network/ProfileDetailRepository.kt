package `in`.testpress.testpress.network

import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.ProfileDetails
import `in`.testpress.testpress.util.SafeAsyncTask
import android.app.Activity
import androidx.lifecycle.MutableLiveData

class ProfileDetailRepository(val serviceProvider: TestpressServiceProvider, val activity: Activity) {
    var profileDetails = MutableLiveData<ProfileDetails?>()
    fun get() {
        object : SafeAsyncTask<ProfileDetails>() {
            override fun call(): ProfileDetails {
                return serviceProvider.getService(activity).profileDetails
            }

            override fun onSuccess(resource: ProfileDetails) {
                profileDetails.postValue(resource)
            }

            override fun onException(exception: Exception) {
                profileDetails.postValue(null)
            }
        }.execute()
    }
}
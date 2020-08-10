package `in`.testpress.testpress.ui

import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.ProfileDetails
import `in`.testpress.testpress.util.SafeAsyncTask
import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileDetailsViewModel : ViewModel() {
    private var profileDetails = MutableLiveData<ProfileDetails?>()
    fun get(serviceProvider: TestpressServiceProvider, activity: Activity): LiveData<ProfileDetails?> {
        object : SafeAsyncTask<ProfileDetails>() {
            override fun call(): ProfileDetails {
                return serviceProvider.getService(activity).profileDetails
            }

            override fun onException(e: Exception) {
                this@ProfileDetailsViewModel.profileDetails.value = null
            }

            override fun onSuccess(profileDetails: ProfileDetails) {
                this@ProfileDetailsViewModel.profileDetails.value = profileDetails
            }
        }.execute()
        return profileDetails
    }
}

package `in`.testpress.testpress.ui

import `in`.testpress.testpress.models.ProfileDetails
import `in`.testpress.testpress.network.ProfileDetailRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileDetailsViewModel(private val profileDetailRepository: ProfileDetailRepository) : ViewModel() {

    fun get(): MutableLiveData<ProfileDetails?> {
        return profileDetailRepository.get()
    }
}

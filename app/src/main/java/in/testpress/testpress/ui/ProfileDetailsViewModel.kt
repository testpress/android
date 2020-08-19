package `in`.testpress.testpress.ui

import `in`.testpress.course.network.Resource
import `in`.testpress.testpress.models.ProfileDetails
import `in`.testpress.testpress.network.ProfileDetailRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileDetailsViewModel(private val profileDetailRepository: ProfileDetailRepository) : ViewModel() {
    var profileDetails = MutableLiveData<Resource<ProfileDetails?>>()
    fun get(): LiveData<Resource<ProfileDetails?>> {
        return profileDetailRepository.get()
    }
}

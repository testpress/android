package `in`.testpress.testpress.util

import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.network.ProfileDetailRepository
import `in`.testpress.testpress.ui.ProfileDetailsViewModel
import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ProfileDetailViewModelFactory(private val activity: Activity, val serviceProvider: TestpressServiceProvider): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileDetailsViewModel::class.java)) {
            return ProfileDetailsViewModel(ProfileDetailRepository(serviceProvider, activity)) as T
        }

        throw IllegalArgumentException("Wrong Parameters")
    }
}
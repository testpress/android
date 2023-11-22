package `in`.testpress.testpress.viewmodel

import `in`.testpress.network.Resource
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.repository.InstituteRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel(private val repository: InstituteRepository):ViewModel() {

    fun getInstituteSettings(): LiveData<Resource<InstituteSettings>> {
        return repository.getInstituteSettings()
    }
}
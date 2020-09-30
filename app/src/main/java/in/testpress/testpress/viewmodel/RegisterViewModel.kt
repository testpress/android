package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.repository.RegisterRepository
import androidx.lifecycle.ViewModel

class RegisterViewModel(private val repository: RegisterRepository) : ViewModel() {

    val result = repository.result

    fun register(userDetails: HashMap<String, String>) {
        repository.register(userDetails)
    }
}

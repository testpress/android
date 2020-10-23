package `in`.testpress.testpress.util.fakes

import androidx.lifecycle.ViewModel

class FakeRegisterViewModel: ViewModel() {

    private val repository = FakeRegisterRepository()

    fun register() {
        repository.register(hashMapOf())
    }
}

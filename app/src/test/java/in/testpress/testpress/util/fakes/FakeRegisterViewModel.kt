package `in`.testpress.testpress.util.fakes

import androidx.lifecycle.ViewModel

class FakeViewModel: ViewModel() {

    private val repository = FakeRepository()

    fun register() {
        repository.register(hashMapOf())
    }

    fun initializeTestPressSession() {
        repository
    }
}

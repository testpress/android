package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.util.fakes.FakeRepository
import `in`.testpress.testpress.util.fakes.FakeViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class RegisterViewModelTest {

    private val repository = mock(FakeRepository::class.java)

    private val viewModel = FakeViewModel()

    @Test
    fun registerShouldCallRepository() {
        viewModel.register()
        verify(repository).register(hashMapOf())
    }
}

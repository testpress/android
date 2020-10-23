package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.util.fakes.FakeRegisterRepository
import `in`.testpress.testpress.util.fakes.FakeRegisterViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class RegisterViewModelTest {

    private val repository = mock(FakeRegisterRepository::class.java)

    private val viewModel = FakeRegisterViewModel()

    @Test
    fun registerShouldCallRepository() {
        viewModel.register()
        verify(repository).register(hashMapOf())
    }
}

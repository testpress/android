package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.repository.RegisterRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(JUnit4::class)
class RegisterViewModelTest {

    private val repository = mock(RegisterRepository::class.java)

    private val viewModel = RegisterViewModel(repository)

    @Test
    fun registerShouldCallRepository() {
        viewModel.register(hashMapOf())
        verify(repository).register(hashMapOf())
    }
}

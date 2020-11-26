package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.util.fakes.FakeRepository
import `in`.testpress.testpress.util.fakes.FakeViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@RunWith(JUnit4::class)
class AutoLoginViewModelTest {
    private val repository = Mockito.mock(FakeRepository::class.java)

    private val viewModel = FakeViewModel()

    @Test
    fun initializeTestPressSessionShouldCallRepository() {
        viewModel.initializeTestPressSession()
        Mockito.verify(repository).initialize()
    }
}
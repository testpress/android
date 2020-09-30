package `in`.testpress.testpress.util

import `in`.testpress.enums.Status
import `in`.testpress.testpress.util.fakes.FakeRegisterRepository
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegisterRepositoryTest  {

    private val repository = FakeRegisterRepository()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun successNetworkCallShouldReturnSuccessResponse() {
        runBlocking {
            repository.apiStatus = Status.SUCCESS
            repository.register(userDetails = hashMapOf())
            assertEquals(repository.isRegisterSuccessful, true)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        runBlocking {
            repository.apiStatus = Status.ERROR
            repository.register(hashMapOf())
            assertEquals(repository.isRegisterSuccessful, false)
            assertTrue(repository.exception != null)
        }
    }
}

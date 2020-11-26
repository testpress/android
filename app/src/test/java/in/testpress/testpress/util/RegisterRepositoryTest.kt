package `in`.testpress.testpress.util

import `in`.testpress.enums.Status
import `in`.testpress.testpress.util.fakes.FakeRepository
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RegisterRepositoryTest  {

    private val repository = FakeRepository()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun successNetworkCallShouldReturnSuccessResponse() {
        runBlocking {
            repository.apiStatus = Status.SUCCESS
            repository.register(userDetails = hashMapOf())
            assertEquals(repository.result.status, Status.SUCCESS)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        runBlocking {
            repository.apiStatus = Status.ERROR
            repository.register(hashMapOf())
            assertEquals(repository.result.status, Status.ERROR)
        }
    }

    @Test
    fun initializeNetworkCallShouldReturnSuccess() {
        runBlocking {
            repository.apiStatus = Status.SUCCESS
            repository.initialize()
            assertEquals(repository.initializeResponse.status, Status.SUCCESS)
        }
    }

    @Test
    fun initializeNetworkFailureShouldReturnException() {
        runBlocking {
            repository.apiStatus = Status.ERROR
            repository.register(hashMapOf())
            assertEquals(repository.result.status, Status.ERROR)
        }
    }
}

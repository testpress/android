package `in`.testpress.testpress.authenticator

import android.accounts.AccountManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class LogoutServiceTest {

    private val mockContext = mock(Context::class.java)
    private val mockAccountManager = mock(AccountManager::class.java)

    @Test
    fun testLogoutServiceProperties() {
        val logoutService = LogoutService(mockContext, mockAccountManager)
        assertEquals(mockContext, logoutService.context)
        assertEquals(mockAccountManager, logoutService.accountManager)
    }

    @Test
    fun testLogoutTask_callbackExecutedAtMostOnceEvenIfItThrows() {
        var callbackCount = 0
        val throwingRunnable = Runnable {
            callbackCount++
            throw RuntimeException("Simulated navigation failure")
        }

        val logoutTaskClass = Class.forName("in.testpress.testpress.authenticator.LogoutService\$LogoutTask")
        val constructor = logoutTaskClass.getDeclaredConstructor(Context::class.java, Runnable::class.java)
        constructor.isAccessible = true
        val taskInstance = constructor.newInstance(mockContext, throwingRunnable)

        val onSuccessMethod = logoutTaskClass.getDeclaredMethod("onSuccess", java.lang.Boolean::class.java)
        onSuccessMethod.isAccessible = true

        val onExceptionMethod = logoutTaskClass.getDeclaredMethod("onException", java.lang.Exception::class.java)
        onExceptionMethod.isAccessible = true

        try {
            onSuccessMethod.invoke(taskInstance, true)
        } catch (e: Exception) {
            // SafeAsyncTask catches exception from onSuccess and routes it to onException
        }

        // SafeAsyncTask routes to onException
        onExceptionMethod.invoke(taskInstance, RuntimeException("Exception from onSuccess"))

        assertEquals(1, callbackCount)
    }
}

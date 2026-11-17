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
}

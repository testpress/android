package `in`.testpress.testpress.util

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import `in`.testpress.testpress.BuildConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class CommonUtilsTest {

    private val mockContext = mock(Context::class.java)
    private val mockAccountManager = mock(AccountManager::class.java)

    @Test
    fun isUserAuthenticated_returnsFalse_whenAccountManagerIsNull() {
        `when`(mockContext.getSystemService(Context.ACCOUNT_SERVICE)).thenReturn(null)
        val result = CommonUtils.isUserAuthenticated(mockContext)
        assertFalse(result)
    }

    @Test
    fun isUserAuthenticated_returnsFalse_whenAccountsArrayIsEmpty() {
        `when`(mockContext.getSystemService(Context.ACCOUNT_SERVICE)).thenReturn(mockAccountManager)
        `when`(mockAccountManager.getAccountsByType(BuildConfig.APPLICATION_ID)).thenReturn(emptyArray())
        val result = CommonUtils.isUserAuthenticated(mockContext)
        assertFalse(result)
    }

    @Test
    fun isUserAuthenticated_returnsTrue_whenAccountsArrayIsNotEmpty() {
        `when`(mockContext.getSystemService(Context.ACCOUNT_SERVICE)).thenReturn(mockAccountManager)
        val mockAccount = Account("testuser", BuildConfig.APPLICATION_ID)
        `when`(mockAccountManager.getAccountsByType(BuildConfig.APPLICATION_ID)).thenReturn(arrayOf(mockAccount))
        val result = CommonUtils.isUserAuthenticated(mockContext)
        assertTrue(result)
    }
}

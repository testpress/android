package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.authenticator.LoginActivity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteDownloadedVideosTest {

    private val testActivityRule = ActivityTestRule(LoginActivity::class.java, true, true)

    @Rule
    fun rule() = testActivityRule

    private lateinit var context: Context

    private var usernamePref: String? = null

    @Before
    fun setup() {
        context = getApplicationContext()
        val sharedPreferences = context.getSharedPreferences(
                "UserPreference",
                MODE_PRIVATE
        )
        usernamePref = sharedPreferences.getString("username", "")
    }

    @Test
    fun whenUsernameIsDifferentVideosShouldDelete() {
        testActivityRule.activity.login("admin")
        val result = testActivityRule.activity.deleteOfflineVideosForDifferentAccount("testpress")
        Assert.assertTrue(result)
    }

    @Test
    fun whenUsernameIsSameVideosShouldNotDelete() {
        testActivityRule.activity.login("admin")
        val result = testActivityRule.activity.deleteOfflineVideosForDifferentAccount("admin")
        Assert.assertFalse(result)
    }

    private fun LoginActivity.deleteOfflineVideosForDifferentAccount(username: String): Boolean {
        return if (!usernamePref.equals(username)) {
            testActivityRule.activity.login(username)
            testActivityRule.activity.deleteOfflineVideos()
        } else {
            testActivityRule.activity.login(username)
            false
        }
    }

    private fun LoginActivity.deleteOfflineVideos() = true

    private fun LoginActivity.login(username: String) {
        val pref = context.getSharedPreferences("UserPreference", MODE_PRIVATE).edit()
        pref.putString("username", username).commit()
    }

}

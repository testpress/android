package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.ui.fragments.AutoLoginFragment
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class AutoLoginFragmentTest {

    private val testActivityRule = ActivityTestRule(RegisterActivity::class.java, true, true)

    private val testMainActivityRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Rule
    fun rule() = testActivityRule

    private lateinit var autoLoginFragment: AutoLoginFragment

    @Before
    fun setUp() {
        autoLoginFragment = AutoLoginFragment()
    }

    @Test
    fun whenNavigateToAutoLoginFragmentTheViewShouldNotBeNull() {
        testActivityRule.activity.navigateToAutoLoginFragment()
        Assert.assertNotNull(testActivityRule.activity.findViewById(R.id.container))
    }

    @Test
    fun whenRegisterSuccessfulItShouldOpenMainActivity() {
        autoLoginFragment.activity = testActivityRule.activity
        autoLoginFragment.performRegister()
        Assert.assertNotNull(testMainActivityRule.activity)
    }

    private fun AutoLoginFragment.performRegister() {
        autoLoginFragment.gotoToMainActivity()
    }

    private fun AutoLoginFragment.gotoToMainActivity() = testMainActivityRule.launchActivity(Intent())

}


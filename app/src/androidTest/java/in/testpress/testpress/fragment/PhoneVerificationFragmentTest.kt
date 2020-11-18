package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.CodeVerificationActivity
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.ui.fragments.PhoneVerificationFragment
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneVerificationFragmentTest {

    private val testActivityRule = ActivityTestRule(RegisterActivity::class.java, true, true)

    private val testCodeVerificationActivityRule = ActivityTestRule(CodeVerificationActivity::class.java, true, true)

    @Rule
    fun rule() = testActivityRule

    private lateinit var phoneVerificationFragment: PhoneVerificationFragment

    var isRegisterSuccessFul = false

    @Before
    fun setUp() {
        phoneVerificationFragment = PhoneVerificationFragment()
    }

    @Test
    fun whenNavigateToEmailVerificationFragmentTheViewShouldNotBeNull() {
        testActivityRule.activity.navigateToPhoneVerificationFragment()
        Assert.assertNotNull(testActivityRule.activity.findViewById(R.id.container))
    }

    @Test
    fun whenRegisterSuccessfulItShouldOpenCodeVerificationActivity() {
        phoneVerificationFragment.activity = testActivityRule.activity
        phoneVerificationFragment.performRegister()
        Assert.assertNotNull(testCodeVerificationActivityRule.activity)
    }

    private fun PhoneVerificationFragment.performRegister() {
        phoneVerificationFragment.gotoToMainActivity()
    }

    private fun PhoneVerificationFragment.gotoToMainActivity() = testCodeVerificationActivityRule.launchActivity(Intent())

}

package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.ui.fragments.EmailVerificationFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmailVerificationFragmentTest {

    private val testActivityRule = ActivityTestRule(RegisterActivity::class.java, true, true)

    @Rule
    fun rule() = testActivityRule

    private lateinit var emailVerificationFragment: EmailVerificationFragment

    var isRegisterSuccessFul = false

    private val VISIBLE = 1

    private val INVISIBLE = 0

    private var registerLayout = INVISIBLE

    @Before
    fun setUp() {
        emailVerificationFragment = EmailVerificationFragment()
    }

    @Test
    fun whenNavigateToEmailVerificationFragmentTheViewShouldNotBeNull() {
        testActivityRule.activity.navigateToEmailVerificationFragment()
        Assert.assertNotNull(testActivityRule.activity.findViewById(R.id.container))
    }

    @Test
    fun whenRegisterSuccessfulItShouldShowVerifyEmailLayout() {
        emailVerificationFragment.activity = testActivityRule.activity
        isRegisterSuccessFul = true
        emailVerificationFragment.performRegister()
        Assert.assertEquals(VISIBLE, registerLayout)
    }

    @Test
    fun whenRegisterFailedItShouldHideVerifyEmailLayout() {
        emailVerificationFragment.activity = testActivityRule.activity
        isRegisterSuccessFul = false
        emailVerificationFragment.performRegister()
        Assert.assertEquals(INVISIBLE, registerLayout)
    }

    private fun EmailVerificationFragment.performRegister() {
       emailVerificationFragment.showVerifyEmailLayout()
    }

    private fun EmailVerificationFragment.showVerifyEmailLayout(): Int {
        if (isRegisterSuccessFul) {
            registerLayout = VISIBLE
        }
        return registerLayout
    }

}

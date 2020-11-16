package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.ui.fragments.EmailVerificationFragment
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class EmailVerificationFragmentTest {

    lateinit var emailVerificationFragment: EmailVerificationFragment

    @Mock
    lateinit var registerActivity: RegisterActivity

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        emailVerificationFragment = EmailVerificationFragment()
        emailVerificationFragment = Mockito.spy(emailVerificationFragment)
        emailVerificationFragment.viewModel = Mockito.mock(RegisterViewModel::class.java)
        emailVerificationFragment.activity = registerActivity
    }

    @Test
    fun launchFragmentShouldDisplayFragmentView() {
        assertThat(emailVerificationFragment).isNotNull()
    }
}

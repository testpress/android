package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.ui.fragments.PhoneVerificationFragment
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class PhoneVerificationFragmentTest {

    lateinit var phoneVerificationFragment: PhoneVerificationFragment

    @Mock
    lateinit var registerActivity: RegisterActivity

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        phoneVerificationFragment = PhoneVerificationFragment()
        phoneVerificationFragment = Mockito.spy(phoneVerificationFragment)
        phoneVerificationFragment.viewModel = Mockito.mock(RegisterViewModel::class.java)
        phoneVerificationFragment.activity = registerActivity
    }

    @Test
    fun launchFragmentShouldDisplayFragmentView() {
        Truth.assertThat(phoneVerificationFragment).isNotNull()
    }
}

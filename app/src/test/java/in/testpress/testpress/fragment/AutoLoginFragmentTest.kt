package `in`.testpress.testpress.fragment

import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.ui.fragments.AutoLoginFragment
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

open class AutoLoginFragmentTest {

    lateinit var autoLoginFragment: AutoLoginFragment

    @Mock
    lateinit var registerActivity: RegisterActivity

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        autoLoginFragment = AutoLoginFragment()
        autoLoginFragment = Mockito.spy(autoLoginFragment)
        autoLoginFragment.viewModel = Mockito.mock(RegisterViewModel::class.java)
        autoLoginFragment.activity = registerActivity
    }

    @Test
    fun launchFragmentShouldDisplayFragmentView() {
        Truth.assertThat(autoLoginFragment).isNotNull()
    }
}

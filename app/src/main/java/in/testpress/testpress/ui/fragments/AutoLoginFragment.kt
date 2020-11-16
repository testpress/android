package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.authenticator.LoginActivity
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.util.ProgressUtil
import `in`.testpress.testpress.viewmodel.AutoLoginViewModel
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.register_activity.*

open class AutoLoginFragment: RegistrationBaseFragment() {

    private lateinit var instituteSettings: InstituteSettings

    private val autoLoginViewModel by lazy {
        ViewModelProvider(this).get(AutoLoginViewModel::class.java)
    }

    override fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                register()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> autoLogin()
                Status.ERROR -> onRegisterException(it.exception)
            }
        })

        autoLoginViewModel.result.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> navigateToMainActivity()
                Status.ERROR -> navigateToLoginActivity()
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun autoLogin() {
        instituteSettings = viewModel.instituteSettings
        autoLoginViewModel.initializeTestPressSession(activity,instituteSettings,
                editTextUsername.text.toString(), editTextPassword.text.toString(), activity.testPressService)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }
}

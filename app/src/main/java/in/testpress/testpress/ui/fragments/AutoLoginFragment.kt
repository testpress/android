package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.authenticator.LoginActivity
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.util.ProgressUtil
import `in`.testpress.testpress.viewmodel.LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.register_activity.*

class AutoLoginFragment: RegistrationBaseFragment() {

    private lateinit var instituteSettings: InstituteSettings

    private val loginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViewModelObservers()
        setTextWatchers()
    }

    private fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                register()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> autoLogin()
                Status.ERROR -> setPostDetailsException(it.exception)
            }
        })

        loginViewModel.result.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> navigateToMainActivity()
                Status.ERROR -> navigateToLoginActivity()
            }
        })
    }

    private fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun autoLogin() {
        instituteSettings = viewModel.instituteSettings
        loginViewModel.initializeTestPressSession(activity,instituteSettings,
                editTextUsername.text.toString(), editTextPassword.text.toString())
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }
}

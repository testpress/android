package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivity
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.util.ProgressUtil
import `in`.testpress.testpress.viewmodel.AutoLoginViewModel
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog

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
                else -> {}
            }
        })

        autoLoginViewModel.result.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> navigateToMainActivity()
                Status.ERROR -> navigateToLoginActivity()
                else -> {}
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun autoLogin() {
        ProgressUtil.progressDialog.dismiss()
        instituteSettings = viewModel.instituteSettings
        if (instituteSettings.isDeviceBindingEnabled()) {
            showDeviceBindingConfirmation(
                getString(R.string.device_binding_signup_confirm_title),
                getString(R.string.device_binding_signup_confirm_message)
            ) {
                proceedWithAutoLogin()
            }
        } else {
            proceedWithAutoLogin()
        }
    }

    private fun proceedWithAutoLogin() {
        autoLoginViewModel.initializeTestPressSession(
            activity,
            instituteSettings,
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString(),
            activity.testPressService
        )
    }

    private fun showDeviceBindingConfirmation(title: String, message: String, onConfirm: () -> Unit) {
        MaterialDialog.Builder(requireContext())
            .title(title)
            .content(message)
            .positiveText(R.string.ok)
            .negativeText(R.string.cancel)
            .onPositive { _, _ -> onConfirm() }
            .show()
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

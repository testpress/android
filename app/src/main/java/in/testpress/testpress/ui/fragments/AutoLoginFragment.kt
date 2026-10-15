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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.testpress.util.DeviceBindingDialogs

open class AutoLoginFragment: RegistrationBaseFragment() {

    private lateinit var instituteSettings: InstituteSettings

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var confirmationDialog: AlertDialog? = null

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
        val username = binding.editTextUsername.text.toString()
        val password = binding.editTextPassword.text.toString()
        if (confirmationDialog?.isShowing == true) return
        confirmationDialog = DeviceBindingDialogs.confirmIfBindingEnabled(
            requireContext(),
            instituteSettings,
            onProceed = {
                if (isAdded && view != null) {
                    proceedWithAutoLogin(username, password)
                }
            }
        )
    }

    private fun proceedWithAutoLogin(username: String, password: String) {
        autoLoginViewModel.initializeTestPressSession(
            activity,
            instituteSettings,
            username,
            password,
            activity.testPressService
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }

    override fun onDestroyView() {
        confirmationDialog?.dismiss()
        confirmationDialog = null
        super.onDestroyView()
    }
}

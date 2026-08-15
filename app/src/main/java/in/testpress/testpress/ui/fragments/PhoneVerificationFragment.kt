package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.authenticator.CodeVerificationActivity
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.util.ProgressUtil
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.phone.SmsRetriever

open class PhoneVerificationFragment : RegistrationBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneLayout.visibility = View.VISIBLE
        showOrHideCountryCodePicker()
    }

    override fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                initiateSmsRetrieverClient()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> navigateToCodeVerificationActivity()
                Status.ERROR -> onRegisterException(it.exception)
                else -> {}
            }
        })
    }

    private fun initiateSmsRetrieverClient() {
        val client = SmsRetriever.getClient(activity)
        val task = client?.startSmsRetriever()
        task?.addOnSuccessListener {
            register()
        }
        task?.addOnFailureListener {
            register()  // user have to manually enter the code
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun navigateToCodeVerificationActivity() {
        val intent = Intent(getActivity(), CodeVerificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("username", binding.editTextUsername.text.toString())
            putExtra("password", binding.editTextPassword.text.toString())
            putExtra("phoneNumber", binding.editTextPhone.text.toString())
        }
        startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
    }

    private fun showOrHideCountryCodePicker() {
        if (viewModel.isTwilioEnabled) {
            binding.countryCodePicker.apply {
                visibility = View.VISIBLE
                registerCarrierNumberEditText(binding.editTextPhone)
                setNumberAutoFormattingEnabled(false)
            }
        } else {
            binding.countryCodePicker.visibility = View.GONE
        }
    }
}

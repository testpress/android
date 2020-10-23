package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.authenticator.CodeVerificationActivity
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.util.ProgressUtil
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.register_activity.*

class PhoneVerificationFragment : RegistrationBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        phoneLayout.visibility = View.VISIBLE
        initViewModel()
        initViewModelObservers()
        setCountryCodePicker()
        hideErrorMessageOnTextChange()
        showPasswordToggleOnTextChange()
    }

    private fun initViewModelObservers() {
        viewModel.isUserDataValid.observe(viewLifecycleOwner, Observer {
            if (it) {
                initiateSmsRetrieverClient()
            }
        })

        viewModel.registrationResponse.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> navigateToCodeVerificationActivity()
                Status.ERROR -> setPostDetailsException(it.exception)
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

    private fun register() {
        ProgressUtil.showLoadingDialog(activity)
        viewModel.register()
    }

    private fun navigateToCodeVerificationActivity() {
        val intent = Intent(getActivity(), CodeVerificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("username", editTextUsername.text.toString())
            putExtra("password", editTextPassword.text.toString())
            putExtra("phoneNumber", editTextPhone.text.toString())
        }
        startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
    }

    private fun setCountryCodePicker() {
        if (viewModel.isTwilioEnabled) {
            countryCodePicker.apply {
                visibility = View.VISIBLE
                registerCarrierNumberEditText(editTextPhone)
                setNumberAutoFormattingEnabled(false)
            }
        } else {
            countryCodePicker.visibility = View.GONE
        }
    }
}

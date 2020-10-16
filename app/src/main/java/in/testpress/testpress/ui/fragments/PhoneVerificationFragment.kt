package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.CodeVerificationActivity
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.authenticator.RegisterActivity
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.models.RegistrationErrorDetails
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.ProgressUtil
import `in`.testpress.testpress.util.TextChangeUtil
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.register_activity.*
import retrofit.RetrofitError
import java.util.*

class PhoneVerificationFragment : Fragment() {

    lateinit var viewModel: RegisterViewModel

    lateinit var activity: RegisterActivity

    lateinit var binding: RegisterActivityBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_activity, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = getActivity() as RegisterActivity
        phoneLayout.visibility = View.VISIBLE
        initViewModel()
        setCountryCodePicker()
        setTextWatchers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return RegisterViewModel(RegisterRepository(activity.testPressService), binding) as T
            }
        }).get(RegisterViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.allowToRegister.observe(viewLifecycleOwner, Observer { canRegister ->
            if (canRegister) {
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

    private fun setPostDetailsException(e: Exception?) {
        buttonRegister.isEnabled = true
        ProgressUtil.progressDialog.dismiss()
        if ((e is RetrofitError)) {
            val registrationErrorDetails = e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
            viewModel.handleErrorResponse(registrationErrorDetails)
        }
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

    private fun setTextWatchers() {
        val editTextMap = Hashtable<EditText, TextView>()
        editTextMap[editTextUsername] = usernameErrorText
        editTextMap[editTextPassword] = passwordErrorText
        editTextMap[editTextConfirmPassword] = confirmPasswordErrorText
        editTextMap[editTextEmail] = emailErrorText
        editTextMap[editTextPhone] = phoneErrorText
        for (editText in editTextMap.keys()) {
            editTextMap[editText]?.let { TextChangeUtil.hideErrorMessageOnTextChange(editText, it) }
        }
        TextChangeUtil.showPasswordToggleOnTextChange(editTextPassword, passwordErrorText, passwordInputLayout)
        TextChangeUtil.showPasswordToggleOnTextChange(editTextConfirmPassword, confirmPasswordErrorText, confirmPasswordInputLayout)
    }
}

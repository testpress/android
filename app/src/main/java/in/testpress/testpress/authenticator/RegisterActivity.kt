package `in`.testpress.testpress.authenticator

import `in`.testpress.enums.Status
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.RegistrationErrorDetails
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.InternetConnectivityChecker
import `in`.testpress.testpress.util.ProgressUtil.progressDialog
import `in`.testpress.testpress.util.ProgressUtil.showLoadingDialog
import `in`.testpress.testpress.util.TextChangeUtil.hideErrorMessageOnTextChange
import `in`.testpress.testpress.util.TextChangeUtil.showPasswordToggleOnTextChange
import `in`.testpress.testpress.viewmodel.RegisterViewModel
import `in`.testpress.util.EventsTrackerFacade
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.register_activity.*
import kotlinx.android.synthetic.main.success_message_layout.*
import retrofit.RetrofitError
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {

    @Inject
    lateinit var testPressService: TestpressService
    private var isTwilioEnabled = false
    private lateinit var verificationMethod: VerificationMethod
    private var internetConnectivityChecker = InternetConnectivityChecker(this)
    private lateinit var viewModel : RegisterViewModel
    private var userDetails = UserDetails()
    private lateinit var binding: RegisterActivityBinding
    private var instituteSettingsList: MutableList<InstituteSettings> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.register_activity)
        initViewModel()
        initializeData()
        finishActivityWhenInstituteSettingsEmpty()
        setPhoneVerification()
        setPasswordToggleVisibility()
        setCountryCodePicker()
        setTextWatchers()
        initObservers()
        initListeners()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return RegisterViewModel(RegisterRepository(testPressService), binding) as T
            }
        }).get(RegisterViewModel::class.java)
    }

    private fun initializeData() {
        binding.viewModel = viewModel
        instituteSettingsList = viewModel.instituteSettingsList
        verificationMethod = viewModel.verificationMethod
        isTwilioEnabled = viewModel.isTwilioEnabled
    }

    private fun finishActivityWhenInstituteSettingsEmpty() {
        if (instituteSettingsList.size == 0) {
            finish()
        }
    }

    private fun setPhoneVerification() {
        if (verificationMethod == VerificationMethod.MOBILE) {
            showPhoneVerification()
        } else {
            hidePhoneVerification()
        }
    }

    private fun showPhoneVerification() {
        phoneLayout.visibility = View.VISIBLE
        if (isTwilioEnabled) {
            countryCodePicker.visibility = View.VISIBLE
        }
    }

    private fun hidePhoneVerification() {
        phoneLayout.visibility = View.GONE
        countryCodePicker.visibility = View.GONE
        isTwilioEnabled = false
    }

    private fun setPasswordToggleVisibility() {
        showPasswordToggleOnTextChange(editTextPassword, passwordErrorText, passwordInputLayout)
        showPasswordToggleOnTextChange(editTextConfirmPassword, confirmPasswordErrorText, confirmPasswordInputLayout)
    }

    private fun setCountryCodePicker() {
        if (isTwilioEnabled) {
            countryCodePicker.registerCarrierNumberEditText(editTextPhone)
            countryCodePicker.setNumberAutoFormattingEnabled(false)
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
            editTextMap[editText]?.let { hideErrorMessageOnTextChange(editText, it) }
        }
    }

    private fun initObservers() {
        viewModel.initRegistration.observe(this, androidx.lifecycle.Observer { canRegister ->
            if (canRegister) {
                initRegistration()
            }
        })
    }

    private fun initRegistration() {
        if (verificationMethod == VerificationMethod.MOBILE) {
            showVerifyPhoneAlertDialog()
        } else {
            register()
        }
    }

    private fun showVerifyPhoneAlertDialog() {
        AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle("verify phone")
                .setMessage("We will verify the phone number\n\n" +
                        editTextPhone.text.toString() +
                        "\n\nIs this OK, or would you like to edit the number?")
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    if (internetConnectivityChecker.isConnected) {
                        initiateSmsRetrieverClient()
                    } else {
                        internetConnectivityChecker.showAlert()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.edit, null)
                .show()
    }

    private fun initiateSmsRetrieverClient() {
        val client = SmsRetriever.getClient(this)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            register()
        }
        task.addOnFailureListener {
            register()  // user have to manually enter the code
        }
    }

    private fun register() {
        buttonRegister.isEnabled = false
        showLoadingDialog(this)
        viewModel.result.observe(this, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        setPostDetailsSuccess()
                    }
                }
                Status.ERROR -> setPostDetailsException(it.exception)
            }
        })
        viewModel.register()
    }

    private fun setPostDetailsSuccess() {
        logEvent()
        progressDialog.dismiss()
        when (verificationMethod) {
            VerificationMethod.MOBILE -> navigateToCodeVerificationActivity()
            VerificationMethod.EMAIL -> showVerifyEmailLayout()
            VerificationMethod.NONE -> showVerifyEmailLayout()
        }
    }

    private fun logEvent() {
        val eventsTrackerFacade = EventsTrackerFacade(applicationContext)
        val params = HashMap<String, Any>()
        params["username"] = editTextUsername.text.toString()
        eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params)
    }

    private fun navigateToCodeVerificationActivity() {
        val intent = Intent(this, CodeVerificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("username", userDetails.username)
            putExtra("password", userDetails.password)
            putExtra("phoneNumber", userDetails.phoneNumber)
        }
        startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
    }

    private fun showVerifyEmailLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }

    private fun setPostDetailsException(e: Exception?) {
        buttonRegister.isEnabled = true
        progressDialog.dismiss()
        if ((e is RetrofitError)) {
            val registrationErrorDetails = e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
            viewModel.handleErrorResponse(registrationErrorDetails)
        }
    }

    private fun initListeners() {
        setOnClickListeners()
        setEditorActionListener()
    }

    private fun setOnClickListeners() {
        success_ok.setOnClickListener {
            finish()
        }
    }

    private fun setEditorActionListener() {
        editTextConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE && buttonRegister.isEnabled) {
                if (viewModel.isValid()) {
                    initRegistration()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            setResult(resultCode)
            finish()
        }
    }
}

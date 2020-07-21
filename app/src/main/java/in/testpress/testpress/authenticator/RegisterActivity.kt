package `in`.testpress.testpress.authenticator

import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.RegistrationErrorDetails
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import `in`.testpress.testpress.ui.utils.ShowProgressUtil.progressDialog
import `in`.testpress.testpress.ui.utils.ShowProgressUtil.showProgressDialog
import `in`.testpress.testpress.util.*
import `in`.testpress.testpress.viewmodels.RegisterViewModel
import `in`.testpress.util.EventsTrackerFacade
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var registrationSuccessResponse: RegistrationSuccessResponse
    private var internetConnectivityChecker = InternetConnectivityChecker(this)
    private lateinit var viewModel: RegisterViewModel

    enum class VerificationMethod { MOBILE, EMAIL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
        Injector.inject(this)
        initViewModel()
        getVerificationMethod()
        setPhoneVerificationVisibility()
        setTextWatchers()
        initListeners()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(this@RegisterActivity) as T
            }
        }).get(RegisterViewModel::class.java)
    }

    private fun getVerificationMethod() {
        val instituteSettingsList = viewModel.instituteSettingsList
        if (instituteSettingsList.size != 0) {
            verificationMethod = viewModel.verificationMethod
            setIsTwilioEnabled()
        } else {
            finish()
        }
    }

    private fun setIsTwilioEnabled() {
        isTwilioEnabled = viewModel.isTwilioEnabled
    }

    private fun setPhoneVerificationVisibility() {
        if (verificationMethod == VerificationMethod.MOBILE) {
            phoneLayout.visibility = View.VISIBLE
            if (!isTwilioEnabled) {
                countryCodePicker.visibility = View.GONE
            }
        } else {
            phoneLayout.visibility = View.GONE
            countryCodePicker.visibility = View.GONE
            isTwilioEnabled = false
        }
        if (isTwilioEnabled) {
            setCountryCodePicker()
        }
    }

    private fun setCountryCodePicker() {
        countryCodePicker.registerCarrierNumberEditText(editTextPhone)
        countryCodePicker.setNumberAutoFormattingEnabled(false)
    }

    private fun setTextWatchers() {
        val editTextMap = Hashtable<EditText, TextView>()
        editTextMap[editTextUsername] = usernameErrorText
        editTextMap[editTextPassword] = passwordErrorText
        editTextMap[editTextConfirmPassword] = confirmPasswordErrorText
        editTextMap[editTextEmail] = emailErrorText
        editTextMap[editTextPhone] = phoneErrorText
        for (editText in editTextMap.keys()) {
            editTextMap[editText]?.let { TextChangeUtil().hideErrorMessageOnTextChange(editText, it) }
        }
    }

    private fun initListeners() {
        setEditorActionListener()
        setOnClickListeners()
    }

    private fun setEditorActionListener() {
        editTextConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE && buttonRegister.isEnabled) {
                register()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun register() {
        if (isValid()) {
            if (verificationMethod == VerificationMethod.MOBILE) {
                showVerifyPhoneAlertDialog()
            } else {
                postDetails()
            }
        }
    }

    private var isValid = true
    private fun isValid(): Boolean {
        checkAndSetEmptyError()
        verifyInput()
        return isValid
    }

    private fun checkAndSetEmptyError() {
        if (editTextPassword.text.toString().trim().isEmpty()) {
            setErrorText(passwordErrorText, isValid = false)
        }
        if (editTextConfirmPassword.text.toString().trim().isEmpty()) {
            setErrorText(confirmPasswordErrorText, isValid = false)
        }
        if (editTextEmail.text.toString().trim().isEmpty()) {
            setErrorText(emailErrorText, isValid = false)
        }
        if (editTextUsername.text.toString().trim().isEmpty()) {
            setErrorText(usernameErrorText, isValid = false)
        }

        if ((editTextPhone.text.toString().trim().isEmpty() && verificationMethod != VerificationMethod.EMAIL)) {
            setErrorText(phoneErrorText, isValid = false)
        }
    }

    private fun verifyInput() {
        verifyUserName()
        verifyEmail()
        if (verificationMethod == VerificationMethod.MOBILE) {
            verifyPhoneNumber()
        }
        verifyPassword()
        verifyConfirmPassword()
    }

    private fun verifyUserName() {
        val isUsernameValid = Validator.isUsernameValid(editTextUsername.text.toString().trim())
        if (editTextUsername.text.toString().trim().isNotEmpty() && !isUsernameValid) {
            setErrorText(usernameErrorText, getString(R.string.username_error), false)
            editTextUsername.requestFocus()
        }
    }

    private fun verifyEmail() {
        val isEmailValid = Validator.isEmailValid(editTextEmail.text.toString().trim())
        if (editTextEmail.text.toString().trim().isNotEmpty() && !isEmailValid) {
            setErrorText(emailErrorText, getString(R.string.email_error), false)
            editTextEmail.requestFocus()
        }
    }

    private fun verifyPhoneNumber() {
        val isPhoneNumberValid = if (isTwilioEnabled) {
            PhoneNumberValidator.validateInternationalPhoneNumber(countryCodePicker)
        } else {
            PhoneNumberValidator.validatePhoneNumber(editTextPhone.text.toString().trim())
        }

        if (editTextPhone.text.toString().trim().isNotEmpty() && !isPhoneNumberValid) {
            setErrorText(phoneErrorText, getString(R.string.phone_number_error), false)
            editTextPhone.requestFocus()
        }
    }

    private fun verifyPassword() {
        if (editTextPassword.text.toString().trim().isNotEmpty() && editTextPassword.text.toString().trim().length < 6) {
            setErrorText(passwordErrorText, getString(R.string.password_error), false)
            editTextPassword.requestFocus()
        }
    }

    private fun verifyConfirmPassword() {
        if (editTextConfirmPassword.text.toString().trim().isNotEmpty() &&
                editTextPassword.text.toString() != editTextConfirmPassword.text.toString().trim()) {
            setErrorText(confirmPasswordErrorText, getString(R.string.password_mismatch_error), false)
            editTextConfirmPassword.requestFocus()
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
            postDetails()
        }
        task.addOnFailureListener {
            postDetails()
        }
    }

    private fun postDetails() {
        buttonRegister.isEnabled = false
        showProgressDialog(this)
        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                registrationSuccessResponse = getRegistrationSuccessResponse()
                return true
            }

            override fun onException(e: Exception?) {
                super.onException(e)
                setPostDetailsException(e)
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                setPostDetailsSuccess()
            }
        }.execute()
    }

    private fun getRegistrationSuccessResponse(): RegistrationSuccessResponse {
        return if (isTwilioEnabled) {
            registerTestPressService(countryCodePicker.selectedCountryNameCode)
        } else {
            registerTestPressService("")
        }
    }

    private fun registerTestPressService(countryCode: String): RegistrationSuccessResponse {
        return testPressService.register(
                editTextUsername.text.toString(),
                editTextEmail.text.toString(),
                editTextPassword.text.toString(),
                editTextPhone.text.toString(),
                countryCode
        )
    }

    private fun setPostDetailsException(e: Exception?) {
        buttonRegister.isEnabled = true
        if ((e is RetrofitError)) {
            val registrationErrorDetails = e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
            handleRegistrationErrorDetails(registrationErrorDetails)
        }
        progressDialog.dismiss()
    }

    private fun handleRegistrationErrorDetails(registrationErrorDetails: RegistrationErrorDetails) {
        if (registrationErrorDetails.username.isNotEmpty()) {
            setErrorText(usernameErrorText, registrationErrorDetails.username[0], null)
            editTextUsername.requestFocus()
        }
        if (registrationErrorDetails.email.isNotEmpty()) {
            setErrorText(emailErrorText, registrationErrorDetails.email[0], null)
            editTextEmail.requestFocus()
        }
        if (registrationErrorDetails.password.isNotEmpty()) {
            setErrorText(passwordErrorText, registrationErrorDetails.password[0], null)
            editTextPassword.requestFocus()
        }
        if (registrationErrorDetails.phone.isNotEmpty()) {
            setErrorText(phoneErrorText, registrationErrorDetails.phone[0], null)
            editTextPhone.requestFocus()
        }
    }

    private fun setPostDetailsSuccess() {
        progressDialog.dismiss()
        logEvent()
        if (verificationMethod == VerificationMethod.MOBILE) {
            navigateToCodeVerificationActivity()
        } else {
            showSuccessLayout()
        }
    }

    private fun navigateToCodeVerificationActivity() {
        val intent = Intent(this, CodeVerificationActivity::class.java)
        setDataInIntent()
        startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
    }

    private fun setDataInIntent() {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("username", registrationSuccessResponse.username)
        intent.putExtra("password", registrationSuccessResponse.password)
        intent.putExtra("phoneNumber", registrationSuccessResponse.phone)
    }

    private fun showSuccessLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }

    private fun logEvent() {
        val eventsTrackerFacade = EventsTrackerFacade(applicationContext)
        val params = HashMap<String, Any>()
        params["username"] = registrationSuccessResponse.username
        eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params)
    }

    private fun setOnClickListeners() {
        buttonRegister.setOnClickListener {
            register()
        }
        success_ok.setOnClickListener {
            verificationMailSent()
        }
    }

    private fun verificationMailSent() {
        finish()
    }

    private fun setErrorText(errorTextView: TextView, errorText: String = getString(R.string.empty_input_error), isValid: Boolean?) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
        if (isValid == false) {
            this.isValid = isValid
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
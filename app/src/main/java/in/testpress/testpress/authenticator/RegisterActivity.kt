package `in`.testpress.testpress.authenticator

import `in`.testpress.enums.Status
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.*
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.InternetConnectivityChecker
import `in`.testpress.testpress.util.PhoneNumberValidator
import `in`.testpress.testpress.util.ProgressUtil.progressDialog
import `in`.testpress.testpress.util.ProgressUtil.showLoadingDialog
import `in`.testpress.testpress.util.TextChangeUtil.hideErrorMessageOnTextChange
import `in`.testpress.testpress.util.TextChangeUtil.showPasswordToggleOnTextChange
import `in`.testpress.testpress.util.Validator
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
    private val daoSession: DaoSession = TestpressApplication.getDaoSession()
    private val instituteSettingsDao: InstituteSettingsDao = daoSession.instituteSettingsDao
    private var internetConnectivityChecker = InternetConnectivityChecker(this)
    private lateinit var viewModel: RegisterViewModel
    private var userDetails = HashMap<String,String>()

    private val instituteSettingsList: MutableList<InstituteSettings> = instituteSettingsDao.queryBuilder()
            .where(InstituteSettingsDao.Properties.BaseUrl.eq(BuildConfig.BASE_URL))
            .list()

    enum class VerificationMethod { MOBILE, EMAIL, NONE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
        Injector.inject(this)
        initViewModel()
        getVerificationMethod()
        setIsTwilioEnabled()
        setViewVisibility()
        setCountryCodePicker()
        setTextWatchers()
        initListeners()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(RegisterRepository(testPressService)) as T
            }
        }).get(RegisterViewModel::class.java)
    }

    private fun getVerificationMethod() {
        if (instituteSettingsList.size != 0) {
            verificationMethod = InstituteSettings().getVerificationType(instituteSettingsList[0])
        } else {
            finish()
        }
    }

    private fun setIsTwilioEnabled() {
        isTwilioEnabled = instituteSettingsList[0].twilioEnabled
    }

    private fun setViewVisibility() {
        setPhoneVerificationVisibility()
        setPasswordToggleVisibility()
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
    }

    private fun setCountryCodePicker() {
        if (isTwilioEnabled) {
            countryCodePicker.registerCarrierNumberEditText(editTextPhone)
            countryCodePicker.setNumberAutoFormattingEnabled(false)
        }
    }

    private fun setPasswordToggleVisibility() {
        showPasswordToggleOnTextChange(editTextPassword, passwordErrorText, passwordInputLayout)
        showPasswordToggleOnTextChange(editTextConfirmPassword, confirmPasswordErrorText, confirmPasswordInputLayout)
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

    private fun initListeners() {
        setEditorActionListener()
        setOnClickListeners()
    }

    private fun setEditorActionListener() {
        editTextConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE && buttonRegister.isEnabled) {
                if (isValid()) {
                    initRegistration()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun initRegistration() {
        if (verificationMethod == VerificationMethod.MOBILE) {
            showVerifyPhoneAlertDialog()
        } else {
            register()
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
            register()
        }
        task.addOnFailureListener {
            // user have to manually enter the code
            register()
        }
    }

    private fun register() {
        buttonRegister.isEnabled = false
        showLoadingDialog(this)
        viewModel.register(getUserInput())
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
    }

    private fun getUserInput(): HashMap<String, String> {
        userDetails = HashMap<String, String>().apply {
            this["username"] = editTextUsername.text.toString()
            this["email"] = editTextEmail.text.toString()
            this["password"] = editTextPassword.text.toString()
            this["phone"] = editTextPhone.text.toString()
            if (isTwilioEnabled) {
                this["country_code"] = countryCodePicker.selectedCountryNameCode
            } else {
                this["country_code"] = ""
            }
        }
        return userDetails
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
        when(verificationMethod) {
            VerificationMethod.MOBILE -> navigateToCodeVerificationActivity()
            VerificationMethod.EMAIL ->  showVerifyEmailLayout()
            VerificationMethod.NONE ->  showVerifyEmailLayout()
        }
    }

    private fun navigateToCodeVerificationActivity() {
        val intent = Intent(this, CodeVerificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("username", userDetails["username"])
            putExtra("password", userDetails["password"])
            putExtra("phoneNumber", userDetails["phone"])
        }
        startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
    }

    private fun showVerifyEmailLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }

    private fun logEvent() {
        val eventsTrackerFacade = EventsTrackerFacade(applicationContext)
        val params = HashMap<String, Any>()
        params["username"] = editTextUsername.text.toString()
        eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params)
    }

    private fun setOnClickListeners() {
        buttonRegister.setOnClickListener {
           if(isValid()) {
               initRegistration()
           }
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

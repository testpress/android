package `in`.testpress.testpress.authenticator

import `in`.testpress.testpress.BuildConfig.BASE_URL
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.authenticator.LoginActivity.Companion.REQUEST_CODE_REGISTER_USER
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettingsDao
import `in`.testpress.testpress.models.RegistrationErrorDetails
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import `in`.testpress.testpress.util.InternetConnectivityChecker
import `in`.testpress.testpress.util.PhoneNumberValidator
import `in`.testpress.testpress.util.SafeAsyncTask
import `in`.testpress.util.EventsTrackerFacade
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.register_activity.*
import kotlinx.android.synthetic.main.register_activity.editTextUserName
import kotlinx.android.synthetic.main.success_message_layout.*
import retrofit.RetrofitError
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.HashMap


class RegisterActivity : AppCompatActivity() {

    @Inject
    lateinit var testPressService: TestpressService
    private lateinit var progressDialog: MaterialDialog
    private var isTwilioEnabled = false
    private lateinit var verificationMethod: VerificationMethod
    private lateinit var registrationSuccessResponse: RegistrationSuccessResponse
    private var internetConnectivityChecker =  InternetConnectivityChecker(this)
    enum class VerificationMethod { MOBILE, EMAIL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.register_activity)
        ButterKnife.inject(this)
        getVerificationMethod()
        setPhoneVerificationVisibility()
        setTextWatchers()
        setEditorActionListener()
        setOnClickListeners()
    }

    private fun getVerificationMethod() {
        val daoSession = TestpressApplication.getDaoSession()
        val instituteSettingsDao = daoSession.instituteSettingsDao
        val instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list()

        if (instituteSettingsList.size != 0) {
            val instituteSettings = instituteSettingsList[0]
            verificationMethod = if (instituteSettings.verificationMethod == "M") {
                VerificationMethod.MOBILE
            } else {
                VerificationMethod.EMAIL
            }
            isTwilioEnabled = instituteSettings.twilioEnabled
        } else {
            finish()
        }
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
            isTwilioEnabled=false
        }

        if(isTwilioEnabled) {
            countryCodePicker.registerCarrierNumberEditText(editTextPhone)
            countryCodePicker.setNumberAutoFormattingEnabled(false)
        }
    }

    private fun setTextWatchers() {
        val editTextMap = Hashtable<EditText, TextView>()
        editTextMap[editTextUserName] = usernameErrorText
        editTextMap[editTextPassword] = passwordErrorText
        editTextMap[editTextConfirmPassword] = confirmPasswordErrorText
        editTextMap[editTextEmail] = emailErrorText
        editTextMap[editTextPhone] = phoneErrorText

        for( editText in editTextMap.keys()) {
            editTextMap[editText]?.let { hideErrorMessageOnTextChange(editText, it) }
        }
    }

   private fun hideErrorMessageOnTextChange(editText: EditText, errorText: TextView) {
       editText.addTextChangedListener(object : TextWatcher {
           override fun afterTextChanged(s: Editable?) {
               errorText.visibility = View.GONE
           }

           override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
           }

           override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
           }
       })
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
        if(validate()) {
            if (verificationMethod == VerificationMethod.MOBILE) {
                showVerifyPhoneAlertDialog()
            } else {
                postDetails()
            }
        }
    }

    private var isValid = true

    private fun validate(): Boolean {
        checkAndSetEmptyError()
        verifyUserName()
        verifyEmail()
        if (verificationMethod == VerificationMethod.MOBILE) {
            verifyPhoneNumber()
        }
        verifyPassword()
        verifyConfirmPassword()
        return isValid
    }

    private fun checkAndSetEmptyError() {
        if (!populated(editTextPassword)) {
            passwordErrorText.visibility = View.VISIBLE
            passwordErrorText.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(editTextConfirmPassword)) {
            confirmPasswordErrorText.visibility = View.VISIBLE
            confirmPasswordErrorText.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(editTextEmail)) {
            emailErrorText.visibility = View.VISIBLE
            emailErrorText.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(editTextUserName)) {
            usernameErrorText.visibility = View.VISIBLE
            usernameErrorText.text = getString(R.string.empty_input_error)
            isValid = false
        }

        if ((!populated(editTextPhone) && verificationMethod != VerificationMethod.EMAIL)) {
            phoneErrorText.visibility = View.VISIBLE
            phoneErrorText.text = getString(R.string.empty_input_error)
            isValid = false
        }
    }

    private fun verifyUserName() {
        val userNamePattern = Pattern.compile("[a-z0-9]*")
        val userNameMatcher = userNamePattern.matcher(editTextUserName.text.toString().trim())
        if (populated(editTextUserName) && !userNameMatcher.matches()) {
            usernameErrorText.visibility = View.VISIBLE
            usernameErrorText.text = getString(R.string.username_error)
            editTextUserName.requestFocus()
            isValid = false
        }
    }

    private fun verifyEmail() {
        if(populated(editTextEmail) && !android.util.Patterns.EMAIL_ADDRESS.matcher(editTextEmail.text.toString().trim()).matches()) {
            emailErrorText.visibility = View.VISIBLE
            emailErrorText.text = getString(R.string.email_error)
            editTextEmail.requestFocus()
            isValid = false
        }
    }

    private fun verifyPhoneNumber() {
        val isPhoneNumberValid = if (isTwilioEnabled) {
            PhoneNumberValidator.validateInternationalPhoneNumber(countryCodePicker)
        } else {
            PhoneNumberValidator.validatePhoneNumber(editTextPhone.text.toString().trim())
        }

        if (populated(editTextPhone) && !isPhoneNumberValid) {
            phoneErrorText.visibility = View.VISIBLE
            phoneErrorText.text = getString(R.string.phone_number_error)
            editTextPhone.requestFocus()
            isValid = false
        }
    }

    private fun verifyPassword() {
        if(populated(editTextPassword) && editTextPassword.text.toString().trim().length < 6 ) {
            passwordErrorText.visibility = View.VISIBLE
            passwordErrorText.text = getString(R.string.password_error)
            editTextPassword.requestFocus()
            isValid = false
        }
    }

    private fun verifyConfirmPassword() {
        if(populated(editTextConfirmPassword) &&
                editTextPassword.text.toString() != editTextConfirmPassword.text.toString().trim()){

            confirmPasswordErrorText.visibility = View.VISIBLE
            confirmPasswordErrorText.text = getString(R.string.password_mismatch_error)
            editTextConfirmPassword.requestFocus()
            isValid = false
        }
    }

    private fun populated(editText: EditText): Boolean {
        return editText.text.toString().trim().isNotEmpty()
    }

    private fun showVerifyPhoneAlertDialog() {
        AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle(R.string.verify_phone)
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

    private fun postDetails() {
        buttonRegister.isEnabled = false
        showProgressDialog()
        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                registrationSuccessResponse = getRegistrationSuccessResponse()
                return true
            }

            override fun onException(e: java.lang.Exception?) {
                super.onException(e)
                buttonRegister.isEnabled = true

                if((e is RetrofitError)) {
                    val registrationErrorDetails =  e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
                    handleRegistrationErrorDetails(registrationErrorDetails)
                }
                progressDialog.dismiss()
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                progressDialog.dismiss()
                logEvent()
                if (verificationMethod == VerificationMethod.MOBILE) {
                   navigateToCodeVerificationActivity()
                } else {
                    showSuccessLayout()
                }
            }

        }.execute()
    }

    private fun showProgressDialog() {
        progressDialog = MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .cancelable(false)
                .show()
    }

    private fun getRegistrationSuccessResponse(): RegistrationSuccessResponse {

        return if(isTwilioEnabled){
            testPressService.register(
                    editTextUserName.text.toString(),
                    editTextEmail.text.toString(),
                    editTextPassword.text.toString(),
                    editTextPhone.text.toString(),
                    countryCodePicker.selectedCountryNameCode
            )
        } else {
            testPressService.register(
                    editTextUserName.text.toString(),
                    editTextEmail.text.toString(),
                    editTextPassword.text.toString(),
                    editTextPhone.text.toString(),
                    ""
            )
        }
    }

    private fun handleRegistrationErrorDetails(registrationErrorDetails: RegistrationErrorDetails) {
        if(registrationErrorDetails.username.isNotEmpty()) {
            setUserNameError(registrationErrorDetails)
        }
        if(registrationErrorDetails.email.isNotEmpty()) {
            setEmailError(registrationErrorDetails)
        }
        if(registrationErrorDetails.password.isNotEmpty()) {
            setPasswordError(registrationErrorDetails)
        }
        if(registrationErrorDetails.phone.isNotEmpty()) {
            setPhoneNumberError(registrationErrorDetails)
        }
    }

    private fun logEvent() {
        val eventsTrackerFacade = EventsTrackerFacade(applicationContext)
        val params = HashMap<String, Any>()
        params["username"] = registrationSuccessResponse.username
        eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params)
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
        intent?.extras?.let {
            intent.putExtras(it)
        }
    }

    private fun showSuccessLayout() {
        registerLayout.visibility = View.GONE
        success_description.setText(R.string.activation_email_sent_message)
        success_complete.visibility = View.VISIBLE
    }

    private fun setUserNameError(registrationErrorDetails: RegistrationErrorDetails) {
        usernameErrorText.visibility = View.VISIBLE
        usernameErrorText.text = registrationErrorDetails.username[0]
        editTextUserName.requestFocus()
    }

    private fun setEmailError(registrationErrorDetails: RegistrationErrorDetails) {
        emailErrorText.visibility = View.VISIBLE
        emailErrorText.text = registrationErrorDetails.email[0]
        editTextEmail.requestFocus()
    }

    private fun setPasswordError(registrationErrorDetails: RegistrationErrorDetails) {
        passwordErrorText.visibility = View.VISIBLE
        passwordErrorText.text = registrationErrorDetails.password[0]
        editTextPassword.requestFocus()
    }

    private fun setPhoneNumberError(registrationErrorDetails: RegistrationErrorDetails) {
        phoneErrorText.visibility = View.VISIBLE
        phoneErrorText.text = registrationErrorDetails.phone[0]
        editTextPhone.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            setResult(resultCode)
            finish()
        }
    }
}

package `in`.testpress.testpress.authenticator;

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
import android.content.DialogInterface
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
import kotlinx.android.synthetic.main.register_activity.et_username
import kotlinx.android.synthetic.main.success_message_layout.*
import retrofit.RetrofitError
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.HashMap


class RegisterActivity: AppCompatActivity() {


    @Inject lateinit var testPressService: TestpressService

    private lateinit var registrationSuccessResponse: RegistrationSuccessResponse
    private lateinit var progressDialog: MaterialDialog
    private var internetConnectivityChecker =  InternetConnectivityChecker(this)
    private var isTwilioEnabled = false
    private lateinit var verificationMethod: VerificationMethod
    enum class VerificationMethod { MOBILE, EMAIL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.register_activity)
        ButterKnife.inject(this)
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
            // Never happen, just for a safety.
            finish()
        }

        et_password_confirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE && b_register.isEnabled) {
                register()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        if (verificationMethod == VerificationMethod.MOBILE) {
            phone_layout.visibility = View.VISIBLE

            if (!isTwilioEnabled) {
                country_code_picker.visibility = View.GONE
            }
        } else {
            phone_layout.visibility = View.GONE
            country_code_picker.visibility = View.GONE
            isTwilioEnabled=false
        }

        if(isTwilioEnabled) {
            country_code_picker.registerCarrierNumberEditText(et_phone)
            country_code_picker.setNumberAutoFormattingEnabled(false)
        }
        setTextWatchers()
        setOnClickListeners()
    }

    private fun setTextWatchers() {
        val editTextMap = Hashtable<EditText, TextView>()
        editTextMap[et_username] = username_error
        editTextMap[et_password] = password_error
        editTextMap[et_password_confirm] = confirm_password_error
        editTextMap[et_email] = email_error
        editTextMap[et_phone] = phone_error

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

    private fun postDetails() {
        b_register.isEnabled = false
        progressDialog = MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .cancelable(false)
                .show()

        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                registrationSuccessResponse = if(isTwilioEnabled){
                    testPressService.register(et_username.text.toString(), et_email.text.toString(),
                            et_password.text.toString(), et_phone.text.toString(), country_code_picker.selectedCountryNameCode)
                } else {
                    testPressService.register(et_username.text.toString(), et_email.text.toString(),
                            et_password.text.toString(), et_phone.text.toString(), "")
                }
                return true
            }

            override fun onException(e: java.lang.Exception?) {
                super.onException(e)
                b_register.isEnabled = true
                // Retrofit Errors are handled
                if((e is RetrofitError)) {
                    val registrationErrorDetails =  e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
                    if(registrationErrorDetails.username.isNotEmpty()) {
                        username_error.visibility = View.VISIBLE
                        username_error.text = registrationErrorDetails.username[0]
                        et_username.requestFocus()
                    }
                    if(registrationErrorDetails.email.isNotEmpty()) {
                        email_error.visibility = View.VISIBLE
                        email_error.text = registrationErrorDetails.email[0]
                        et_email.requestFocus()
                    }
                    if(registrationErrorDetails.password.isNotEmpty()) {
                        password_error.visibility = View.VISIBLE
                        password_error.text = registrationErrorDetails.password[0]
                        et_password.requestFocus()
                    }
                    if(registrationErrorDetails.phone.isNotEmpty()) {
                        phone_error.visibility = View.VISIBLE
                        phone_error.text = registrationErrorDetails.phone[0]
                        et_phone.requestFocus()
                    }
                }
                progressDialog.dismiss()
            }

            private fun logEvent() {
                val eventsTrackerFacade = EventsTrackerFacade(applicationContext)
                val params = HashMap<String, Any>()
                params["username"] = registrationSuccessResponse.username
                eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params)
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                progressDialog.dismiss()
                logEvent()
                if (verificationMethod == VerificationMethod.MOBILE) {
                    val intent = Intent(this@RegisterActivity, CodeVerificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("username", registrationSuccessResponse.username)
                    intent.putExtra("password", registrationSuccessResponse.password)
                    intent.putExtra("phoneNumber", registrationSuccessResponse.phone)
                    intent.putExtras(getIntent().extras)
                    startActivityForResult(intent, REQUEST_CODE_REGISTER_USER)
                } else {
                    register_layout.visibility = View.GONE
                    success_description.setText(R.string.activation_email_sent_message)
                    success_complete.visibility = View.VISIBLE
                }
            }
        }.execute()
    }

    private fun populated(editText: EditText): Boolean {
        return editText.text.toString().trim().isNotEmpty()
    }

    private fun validate(): Boolean {
        var isValid = true

        if (!populated(et_password)) {
            password_error.visibility = View.VISIBLE
            password_error.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(et_password_confirm)) {
            confirm_password_error.visibility = View.VISIBLE
            confirm_password_error.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(et_email)) {
            email_error.visibility = View.VISIBLE
            email_error.text = getString(R.string.empty_input_error)
            isValid = false
        }
        if (!populated(et_username)) {
            username_error.visibility = View.VISIBLE
            username_error.text = getString(R.string.empty_input_error)
            isValid = false
        }

        if ((!populated(et_phone) && verificationMethod != VerificationMethod.EMAIL)) {
            phone_error.visibility = View.VISIBLE
            phone_error.text = getString(R.string.empty_input_error)
            isValid = false
        }

        //username verification
        val userNamePattern = Pattern.compile("[a-z0-9]*")
        val userNameMatcher = userNamePattern.matcher(et_username.text.toString().trim())
        if (populated(et_username) && !userNameMatcher.matches()) {
            username_error.visibility = View.VISIBLE
            username_error.text = getString(R.string.username_error)
            et_username.requestFocus()
            isValid = false
        }

        //Email verification
        if(populated(et_email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString().trim()).matches()) {
            email_error.visibility = View.VISIBLE
            email_error.text = getString(R.string.email_error)
            et_email.requestFocus()
            isValid = false
        }
        if (verificationMethod == VerificationMethod.MOBILE) {
            //Phone number verification
            var isPhoneNumberValid: Boolean? = null

            isPhoneNumberValid = if (isTwilioEnabled) {
                PhoneNumberValidator.validateInternationalPhoneNumber(country_code_picker)
            } else {
                PhoneNumberValidator.validatePhoneNumber(et_phone.text.toString().trim())
            }

            if (populated(et_phone) && !isPhoneNumberValid) {
                phone_error.visibility = View.VISIBLE
                phone_error.text = getString(R.string.phone_number_error)
                et_phone.requestFocus()
                isValid = false
            }
        }
        //Password verification
        if(populated(et_password) && et_password.text.toString().trim().length < 6 ) {
            password_error.visibility = View.VISIBLE
            password_error.text = getString(R.string.password_error)
            et_password.requestFocus()
            isValid = false
        }
        //ConfirmPassword verification
        if(populated(et_password_confirm) && et_password.text.toString() != et_password_confirm.text.toString().trim()){
            confirm_password_error.visibility = View.VISIBLE
            confirm_password_error.text = getString(R.string.password_mismatch_error)
            et_password_confirm.requestFocus()
            isValid = false
        }
        return isValid
    }

    private fun setOnClickListeners() {
        b_register.setOnClickListener {
            register()
        }
        success_ok.setOnClickListener {
            verificationMailSent()
        }
    }

    fun register() {
        val populated = populated(et_username) && populated(et_password) &&
                populated(et_email) && populated(et_password_confirm) &&
                (populated(et_phone) || verificationMethod == VerificationMethod.EMAIL)

        if(validate()) {
            if (verificationMethod == VerificationMethod.MOBILE) {
                 AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setTitle("Verify phone")
                        .setMessage("We will verify the phone number\n\n" + et_phone.text.toString() +
                                "\n\nIs this OK, or would you like to edit the number?")
                        .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, _ ->
                            if (internetConnectivityChecker.isConnected) {
                                initiateSmsRetrieverClient()
                            } else {
                                internetConnectivityChecker.showAlert()
                            }
                            dialog.dismiss()
                        })
                        .setNegativeButton(R.string.edit, null)
                        .show()

            } else {
                postDetails()
            }
        }
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

    private fun verificationMailSent() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            setResult(resultCode)
            finish()
        }
    }
}

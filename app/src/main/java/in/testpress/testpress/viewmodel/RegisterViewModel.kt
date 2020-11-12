package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.*
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.RegisterFormUserInputValidator
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit.RetrofitError

class RegisterViewModel(
        private val repository: RegisterRepository,
        private val binding: RegisterActivityBinding) : ViewModel() {

    val registrationResponse = repository.result

    private val daoSession: DaoSession = TestpressApplication.getDaoSession()
    private val instituteSettingsDao: InstituteSettingsDao = daoSession.instituteSettingsDao
    private val instituteSettingsList: MutableList<InstituteSettings> = instituteSettingsDao.queryBuilder()
            .where(InstituteSettingsDao.Properties.BaseUrl.eq(BuildConfig.BASE_URL))
            .list()

    private val verificationMethod: VerificationMethod = InstituteSettings().getVerificationType(instituteSettingsList[0])

    val isTwilioEnabled: Boolean = instituteSettingsList[0].twilioEnabled

    val instituteSettings = instituteSettingsList[0]

    var isUserDataValid = MutableLiveData<Boolean>()

    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()

    fun isUserDetailsValid() {
        val isValid = RegisterFormUserInputValidator(binding, verificationMethod, isTwilioEnabled).isValid()
        if (isValid) {
            isUserDataValid.postValue(true)
        } else {
            isUserDataValid.postValue(false)
        }
    }

    fun register() {
        repository.register(getUserDetails())
    }

    private fun getUserDetails(): UserDetails {
        return UserDetails(
                username = username.value!!,
                email = email.value!!,
                password = password.value!!,
                phoneNumber = phoneNumber.value!!
        )
    }

    fun handleErrorResponse(e: Exception?) {
        if ((e is RetrofitError)) {
            val registrationErrorDetails = e.getBodyAs(RegistrationErrorDetails::class.java) as RegistrationErrorDetails
            if (registrationErrorDetails.username.isNullOrEmpty().not()) {
                setErrorText(binding.usernameErrorText, registrationErrorDetails.username[0])
                binding.editTextUsername.requestFocus()
            }
            if (registrationErrorDetails.email.isNullOrEmpty().not()) {
                setErrorText(binding.emailErrorText, registrationErrorDetails.email[0])
                binding.editTextEmail.requestFocus()
            }
            if (registrationErrorDetails.password.isNullOrEmpty().not()) {
                setErrorText(binding.passwordErrorText, registrationErrorDetails.password[0])
                binding.editTextPassword.requestFocus()
            }
            if (registrationErrorDetails.phone.isNullOrEmpty().not()) {
                setErrorText(binding.phoneErrorText, registrationErrorDetails.phone[0])
                binding.editTextPhone.requestFocus()
            }
        }
    }

    private fun setErrorText(errorTextView: TextView, errorText: String) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
    }
}

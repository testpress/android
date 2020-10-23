package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.*
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.RegisterFormUserInputValidation
import android.view.View
import android.widget.TextView
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel(
        private val repository: RegisterRepository,
        private val binding: RegisterActivityBinding
        ) : ViewModel() {

    val result = repository.result

    private val daoSession: DaoSession = TestpressApplication.getDaoSession()
    private val instituteSettingsDao: InstituteSettingsDao = daoSession.instituteSettingsDao
    val instituteSettingsList: MutableList<InstituteSettings> = instituteSettingsDao.queryBuilder()
            .where(InstituteSettingsDao.Properties.BaseUrl.eq(BuildConfig.BASE_URL))
            .list()

    val verificationMethod: VerificationMethod = InstituteSettings().getVerificationType(instituteSettingsList[0])
    val isTwilioEnabled: Boolean = instituteSettingsList[0].twilioEnabled

    var initRegistration = MutableLiveData<Boolean>(false)
    private val registrationForm = RegisterFormUserInputValidation(binding, verificationMethod, isTwilioEnabled)

    val username = ObservableField<String>()
    val email = ObservableField<String>()
    val phoneNumber = ObservableField<String>()
    val password = ObservableField<String>()
    val confirmPassword = ObservableField<String>()

    fun isValid(): Boolean {
        val isValid = registrationForm.isValid()
        if (isValid) {
            initRegistration.postValue(true)
        } else {
            initRegistration.postValue(false)
        }
        return isValid
    }

    fun register() {
        repository.register(getUserDetails())
    }

    private fun getUserDetails(): UserDetails {
        return UserDetails(
                binding.editTextUsername.text.toString(),
                binding.editTextEmail.text.toString(),
                binding.editTextPassword.text.toString(),
                binding.editTextConfirmPassword.text.toString(),
                binding.editTextPhone.text.toString()
        )
    }

    fun handleErrorResponse(registrationErrorDetails: RegistrationErrorDetails) {
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

    private fun setErrorText(errorTextView: TextView, errorText: String) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
    }
}

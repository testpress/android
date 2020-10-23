package `in`.testpress.testpress.viewmodel

import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.repository.RegisterRepository
import `in`.testpress.testpress.util.RegisterFormUserInputValidation
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel(
        private val repository: RegisterRepository,
        private val binding: RegisterActivityBinding,
        verificationMethod: VerificationMethod,
        isTwilioEnabled: Boolean) : ViewModel() {

    val result = repository.result

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
}

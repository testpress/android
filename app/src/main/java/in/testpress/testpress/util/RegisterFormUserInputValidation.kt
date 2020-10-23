package `in`.testpress.testpress.util

import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.enums.VerificationMethod
import android.view.View
import android.widget.TextView
import androidx.databinding.BaseObservable
import kotlinx.android.synthetic.main.register_activity.view.*

class RegisterFormUserInputValidation(
        private var binding: RegisterActivityBinding,
        private val verificationMethod: VerificationMethod,
        private val isTwilioEnabled: Boolean
) : BaseObservable() {

    private var isValid = true
    private val registerFormEmptyInputValidation = RegistrationFormEmptyInputValidation(binding,verificationMethod)

    fun isValid(): Boolean {
        isValid = true
        isValid = registerFormEmptyInputValidation.isValid()
        validateUserData()
        return isValid
    }

    private fun validateUserData() {
        ifUsernameNotValidSetError()
        ifEmailNotValidSetError()
        if (verificationMethod == VerificationMethod.MOBILE) {
            ifPhoneNumberNotValidSetError()
        }
        ifPasswordNotValidSetError()
        ifConfirmPasswordNotValidSetError()
    }

    private fun ifUsernameNotValidSetError() {
        val username = binding.editTextUsername.text.toString().trim()
        val isUsernameValid = Validator.isUsernameValid(username)
        if (username.isNotEmpty() && !isUsernameValid) {
            binding.editTextUsername.requestFocus()
            binding.editTextUsername.setText("")
            setErrorText(binding.usernameErrorText, "This field can contain only lowercase alphabets, numbers and underscore.", false)
        }
    }

    private fun ifEmailNotValidSetError() {
        val email = binding.editTextEmail.text.toString().trim()
        val isEmailValid = Validator.isEmailValid(email)
        if (email.isNotEmpty() && !isEmailValid) {
            binding.editTextEmail.requestFocus()
            binding.editTextEmail.setText("")
            setErrorText(binding.emailErrorText, "Please enter a valid email address", false)
        }
    }

    private fun ifPhoneNumberNotValidSetError() {
        val phoneNumber = binding.editTextPhone.text.toString().trim()
        val isPhoneNumberValid = if (isTwilioEnabled) {
            PhoneNumberValidator.validateInternationalPhoneNumber(binding.countryCodePicker)
        } else {
            PhoneNumberValidator.validatePhoneNumber(phoneNumber)
        }

        if (phoneNumber.trim().isNotEmpty() && !isPhoneNumberValid) {
            binding.editTextPhone.requestFocus()
            binding.editTextPhone.setText("")
            setErrorText(binding.phoneErrorText, "Please enter a valid mobile number", false)
        }
    }

    private fun ifPasswordNotValidSetError() {
        val password = binding.editTextPassword.text.toString().trim()
        if (password.isNotEmpty() && password.length < 6) {
            binding.editTextPassword.requestFocus()
            binding.editTextPassword.setText("")
            setErrorText(binding.passwordErrorText, "Password should contain at least 6 characters", false)
        }
    }

    private fun ifConfirmPasswordNotValidSetError() {
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
        if (confirmPassword.isNotEmpty() &&
                confirmPassword != binding.editTextPassword.text.toString()) {
            binding.editTextConfirmPassword.requestFocus()
            binding.editTextConfirmPassword.setText("")
            setErrorText(binding.confirmPasswordErrorText, "Passwords don\'t match", false)
        }
    }

    private fun setErrorText(errorTextView: TextView, errorText: String, isValid: Boolean) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
        if (!isValid) {
            this.isValid = false
        }
    }
}

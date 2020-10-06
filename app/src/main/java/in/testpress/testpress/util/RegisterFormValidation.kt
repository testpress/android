package `in`.testpress.testpress.util

import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.models.UserDetails
import android.view.View
import android.widget.TextView
import androidx.databinding.BaseObservable

class RegisterFormValidation(
        private val user: UserDetails,
        private val binding: RegisterActivityBinding,
        private val verificationMethod: VerificationMethod,
        private val isTwilioEnabled: Boolean
) : BaseObservable() {

    private var isValid = true

    fun isValid(): Boolean {
        checkAndSetEmptyError()
        verifyInput()
        return isValid
    }

    private fun checkAndSetEmptyError() {
        if (user.username.trim().isEmpty()) {
            setErrorText(binding.usernameErrorText, isValid = false)
        }
        if (user.password.trim().isEmpty()) {
            setErrorText(binding.passwordErrorText, isValid = false)
        }
        if (user.email.trim().isEmpty()) {
            setErrorText(binding.emailErrorText, isValid = false)
        }
        if (user.confirmPassword.trim().isEmpty()) {
            setErrorText(binding.confirmPasswordErrorText, isValid = false)
        }

        if ((user.phoneNumber.trim().isEmpty() && verificationMethod != VerificationMethod.EMAIL)) {
            setErrorText(binding.passwordErrorText, isValid = false)
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
        val isUsernameValid = Validator.isUsernameValid(user.username.trim())
        if (user.username.trim().isNotEmpty() && !isUsernameValid) {
            setErrorText(binding.usernameErrorText, "This field can contain only lowercase alphabets, numbers and underscore.", false)
            binding.editTextUsername.requestFocus()
        }
    }

    private fun verifyEmail() {
        val isEmailValid = Validator.isEmailValid(user.email.trim())
        if (user.email.trim().isNotEmpty() && !isEmailValid) {
            setErrorText(binding.emailErrorText, "Please enter a valid email address", false)
            binding.editTextEmail.requestFocus()
        }
    }

    private fun verifyPhoneNumber() {
        val isPhoneNumberValid = if (isTwilioEnabled) {
            PhoneNumberValidator.validateInternationalPhoneNumber(binding.countryCodePicker)
        } else {
            PhoneNumberValidator.validatePhoneNumber(user.phoneNumber.trim())
        }

        if (user.phoneNumber.trim().isNotEmpty() && !isPhoneNumberValid) {
            setErrorText(binding.phoneErrorText, "Please enter a valid mobile number", false)
            binding.editTextPhone.requestFocus()
        }
    }

    private fun verifyPassword() {
        if (user.password.trim().isNotEmpty() && user.password.trim().length < 6) {
            setErrorText(binding.passwordErrorText, "Password should contain at least 6 characters", false)
            binding.editTextPassword.requestFocus()
        }
    }

    private fun verifyConfirmPassword() {
        if (user.confirmPassword.trim().isNotEmpty() &&
                binding.editTextPassword.text.toString() != binding.editTextPassword.text.toString()) {
            setErrorText(binding.confirmPasswordErrorText, "Passwords don\'t match", false)
            binding.editTextConfirmPassword.requestFocus()
        }
    }

    private fun setErrorText(errorTextView: TextView, errorText: String = "This field cannot be empty.", isValid: Boolean?) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
        if (isValid == false) {
            this.isValid = isValid
        }
    }
}
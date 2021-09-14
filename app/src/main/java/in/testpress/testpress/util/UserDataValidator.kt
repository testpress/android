package `in`.testpress.testpress.util

import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.databinding.RegisterActivityBinding
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BaseObservable

class UserDataValidator(
        private var binding: RegisterActivityBinding,
        private val verificationMethod: VerificationMethod,
        private val isTwilioEnabled: Boolean
): BaseObservable() {
    private var isValid = true

    init {
        showErrorWhenNotValid()
    }

    fun isValid(): Boolean {
        return isValid
    }

    private fun showErrorWhenNotValid() {
        if (isEmpty(binding.editTextUsername)) {
            showError(binding.usernameErrorText, binding.editTextUsername)
        } else if (isUsernameNotValid()) {
            showError(binding.usernameErrorText, binding.editTextUsername,
                    "This field can contain only lowercase alphabets, numbers and underscore."
            )
        }
        if (isEmpty(binding.editTextEmail)) {
            showError(binding.emailErrorText, binding.editTextEmail)
        } else if (isEmailNotValid()) {
            showError(binding.emailErrorText, binding.editTextEmail,
                    "Please enter a valid email address")
        }

        if (isEmpty(binding.editTextPhone)) {
            showError(binding.phoneErrorText, binding.editTextEmail)
        } else if (verificationMethod == VerificationMethod.MOBILE) {
            if (isPhoneNumberNotValid()) {
                showError(binding.phoneErrorText, binding.editTextPhone,
                        "Please enter a valid mobile number")
            }
        }
        if (isEmpty(binding.editTextPassword)) {
            showError(binding.passwordErrorText, binding.editTextEmail)
        } else if (isPasswordNotValid()) {
            showError(binding.passwordErrorText, binding.editTextPassword,
                    "Password should contain at least 6 characters")
        }
        if (isConfirmPasswordNotValid()) {
            showError(binding.confirmPasswordErrorText, binding.editTextConfirmPassword,
                    "Passwords don\'t match")
        }
    }

    private fun isUsernameNotValid(): Boolean {
        val username = binding.editTextUsername.text.toString().trim()
        val isUsernameValid = Validator.isUsernameValid(username)
        if (username.isNotEmpty() && !isUsernameValid) {
            return true
        }
        return false
    }

    private fun isEmailNotValid(): Boolean{
        val email = binding.editTextEmail.text.toString().trim()
        val isEmailValid = Validator.isEmailValid(email)
        if (email.isNotEmpty() && !isEmailValid) {
            return true
        }
        return false
    }

    private fun isPhoneNumberNotValid(): Boolean {
        val phoneNumber = binding.editTextPhone.text.toString().trim()
        val isPhoneNumberValid = if (isTwilioEnabled) {
            PhoneNumberValidator.validateInternationalPhoneNumber(binding.countryCodePicker)
        } else {
            PhoneNumberValidator.validatePhoneNumber(phoneNumber)
        }

        if (phoneNumber.trim().isNotEmpty() && !isPhoneNumberValid) {
            return true
        }
        return false
    }

    private fun isPasswordNotValid(): Boolean {
        val password = binding.editTextPassword.text.toString().trim()
        if (password.isNotEmpty() && password.length < 6) {
            return true
        }
        return false
    }

    private fun isConfirmPasswordNotValid(): Boolean {
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
        if (confirmPassword.isNotEmpty() &&
                confirmPassword != binding.editTextPassword.text.toString()) {
            return true
        }
        return false
    }

    private fun isEmpty(editText: EditText): Boolean {
        return editText.text.toString().trim().isEmpty()
    }

    private fun showError(errorTextView: TextView, editText: EditText, errorText: String = "This field cannot be empty.") {
        editText.requestFocus()
        editText.setText("")
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
        isValid = false
    }
}

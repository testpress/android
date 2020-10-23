package `in`.testpress.testpress.util

import `in`.testpress.testpress.databinding.RegisterActivityBinding
import `in`.testpress.testpress.enums.VerificationMethod
import android.view.View
import android.widget.TextView
import androidx.databinding.BaseObservable

class RegistrationFormEmptyInputValidation(
        private val binding: RegisterActivityBinding,
        private val verificationMethod: VerificationMethod
): BaseObservable() {

    private var isValid: Boolean = true

    fun isValid(): Boolean {
        isValid = true
        ifEmptyInputSetInputError()
        return isValid
    }

    private fun ifEmptyInputSetInputError() {

        if (binding.editTextUsername.text.toString().trim().isEmpty()) {
            setErrorText(binding.usernameErrorText, isValid = false)
        }

        if (binding.editTextPassword.text.toString().trim().isEmpty()) {
            setErrorText(binding.passwordErrorText, isValid = false)
        }
        if (binding.editTextEmail.text.toString().trim().isEmpty()) {
            setErrorText(binding.emailErrorText, isValid = false)
        }
        if (verificationMethod == VerificationMethod.MOBILE) {
            if (binding.editTextPhone.text.toString().trim().isEmpty()) {
                setErrorText(binding.phoneErrorText, isValid = false)
            }
        }
        if (binding.editTextConfirmPassword.text.toString().trim().isEmpty()) {
            setErrorText(binding.confirmPasswordErrorText, isValid = false)
        }

        if ((binding.editTextPassword.text.toString().trim().isEmpty() && verificationMethod != VerificationMethod.EMAIL)) {
            setErrorText(binding.passwordErrorText, isValid = false)
        }
    }

    private fun setErrorText(errorTextView: TextView, errorText: String = "This field cannot be empty.", isValid: Boolean) {
        errorTextView.visibility = View.VISIBLE
        errorTextView.text = errorText
        this.isValid = isValid
    }
}

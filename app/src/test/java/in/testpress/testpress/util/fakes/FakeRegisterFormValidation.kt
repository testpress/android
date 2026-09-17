package `in`.testpress.testpress.util.fakes

import `in`.testpress.testpress.authenticator.RegisterActivity.VerificationMethod
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.Validator

class FakeRegisterFormValidation(private val user: UserDetails, private val verificationMethod: VerificationMethod) {

    private var isValid = true

    fun isValid(): Boolean {
        checkEmptyError()
        verifyInput()
        return isValid
    }

    private fun checkEmptyError() {
        if (user.username.trim().isEmpty()) {
            isValid = false
        }
        if (user.password.trim().isEmpty()) {
            isValid = false
        }
        if (user.email.trim().isEmpty()) {
            isValid = false
        }
        if (user.confirmPassword.trim().isEmpty()) {
            isValid = false
        }

        if (user.phoneNumber.trim().isEmpty()) {
            isValid = false
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
        if (!Validator.isUsernameValid(user.username.trim())) {
            isValid = false
        }
    }

    private fun verifyEmail() {
        val isEmailValid = Validator.isEmailValid(user.email.trim())
        if (user.email.trim().isEmpty() || !isEmailValid) {
            isValid = false
        }
    }

    private fun verifyPhoneNumber() {
        if (user.phoneNumber.trim().isEmpty()) {
            isValid = false
        }
    }

    private fun verifyPassword() {
        if (user.password.trim().isEmpty() || user.password.trim().length < 6) {
            isValid = false
        }
    }

    private fun verifyConfirmPassword() {
        if (user.confirmPassword.trim().isEmpty()) {
            isValid = false
        }
    }
}

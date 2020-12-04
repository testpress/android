package `in`.testpress.testpress.util.fakes

import `in`.testpress.testpress.enums.VerificationMethod
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.Validator

class FakeRegisterFormUserInputValidation(private val user: UserDetails, private val verificationMethod: VerificationMethod) {

    private var isValid = true

    fun isValid(): Boolean {
        verifyInput()
        return isValid
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

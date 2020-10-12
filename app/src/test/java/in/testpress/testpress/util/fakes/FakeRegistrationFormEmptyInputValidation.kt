package `in`.testpress.testpress.util.fakes

import `in`.testpress.testpress.models.UserDetails

class FakeRegistrationFormEmptyInputValidation(private val user: UserDetails) {

    private var isValid = true

    fun isValid(): Boolean {
        ifEmptyInputSetInputError()
        return isValid
    }

    private fun ifEmptyInputSetInputError() {
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
}

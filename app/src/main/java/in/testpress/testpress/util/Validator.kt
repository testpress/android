package `in`.testpress.testpress.util

import java.util.regex.Pattern

object Validator {

    fun isEmailValid(email: String?): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"
        val pat = Pattern.compile(emailRegex)
        return if (email == null) false else pat.matcher(email).matches()
    }

    fun isUsernameValid(username: String?): Boolean {
        val usernameRegex = "^[A-Za-z0-9_]*"
        val pattern = Pattern.compile(usernameRegex)
        return if (username == null) {
            false
        } else pattern.matcher(username).matches()
    }
}

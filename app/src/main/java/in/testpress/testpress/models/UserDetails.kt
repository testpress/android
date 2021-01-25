package `in`.testpress.testpress.models

data class UserDetails(
    var username: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var countryCode: String = ""
)

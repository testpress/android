package `in`.testpress.testpress.models.pojo

data class OTPLoginResponse(
    val token: String? = null,
    val isNewUser: Boolean = false,
    val nonFieldErrors: ArrayList<String> = arrayListOf()
)
package `in`.testpress.testpress.util.fakes

import `in`.testpress.enums.Status
import retrofit.RetrofitError
import java.io.IOException

class FakeRegisterRepository {

    var apiStatus = Status.SUCCESS
    var isRegisterSuccessful: Boolean = false
    var exception: Exception? = null

    fun register(userDetails: HashMap<String, String>) {
        if (apiStatus == Status.SUCCESS) {
            isRegisterSuccessful = true
        } else {
            isRegisterSuccessful = false
            exception = RetrofitError.networkError("", IOException("Something went wrong"))
        }
    }
}
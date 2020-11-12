package `in`.testpress.testpress.util.fakes

import `in`.testpress.enums.Status
import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import androidx.lifecycle.MutableLiveData
import retrofit.RetrofitError
import java.io.IOException

open class FakeRepository {

    var apiStatus = Status.SUCCESS
    lateinit var result: Resource<RegistrationSuccessResponse>
    lateinit var initializeResponse: Resource<Boolean>

    fun register(userDetails: HashMap<String, String>) {
        if (apiStatus == Status.SUCCESS) {
            result = Resource.success(RegistrationSuccessResponse())
        } else {
            result = Resource.error(IOException("Something went wrong"), null)
        }
    }

    fun initialize() {
        if (apiStatus == Status.SUCCESS) {
            initializeResponse = Resource.success(true)
        } else {
            initializeResponse = Resource.error(IOException("Something went wrong"), null)
        }
    }
}

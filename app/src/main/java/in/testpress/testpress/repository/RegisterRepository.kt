package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import `in`.testpress.testpress.util.SafeAsyncTask
import androidx.lifecycle.MutableLiveData

open class RegisterRepository(val testPressService: TestpressService) {

    var result = MutableLiveData<Resource<RegistrationSuccessResponse>>()

    var registrationSuccessResponse: RegistrationSuccessResponse? = null

    fun register(userDetails: HashMap<String, String>) {

        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                registrationSuccessResponse = testPressService.register(userDetails["username"],
                        userDetails["email"],
                        userDetails["password"],
                        userDetails["phone"],
                        userDetails["country_code"]
                )
                return true
            }

            override fun onException(e: Exception) {
                super.onException(e)
                result.value = Resource.error(e, null)
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                result.value = Resource.success(registrationSuccessResponse)
            }
        }.execute()
    }
}

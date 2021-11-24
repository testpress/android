package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import `in`.testpress.testpress.models.UserDetails
import `in`.testpress.testpress.util.SafeAsyncTask
import androidx.lifecycle.MutableLiveData

open class RegisterRepository(val testpressService: TestpressService) {

    var result = MutableLiveData<Resource<RegistrationSuccessResponse>>()

    var registrationSuccessResponse: RegistrationSuccessResponse? = null

    fun register(userDetails: UserDetails) {
        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                registrationSuccessResponse = testpressService.register(userDetails.username,
                        userDetails.email,
                        userDetails.password,
                        userDetails.phoneNumber,
                        userDetails.countryCode
                )
                return true
            }

            override fun onException(e: Exception) {
                super.onException(e)
                result.value = Resource.error(e, null)
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                result.postValue(Resource.success(registrationSuccessResponse))
            }
        }.execute()
    }
}

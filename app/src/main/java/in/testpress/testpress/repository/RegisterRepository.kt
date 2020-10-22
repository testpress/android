package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.Resource
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.RegistrationSuccessResponse
import `in`.testpress.testpress.util.SafeAsyncTask
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class RegisterRepository(val testpressService: TestpressService) {

    var result = MutableLiveData<Resource<RegistrationSuccessResponse>>()

    var registrationSuccessResponse: RegistrationSuccessResponse? = null

    fun register(userDetails: HashMap<String, String>) {

        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                try {
                    registrationSuccessResponse = testpressService.register(userDetails["username"],
                            userDetails["email"],
                            userDetails["password"],
                            userDetails["phone"],
                            userDetails["country_code"]
                    )
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        result.postValue(Resource.error(e, null))
                    }
                }
                return true
            }

            override fun onException(e: Exception) {
                super.onException(e)
                result.value = (Resource.error(e, null))
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                result.postValue(Resource.success(registrationSuccessResponse))
            }
        }.execute()
    }
}

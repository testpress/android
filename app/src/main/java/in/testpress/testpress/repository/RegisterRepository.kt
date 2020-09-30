package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.util.SafeAsyncTask
import androidx.lifecycle.MutableLiveData

open class RegisterRepository(val testpressService: TestpressService) {

    var isRegisterSuccessful = MutableLiveData<Boolean>()
    var exception: Exception? = null

    fun register(userDetails: HashMap<String, String>) {
        object : SafeAsyncTask<Boolean>() {
            override fun call(): Boolean {
                testpressService.register(userDetails["username"],
                        userDetails["email"],
                        userDetails["password"],
                        userDetails["phone"],
                        userDetails["country_code"]
                )
                return true
            }

            override fun onException(e: Exception?) {
                super.onException(e)
                exception = e
                isRegisterSuccessful.postValue(false)
            }

            override fun onSuccess(authSuccess: Boolean?) {
                super.onSuccess(authSuccess)
                isRegisterSuccessful.postValue(true)
            }
        }.execute()
    }
}
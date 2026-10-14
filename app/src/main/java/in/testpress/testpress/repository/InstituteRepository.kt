package `in`.testpress.testpress.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.pojo.GenerateOTPResponse
import `in`.testpress.testpress.models.pojo.OTPLoginResponse
import `in`.testpress.testpress.network.AppNetwork
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonSyntaxException
import java.io.IOException


class InstituteRepository(val context: Context, val testpressService: TestpressService) {
    private val service = AppNetwork(context)

    fun getInstituteSettings(
        forceRefresh: Boolean = false
    ): LiveData<Resource<InstituteSettings>> {
        return object : NetworkBoundResource<InstituteSettings, InstituteSettings>() {
            override fun saveNetworkResponseToDB(item: InstituteSettings) {
                val instituteSettingsDao = TestpressApplication.getDaoSession().instituteSettingsDao
                item.baseUrl = BuildConfig.BASE_URL
                instituteSettingsDao.insertOrReplace(item)
            }

            override fun shouldFetch(data: InstituteSettings?): Boolean {
                return forceRefresh || InstituteSettings.getInstance() == null
            }

            override fun loadFromDb(): LiveData<InstituteSettings> {
                val liveData = MutableLiveData<InstituteSettings>()
                liveData.postValue(InstituteSettings.getInstance())
                return liveData
            }

            override fun createCall(): RetrofitCall<InstituteSettings> {
                return service.getInstituteSettings()
            }
        }.asLiveData()
    }

    fun requestOTP(phoneNumber: Long, countryCode: String): LiveData<Resource<GenerateOTPResponse>> {
        val liveData = MutableLiveData<Resource<GenerateOTPResponse>>()
        service.requestOTP(phoneNumber, countryCode)
            .enqueue(object : TestpressCallback<Void>(){
                override fun onSuccess(result: Void?) {
                    val resource = Resource.success(null)
                    liveData.postValue(resource)
                }

                override fun onException(exception: TestpressException?) {
                    val data = exception?.getErrorBodyAs(
                        exception.response, GenerateOTPResponse::class.java
                    )
                    val resource = Resource.error(exception ?: TestpressException.unexpectedError(IOException()), data)
                    liveData.postValue(resource)
                }

            })
        return liveData
    }

    fun verifyOTP(otp: Int, phoneNumber: Long): LiveData<Resource<OTPLoginResponse>> {
        val liveData = MutableLiveData<Resource<OTPLoginResponse>>()
        service.verifyOTP(otp, phoneNumber)
            .enqueue(object: TestpressCallback<OTPLoginResponse>() {
                override fun onSuccess(result: OTPLoginResponse?) {
                    liveData.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException?) {
                    val data:OTPLoginResponse? = try {
                        exception?.getErrorBodyAs(
                            exception.response, OTPLoginResponse::class.java
                        )
                    } catch (e: JsonSyntaxException) {
                        null
                    }
                    val resource = Resource.error(exception ?: TestpressException.unexpectedError(IOException()), data)
                    liveData.postValue(resource)
                }

            })
        return liveData
    }
}
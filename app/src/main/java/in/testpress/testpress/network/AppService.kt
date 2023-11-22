package `in`.testpress.testpress.network

import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.core.Constants
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.testpress.models.pojo.GenerateOTPResponse
import `in`.testpress.testpress.models.pojo.OTPLoginResponse
import android.content.Context
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.collections.HashMap

const val OTP_URL = "/api/v2.5/auth/generate-otp/"
const val VERIFY_OTP_URL = "/api/v2.5/auth/otp-login/"

interface AppService {
    @GET(Constants.Http.URL_INSTITUTE_SETTINGS_FRAG)
    fun getInstituteSettings(): RetrofitCall<InstituteSettings>

    @POST(OTP_URL)
    fun requestOTP(@Body arguments: HashMap<String, Any>): RetrofitCall<Void>

    @POST(VERIFY_OTP_URL)
    fun verifyOTP(@Body arguments: HashMap<String, Any>): RetrofitCall<OTPLoginResponse>

}

class AppNetwork(context: Context) : TestpressApiClient(BuildConfig.BASE_URL, context) {
    private fun getAppService() = retrofit.create(AppService::class.java)

    fun getInstituteSettings(): RetrofitCall<InstituteSettings> {
        return getAppService().getInstituteSettings()
    }

    fun requestOTP(phoneNumber: Long, countryCode: String): RetrofitCall<Void> {
        val data = hashMapOf<String, Any>()
        data["phone_number"] = phoneNumber
        data["country_code"] = countryCode
        return getAppService().requestOTP(data)
    }

    fun verifyOTP(otp: Int, phoneNumber: Long): RetrofitCall<OTPLoginResponse> {
        val data = hashMapOf<String, Any>()
        data["phone_number"] = phoneNumber
        data["otp"] = otp
        return getAppService().verifyOTP(data)
    }
}
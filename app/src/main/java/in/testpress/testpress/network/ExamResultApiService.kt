package `in`.testpress.testpress.network

import android.annotation.SuppressLint
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import `in`.testpress.testpress.models.pojo.ExamResultResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val EXAM_RESULT_BASE_URL = "https://65.108.62.51/"
const val EXAM_RESULT_API_TOKEN = "8ee49eb9f9e3477aa36d209657024cab"

object ExamType {
    const val MODEL = "Model"
    const val WEEKLY = "Weekly"
}


interface ExamResultApiService {

    @FormUrlEncoded
    @POST("studentexamapi")
    fun fetchExamResults(
        @Field("token") token: String,
        @Field("studentno") studentNo: String,
        @Field("pageno") pageNo: Int,
        @Field("limit") limit: Int,
        @Field("examtype") examType: String
    ): Call<ExamResultResponse>
}

object ExamResultApiClient {

    val service: ExamResultApiService by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(String::class.java, AnyToStringDeserializer())
            .create()

        Retrofit.Builder()
            .baseUrl(EXAM_RESULT_BASE_URL)
            .client(buildTrustingOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ExamResultApiService::class.java)
    }

    @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
    private fun buildTrustingOkHttpClient(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAll, SecureRandom())
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private class AnyToStringDeserializer : JsonDeserializer<String?> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): String? {
            if (json.isJsonNull) return null
            val primitive = json.asJsonPrimitive
            return when {
                primitive.isString -> primitive.asString
                primitive.isNumber -> {
                    val num = primitive.asDouble
                    if (num == num.toLong().toDouble()) num.toLong().toString() else num.toString()
                }
                else -> json.asString
            }
        }
    }
}

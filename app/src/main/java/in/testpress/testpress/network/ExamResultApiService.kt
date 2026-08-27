package `in`.testpress.testpress.network

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
import java.util.concurrent.TimeUnit

private const val EXAM_RESULT_BASE_URL = "https://admissions.brilliantpala.org/studentexamapi/"
const val EXAM_RESULT_API_TOKEN = "8ee49eb9f9e3477aa36d209657024cab"

object ExamType {
    const val MODEL = "Model"
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

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(EXAM_RESULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ExamResultApiService::class.java)
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

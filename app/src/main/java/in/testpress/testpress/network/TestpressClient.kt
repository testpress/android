package `in`.testpress.testpress.network

import `in`.testpress.testpress.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TestpressClient {
    companion object {
        private var retrofit: Retrofit? = null

        fun getClient(): Retrofit {
            when (retrofit) {
                null -> retrofit = Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit as Retrofit
        }
    }
}
package `in`.testpress.testpress.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.exam.network.ExamService
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.testpress.core.Constants.Http.URL_FORUMS_FRAG
import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.models.TestpressApiResponse
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

@JvmSuppressWildcards
interface TestpressAPIService {
    @GET(URL_FORUMS_FRAG)
    fun fetchPosts(
        @QueryMap options: Map<String, Any>
    ): RetrofitCall<TestpressApiResponse<Forum>>
}

class APIClient(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(TestpressAPIService::class.java)


    fun getPosts(queryParams: Map<String, Any>): RetrofitCall<TestpressApiResponse<Forum>> {
        return getService().fetchPosts(queryParams)
    }
}

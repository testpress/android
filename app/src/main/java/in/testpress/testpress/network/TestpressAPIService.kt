package `in`.testpress.testpress.network

import `in`.testpress.network.RetrofitCall
import `in`.testpress.testpress.core.Constants.Http.URL_FORUMS_FRAG
import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.models.TestpressApiResponse
import android.content.Context
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap


@JvmSuppressWildcards
interface TestpressAPIService {
    @GET(URL_FORUMS_FRAG)
    suspend fun fetchPosts(
            @QueryMap options: Map<String, Any>
    ): Response<TestpressApiResponse<Forum>>
}

class APIClient(context: Context) {
    val retrofit = TestpressClient.getClient()
    private fun getService() = retrofit.create(TestpressAPIService::class.java)

    suspend fun getPosts(queryParams: Map<String, Any>): Response<TestpressApiResponse<Forum>> {
        return getService().fetchPosts(queryParams)
    }
}
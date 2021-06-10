package `in`.testpress.testpress.repository

import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.network.APIClient
import `in`.testpress.testpress.util.SafeAsyncTask
import android.accounts.OperationCanceledException
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.jvm.Throws

class DiscussionPageSource(private val apiClient: APIClient): PagingSource<Int, Forum>() {
    override fun getRefreshKey(state: PagingState<Int, Forum>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Forum> {
        val queryParams: Map<String, String> = LinkedHashMap()
        Log.d("TAG", "load: ")


        try {
            // Start refresh at page 1 if undefined.
            val nextPage: Int = params.key ?: 1
            val response = apiClient.getPosts(queryParams).execute().body()
            Log.d("TAG", "load: ${response.results}")
            return LoadResult.Page(
                    data = response.results,
                    prevKey = if (nextPage == 1) null else nextPage - 1,
                    nextKey = nextPage + 1
            )
        } catch (e: Exception) {
            e.stackTrace
            Log.d("TAG", "Error: ${e}")
            return LoadResult.Error(e)
        }
    }

}
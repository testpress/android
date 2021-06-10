package `in`.testpress.testpress.repository

import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.network.APIClient
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

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
            val response = apiClient.getPosts(queryParams)
            val results:List<Forum> = response.body()?.results as List<Forum>
            Log.d("TAG", "load: ${results}")
            return LoadResult.Page(
                    data = results,
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
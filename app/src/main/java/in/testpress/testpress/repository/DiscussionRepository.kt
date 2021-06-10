package `in`.testpress.testpress.repository

import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.network.APIClient
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class DiscussionRepository(private val testpressService: APIClient) {

    fun fetchDiscussions(): Flow<PagingData<Forum>> {
        Log.d("TAG", "fetchDiscussions: ")
        return Pager(
                PagingConfig(pageSize = 40, enablePlaceholders = true)
        ) {
            DiscussionPageSource(testpressService)
        }.flow
    }
}
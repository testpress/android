package `in`.testpress.testpress.ui

import `in`.testpress.testpress.models.Forum
import `in`.testpress.testpress.network.APIClient
import `in`.testpress.testpress.repository.DiscussionRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class DiscussionViewModel(application: Application) : AndroidViewModel(application) {
    private val service = APIClient(application)
    private val repository = DiscussionRepository(service)

    fun fetchPosts(): Flow<PagingData<Forum>> {
        return repository.fetchDiscussions().cachedIn(viewModelScope)
    }
}

class DiscussionViewModelFactory(
        private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            DiscussionViewModel(application) as T
}
package `in`.testpress.testpress.viewmodel

import `in`.testpress.models.DomainDiscussionThreadAnswer
import `in`.testpress.network.Resource
import `in`.testpress.testpress.repository.ForumAnswerRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class DiscussionAnswerViewModel(val repository: ForumAnswerRepository): ViewModel() {
    fun getDiscussionAnswer(id: Long): LiveData<Resource<DomainDiscussionThreadAnswer>> {
        return repository.loadDiscussionAnswer(id)
    }
}
package `in`.testpress.testpress.viewmodel

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.models.DomainDiscussionAnswer
import `in`.testpress.models.NetworkDiscussionAnswer
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.testpress.repository.ForumAnswerRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class DiscussionAnswerViewModel(val repository: ForumAnswerRepository): ViewModel() {
    fun getDiscussionAnswer(id: Long): LiveData<Resource<DomainDiscussionAnswer>> {
        return repository.loadDiscussionAnswer(id)
    }
}
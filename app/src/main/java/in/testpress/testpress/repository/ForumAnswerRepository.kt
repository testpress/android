package `in`.testpress.testpress.repository

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.models.DomainDiscussionAnswer
import `in`.testpress.models.NetworkDiscussionAnswer
import `in`.testpress.models.asDatabaseModel
import `in`.testpress.models.asDomainModel
import `in`.testpress.network.APIClient
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

class ForumAnswerRepository(val context: Context) {
    val discussionAnswerDao = TestpressDatabase(context).discussionAnswerDao()
    private val service = APIClient(context)

    fun loadDiscussionAnswer(forumId: Long): LiveData<Resource<DomainDiscussionAnswer>> {
        return object : NetworkBoundResource<DomainDiscussionAnswer, NetworkDiscussionAnswer>() {
            override fun createCall(): RetrofitCall<NetworkDiscussionAnswer> {
                return service.getDiscussionAnswer(forumId)
            }

            override fun loadFromDb(): LiveData<DomainDiscussionAnswer> {
                return Transformations.map(discussionAnswerDao.getByForumId(forumId)) {
                    it?.asDomainModel()
                }
            }

            override fun saveNetworkResponseToDB(item: NetworkDiscussionAnswer) {
                discussionAnswerDao.insert(item.asDatabaseModel())
            }

            override fun shouldFetch(data: DomainDiscussionAnswer?): Boolean {
                return true
            }

        }.asLiveData()
    }
}
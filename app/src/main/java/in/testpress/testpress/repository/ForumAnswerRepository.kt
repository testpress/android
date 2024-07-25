package `in`.testpress.testpress.repository

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.models.*
import `in`.testpress.network.APIClient
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class ForumAnswerRepository(val context: Context) {
    val discussionAnswerDao = TestpressDatabase(context).discussionAnswerDao()
    private val service = APIClient(context)

    fun loadDiscussionAnswer(forumId: Long): LiveData<Resource<DomainDiscussionThreadAnswer>> {
        return object : NetworkBoundResource<DomainDiscussionThreadAnswer, NetworkDiscussionThreadAnswer>() {
            override fun createCall(): RetrofitCall<NetworkDiscussionThreadAnswer> {
                return service.getDiscussionAnswer(forumId)
            }

            override fun loadFromDb(): LiveData<DomainDiscussionThreadAnswer> {
                return discussionAnswerDao.getByForumId(forumId).map { it?.asDomainModel()!! }
            }

            override fun saveNetworkResponseToDB(item: NetworkDiscussionThreadAnswer) {
                discussionAnswerDao.insert(item.asDatabaseModel())
            }

            override fun shouldFetch(data: DomainDiscussionThreadAnswer?): Boolean {
                return true
            }

        }.asLiveData()
    }
}
package `in`.testpress.testpress.repository

import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.network.AppNetwork
import `in`.testpress.testpress.util.PreferenceManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder

class DashBoardRepository(
    val context: Context
) {
    private val service = AppNetwork(context)

    fun loadData(): LiveData<Resource<DashboardResponse>> {
        return object : NetworkBoundResource<DashboardResponse, DashboardResponse>() {

            override fun loadFromDb(): LiveData<DashboardResponse> {
                val liveData = MutableLiveData<DashboardResponse>()
                liveData.postValue(
                    PreferenceManager.getDashboardDataPreferences(context)
                )
                return liveData
            }

            override fun saveNetworkResponseToDB(item: DashboardResponse) {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(item)
                PreferenceManager.setDashboardData(context, json)
            }

            override fun shouldFetch(data: DashboardResponse?): Boolean {
                return true
            }

            override fun createCall(): RetrofitCall<DashboardResponse> {
                return service.getDashBoardData()
            }
        }.asLiveData()
    }

}
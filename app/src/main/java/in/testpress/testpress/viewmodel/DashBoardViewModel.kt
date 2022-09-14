package `in`.testpress.testpress.viewmodel

import `in`.testpress.network.Resource
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.repository.DashBoardRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class DashBoardViewModel(
    private val dashBoardRepository: DashBoardRepository
) : ViewModel() {

    fun loadData(): LiveData<Resource<DashboardResponse>> {
        return dashBoardRepository.loadData()
    }

}


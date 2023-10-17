package `in`.testpress.testpress.viewmodel

import `in`.testpress.network.Resource
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.repository.DashBoardRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner


class DashBoardViewModel(
    private val dashBoardRepository: DashBoardRepository
) : ViewModel() {

    fun loadData(): LiveData<Resource<DashboardResponse>> {
        return dashBoardRepository.loadData()
    }

    companion object {
        fun initializeViewModel(owner: ViewModelStoreOwner, repository: DashBoardRepository):DashBoardViewModel{
            return ViewModelProvider(owner, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashBoardViewModel(repository) as T
                }
            }).get(DashBoardViewModel::class.java)
        }
    }

}


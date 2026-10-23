package `in`.testpress.testpress.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import `in`.testpress.testpress.models.pojo.PerformanceDashboardData
import `in`.testpress.testpress.models.pojo.PerformanceDataRequest
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFiltersRequest
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceOverviewData
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import `in`.testpress.testpress.repository.PerformanceAnalysisRepository
import `in`.testpress.testpress.repository.PerformanceApiState

class PerformanceAnalysisViewModel : ViewModel() {

    private val repository = PerformanceAnalysisRepository()

    private val _filtersState =
        MutableLiveData<PerformanceApiState<PerformanceFiltersData>>()
    val filtersState: LiveData<PerformanceApiState<PerformanceFiltersData>> = _filtersState

    private val _overviewState =
        MutableLiveData<PerformanceApiState<PerformanceOverviewData>>()
    val overviewState: LiveData<PerformanceApiState<PerformanceOverviewData>> = _overviewState

    private val _dashboardState =
        MutableLiveData<PerformanceApiState<PerformanceDashboardData>>()
    val dashboardState: LiveData<PerformanceApiState<PerformanceDashboardData>> = _dashboardState

    private val _leaderboardState =
        MutableLiveData<PerformanceApiState<PerformanceLeaderboardData>>()
    val leaderboardState: LiveData<PerformanceApiState<PerformanceLeaderboardData>> =
        _leaderboardState

    private val _subjectBoardState =
        MutableLiveData<PerformanceApiState<PerformanceSubjectBoardData>>()
    val subjectBoardState: LiveData<PerformanceApiState<PerformanceSubjectBoardData>> =
        _subjectBoardState

    private val _historyState =
        MutableLiveData<PerformanceApiState<PerformanceHistoryData>>()
    val historyState: LiveData<PerformanceApiState<PerformanceHistoryData>> = _historyState

    fun loadFilters(request: PerformanceFiltersRequest) {
        repository.fetchFilters(request) { _filtersState.value = it }
    }

    fun loadOverview(request: PerformanceDataRequest) {
        repository.fetchOverview(request) { _overviewState.value = it }
    }

    fun loadDashboard(request: PerformanceDataRequest) {
        repository.fetchDashboard(request) { _dashboardState.value = it }
    }

    fun loadLeaderboard(request: PerformanceDataRequest) {
        repository.fetchLeaderboard(request) { _leaderboardState.value = it }
    }

    fun loadSubjectBoard(request: PerformanceDataRequest) {
        repository.fetchSubjectBoard(request) { _subjectBoardState.value = it }
    }

    fun loadHistory(request: PerformanceDataRequest) {
        repository.fetchHistory(request) { _historyState.value = it }
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelAll()
    }
}

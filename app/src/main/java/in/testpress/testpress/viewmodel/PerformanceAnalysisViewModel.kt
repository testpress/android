package `in`.testpress.testpress.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import `in`.testpress.testpress.ui.utils.PerformanceFormatter

class PerformanceAnalysisViewModel(
    private val repository: PerformanceAnalysisRepository
) : ViewModel() {

    var studentNo: String = ""
        private set

    var cachedFiltersData: PerformanceFiltersData? = null
        private set

    var selectedAcademicYear: Int? = null
        private set

    var selectedTabType: String? = null

    var currentHistoryPage: Int = 1
        private set

    var totalHistoryPages: Int = 1
        private set

    var historyPageSize: Int = 5
        private set

    var trendSubjectKeys: List<String> = emptyList()
        private set

    var leaderboardSubjectKeys: List<String> = emptyList()
        private set

    var subjectBoardSubjectKeys: List<String> = emptyList()
        private set

    var historySubjectKeys: List<String> = emptyList()
        private set

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

    fun setStudent(studentNumber: String) {
        studentNo = studentNumber
    }

    fun onFiltersLoaded(data: PerformanceFiltersData) {
        cachedFiltersData = data
        selectedAcademicYear = data.year?.numeric?.toInt()
            ?: data.year?.raw?.toIntOrNull()
            ?: data.years.firstOrNull()

        if (data.pageSize > 0) {
            historyPageSize = data.pageSize
        }

        val subjectKeys = data.subjects.map { it.key }
        trendSubjectKeys = subjectKeys
        leaderboardSubjectKeys = subjectKeys
        subjectBoardSubjectKeys = subjectKeys
        historySubjectKeys = subjectKeys
    }

    fun requestFilters(type: String? = null) {
        if (studentNo.isBlank()) return
        selectedAcademicYear = null
        selectedTabType = type
        repository.fetchFilters(PerformanceFiltersRequest(studentNo = studentNo, type = type)) {
            _filtersState.value = it
        }
    }

    fun requestOverview() {
        if (studentNo.isBlank()) return
        val activeType = cachedFiltersData?.type ?: selectedTabType
        val activeYear = selectedAcademicYear
            ?: cachedFiltersData?.year?.numeric?.toInt()
            ?: cachedFiltersData?.year?.raw?.toIntOrNull()
            ?: cachedFiltersData?.years?.firstOrNull()

        repository.fetchOverview(
            PerformanceDataRequest(
                studentNo = studentNo,
                type = activeType,
                year = activeYear
            )
        ) { _overviewState.value = it }
    }

    fun requestDashboard(monthPos: Int, yearPos: Int, subjectPos: Int) {
        if (studentNo.isBlank()) return

        val selectedMonth = if (monthPos > 0) cachedFiltersData?.months?.getOrNull(monthPos - 1) else null
        val selectedYear = if (yearPos > 0) cachedFiltersData?.years?.getOrNull(yearPos - 1) else null
        val selectedSubject = if (subjectPos > 0) trendSubjectKeys.getOrNull(subjectPos - 1) else PerformanceFormatter.DEFAULT_SUBJECT_KEY

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedTabType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject
        )
        repository.fetchDashboard(request) { _dashboardState.value = it }
    }

    fun requestLeaderboard(monthPos: Int, yearPos: Int, limitPos: Int, subjectPos: Int) {
        if (studentNo.isBlank()) return

        val selectedMonth = if (monthPos > 0) cachedFiltersData?.months?.getOrNull(monthPos - 1) else null
        val selectedYear = if (yearPos > 0) cachedFiltersData?.years?.getOrNull(yearPos - 1) else null
        val selectedLimit = when (limitPos) {
            0 -> 5
            1 -> 10
            2 -> 20
            3 -> null
            else -> 5
        }
        val selectedSubject = if (subjectPos > 0) leaderboardSubjectKeys.getOrNull(subjectPos - 1) else PerformanceFormatter.DEFAULT_SUBJECT_KEY

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedTabType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            limit = selectedLimit
        )
        repository.fetchLeaderboard(request) { _leaderboardState.value = it }
    }

    fun requestSubjectBoard(monthPos: Int, yearPos: Int, limitPos: Int, subjectPos: Int) {
        if (studentNo.isBlank()) return

        val selectedMonth = if (monthPos > 0) cachedFiltersData?.months?.getOrNull(monthPos - 1) else null
        val selectedYear = if (yearPos > 0) cachedFiltersData?.years?.getOrNull(yearPos - 1) else null
        val selectedLimit = when (limitPos) {
            0 -> 5
            1 -> 10
            2 -> 20
            3 -> null
            else -> 5
        }
        val selectedSubject = if (subjectPos > 0) subjectBoardSubjectKeys.getOrNull(subjectPos - 1) else PerformanceFormatter.DEFAULT_SUBJECT_KEY

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedTabType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            limit = selectedLimit
        )
        repository.fetchSubjectBoard(request) { _subjectBoardState.value = it }
    }

    fun requestHistoryPage(page: Int, monthPos: Int, yearPos: Int, subjectPos: Int) {
        if (studentNo.isBlank()) return

        val selectedMonth = if (monthPos > 0) cachedFiltersData?.months?.getOrNull(monthPos - 1) else null
        val selectedYear = if (yearPos > 0) cachedFiltersData?.years?.getOrNull(yearPos - 1) else null
        val selectedSubject = if (subjectPos > 0) historySubjectKeys.getOrNull(subjectPos - 1) else PerformanceFormatter.DEFAULT_SUBJECT_KEY

        currentHistoryPage = page
        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedTabType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            page = page,
            limit = historyPageSize,
            length = historyPageSize
        )
        repository.fetchHistory(request) { _historyState.value = it }
    }

    fun updateHistoryPagination(history: PerformanceHistoryData?) {
        if (history != null && history.length > 0) {
            historyPageSize = history.length
        }
        val total = history?.total ?: 0
        val pageSize = if (historyPageSize > 0) historyPageSize else 5
        val serverPages = history?.pages ?: 0
        totalHistoryPages = if (serverPages > 0) {
            serverPages
        } else if (total > 0) {
            ((total + pageSize - 1) / pageSize).coerceAtLeast(1)
        } else {
            1
        }
        val page = history?.page ?: currentHistoryPage
        currentHistoryPage = page.coerceIn(1, totalHistoryPages)
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelAll()
    }
}

class PerformanceViewModelFactory(
    private val repository: PerformanceAnalysisRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerformanceAnalysisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerformanceAnalysisViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

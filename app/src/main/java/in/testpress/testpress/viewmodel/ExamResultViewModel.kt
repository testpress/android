package `in`.testpress.testpress.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import `in`.testpress.testpress.models.pojo.ExamResult
import `in`.testpress.testpress.network.ExamType
import `in`.testpress.testpress.repository.ExamResultRepository
import `in`.testpress.testpress.repository.ExamResultState
import kotlin.math.ceil


class ExamResultViewModel : ViewModel() {

    private val repository = ExamResultRepository()

    private val _results = MutableLiveData<List<ExamResult>>()
    val results: LiveData<List<ExamResult>> = _results

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData<Int>(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _examType = MutableLiveData<String>(ExamType.MODEL)
    val examType: LiveData<String> = _examType


    private var studentNo: String = ""
    private var pageLimit: Int = ExamResultRepository.FALLBACK_LIMIT

    /**
     * Must be called once with the logged-in student's username before any fetch.
     * @param limit Number of rows that fit on the current screen (calculated by the Fragment).
     */
    fun initialize(studentNo: String, limit: Int = ExamResultRepository.FALLBACK_LIMIT) {
        if (this.studentNo.isEmpty()) {
            this.studentNo = studentNo
            this.pageLimit = limit
            fetchResults()
        }
    }

    fun updatePageLimit(limit: Int) {
        if (pageLimit == limit) return
        pageLimit = limit
        _currentPage.value = 1
        fetchResults()
    }

    fun isInitialized(): Boolean = studentNo.isNotEmpty()

    fun changeExamType(examType: String) {
        if (_examType.value == examType) return
        _examType.value = examType
        _currentPage.value = 1
        fetchResults()
    }

    fun goToPage(page: Int) {
        val total = _totalPages.value ?: 1
        if (page < 1 || page > total) return
        _currentPage.value = page
        fetchResults()
    }

    fun goToPreviousPage() {
        val current = _currentPage.value ?: 1
        if (current > 1) goToPage(current - 1)
    }

    fun goToNextPage() {
        val current = _currentPage.value ?: 1
        val total = _totalPages.value ?: 1
        if (current < total) goToPage(current + 1)
    }

    fun retry() = fetchResults()


    override fun onCleared() {
        super.onCleared()
        repository.cancelPendingRequests()
    }

    private fun fetchResults() {
        if (studentNo.isEmpty()) {
            _isLoading.value = false
            _error.value = "Student details not found. Please log in again."
            _results.value = emptyList()
            return
        }

        repository.fetchResults(
            studentNo = studentNo,
            examType = _examType.value ?: ExamType.MODEL,
            pageNo = _currentPage.value ?: 1,
            limit = pageLimit,
            onResult = { state ->
                when (state) {
                    is ExamResultState.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is ExamResultState.Success -> {
                        _isLoading.value = false
                        val dataList = state.response.data ?: emptyList()
                        _results.value = if (pageLimit > 0) dataList.take(pageLimit) else dataList
                        val pages = if (state.response.limit > 0) {
                            ceil(state.response.totalCount.toDouble() / state.response.limit).toInt()
                                .coerceAtLeast(1)
                        } else 1
                        _totalPages.value = pages
                    }
                    is ExamResultState.Error -> {
                        _isLoading.value = false
                        _error.value = state.message
                        _results.value = emptyList()
                    }
                }
            }
        )
    }
}

package `in`.testpress.testpress.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.core.text.HtmlCompat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import androidx.lifecycle.ViewModelProvider
import com.github.testpress.mikephil.charting.charts.LineChart
import com.github.testpress.mikephil.charting.components.AxisBase
import com.github.testpress.mikephil.charting.components.XAxis
import com.github.testpress.mikephil.charting.data.Entry
import com.github.testpress.mikephil.charting.data.LineData
import com.github.testpress.mikephil.charting.data.LineDataSet
import com.github.testpress.mikephil.charting.formatter.AxisValueFormatter
import com.github.testpress.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.tabs.TabLayout
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.BuildConfig.APPLICATION_ID
import `in`.testpress.testpress.R
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.databinding.ActivityPerformanceResultsBinding
import `in`.testpress.testpress.databinding.ItemPaHistoryRowBinding
import `in`.testpress.testpress.databinding.ItemPaTopperRowBinding
import `in`.testpress.testpress.models.pojo.PerformanceDataRequest
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFiltersRequest
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceHistoryRow
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardRow
import `in`.testpress.testpress.models.pojo.PerformanceRank
import `in`.testpress.testpress.models.pojo.PerformanceSummary
import `in`.testpress.testpress.models.pojo.PerformanceTrend
import `in`.testpress.testpress.network.PerformanceAnalysisApiClient
import `in`.testpress.testpress.repository.PerformanceApiState
import android.view.ViewGroup
import android.graphics.Typeface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TableRow
import `in`.testpress.testpress.models.pojo.PerformanceFocus
import `in`.testpress.testpress.models.pojo.PerformanceFocusPoint
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardRow
import `in`.testpress.testpress.models.pojo.PerformanceSubjectColumn
import java.text.NumberFormat
import java.util.Locale
import `in`.testpress.testpress.viewmodel.PerformanceAnalysisViewModel

class PerformanceResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerformanceResultsBinding
    private lateinit var viewModel: PerformanceAnalysisViewModel
    private var studentNo: String = ""
    private var tabSelectedListener: TabLayout.OnTabSelectedListener? = null
    private var cachedFiltersData: PerformanceFiltersData? = null
    private var selectedAcademicYear: Int? = null
    private var currentHistoryPage = 1
    private var totalHistoryPages = 1
    private var historyPageSize = 5
    private var historySubjectKeys = listOf<String>()
    private var leaderboardSubjectKeys = listOf<String>()
    private var subjectBoardSubjectKeys = listOf<String>()
    private var trendSubjectKeys = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        studentNo = getStudentNo()
        viewModel = ViewModelProvider(this)[PerformanceAnalysisViewModel::class.java]

        setupTestTabs()
        setupTrendChart()
        setupTrendFilters()
        setupLeaderboard()
        setupSubjectBoard()
        setupHistory()
        observeStates()

        binding.resultModalCloseBtn.setOnClickListener {
            binding.notifymessageModal.visibility = View.GONE
        }
        binding.modalBackdrop.setOnClickListener {
            binding.notifymessageModal.visibility = View.GONE
        }

        if (studentNo.isNotBlank()) {
            requestFilters()
        } else {
            Toast.makeText(this, R.string.performance_results_no_student, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tabSelectedListener?.let { binding.testTabs.removeOnTabSelectedListener(it) }
        tabSelectedListener = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_actionbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.performance_results_title)
        }
    }

    private fun setupTestTabs() {
        val defaultTypes = listOf(
            getString(R.string.performance_tab_model),
            getString(R.string.performance_tab_weekly),
            getString(R.string.performance_tab_screening_test)
        )
        populateTabs(defaultTypes, defaultTypes.firstOrNull())

        tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val selectedType = tab.text?.toString() ?: return
                onTabTypeSelected(selectedType)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        }
        tabSelectedListener?.let { binding.testTabs.addOnTabSelectedListener(it) }
    }

    private fun populateTabs(types: List<String>, selectedType: String?) {
        if (types.isEmpty()) return

        tabSelectedListener?.let { binding.testTabs.removeOnTabSelectedListener(it) }

        val currentTabCount = binding.testTabs.tabCount
        var needsRecreation = currentTabCount != types.size
        if (!needsRecreation) {
            for (i in 0 until currentTabCount) {
                if (binding.testTabs.getTabAt(i)?.text?.toString() != types[i]) {
                    needsRecreation = true
                    break
                }
            }
        }

        if (needsRecreation) {
            binding.testTabs.removeAllTabs()
            types.forEach { type ->
                val tab = binding.testTabs.newTab().setText(type)
                binding.testTabs.addTab(tab)
            }
        }

        val activeType = selectedType ?: types.firstOrNull()
        val activeIndex = types.indexOf(activeType).takeIf { it >= 0 } ?: 0
        binding.testTabs.getTabAt(activeIndex)?.select()
        reorderSectionsForTab()

        tabSelectedListener?.let { binding.testTabs.addOnTabSelectedListener(it) }
    }

    private fun onTabTypeSelected(type: String) {
        val isScreening = type.equals(getString(R.string.performance_tab_screening_test), ignoreCase = true)

        binding.paStats.visibility = if (isScreening) View.GONE else View.VISIBLE
        binding.trendCard.visibility = if (isScreening) View.GONE else View.VISIBLE
        binding.rankCard.visibility = if (isScreening) View.GONE else View.VISIBLE
        binding.topperCard.visibility = if (isScreening) View.GONE else View.VISIBLE
        binding.subjectCard.visibility = if (isScreening) View.GONE else View.VISIBLE
        binding.focusCard.visibility = if (isScreening) View.GONE else View.VISIBLE

        reorderSectionsForTab()
        requestFilters(type)
    }

    private fun reorderSectionsForTab() {
        val container = binding.paContentContainer
        val historyCard = binding.historyCard
        val historyIndex = container.indexOfChild(historyCard)
        if (historyIndex == -1) return

        val trendIndex = container.indexOfChild(binding.trendCard)
        if (trendIndex != -1 && historyIndex != trendIndex + 1) {
            container.removeView(historyCard)
            val newTrendIndex = container.indexOfChild(binding.trendCard)
            container.addView(historyCard, newTrendIndex + 1)
        }
    }

    private fun observeStates() {
        viewModel.filtersState.observe(this) { state ->
            if (state is PerformanceApiState.Success) {
                cachedFiltersData = state.data
                selectedAcademicYear = state.data.year?.numeric?.toInt()
                    ?: state.data.year?.raw?.toIntOrNull()
                    ?: state.data.years.firstOrNull()
                if (state.data.pageSize > 0) {
                    historyPageSize = state.data.pageSize
                }
                val currentTab = binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()
                val activeType = state.data.type?.takeIf { it.isNotBlank() } ?: currentTab
                if (state.data.types.isNotEmpty()) {
                    populateTabs(state.data.types, activeType)
                }
                populateTrendSpinners(state.data)
                populateLeaderboardSpinners(state.data)
                populateSubjectBoardSpinners(state.data)
                populateHistorySpinners(state.data)
                requestOverview()
            }
        }
        viewModel.overviewState.observe(this) { state ->
            if (state is PerformanceApiState.Success) {
                bindStats(state.data.dashboard?.summary)
                bindTrendChart(state.data.dashboard?.trend)
                bindRank(state.data.dashboard?.rank)
                bindLeaderboard(state.data.leaderboard)
                bindSubjectBoard(state.data.subjectboard)
                bindFocus(state.data.dashboard?.focus ?: emptyList())
                bindHistory(state.data.history)
            }
        }
        viewModel.dashboardState.observe(this) { state ->
            if (state is PerformanceApiState.Success) {
                bindStats(state.data.summary)
                bindTrendChart(state.data.trend)
                bindRank(state.data.rank)
                bindFocus(state.data.focus)
            }
        }
        viewModel.leaderboardState.observe(this) { state ->
            when (state) {
                is PerformanceApiState.Loading -> {
                    binding.topperProgress.visibility = View.VISIBLE
                }
                is PerformanceApiState.Success -> {
                    binding.topperProgress.visibility = View.GONE
                    bindLeaderboard(state.data)
                }
                is PerformanceApiState.Error -> {
                    binding.topperProgress.visibility = View.GONE
                    bindLeaderboard(null)
                }
                null -> Unit
            }
        }
        viewModel.subjectBoardState.observe(this) { state ->
            when (state) {
                is PerformanceApiState.Loading -> {
                    binding.subjectProgress.visibility = View.VISIBLE
                }
                is PerformanceApiState.Success -> {
                    binding.subjectProgress.visibility = View.GONE
                    bindSubjectBoard(state.data)
                }
                is PerformanceApiState.Error -> {
                    binding.subjectProgress.visibility = View.GONE
                    bindSubjectBoard(null)
                }
                null -> Unit
            }
        }
        viewModel.historyState.observe(this) { state ->
            when (state) {
                is PerformanceApiState.Loading -> {
                    binding.historyProgress.visibility = View.VISIBLE
                }
                is PerformanceApiState.Success -> {
                    binding.historyProgress.visibility = View.GONE
                    bindHistory(state.data)
                }
                is PerformanceApiState.Error -> {
                    binding.historyProgress.visibility = View.GONE
                    bindHistory(null)
                }
                null -> Unit
            }
        }
    }

    private fun requestFilters(type: String? = null) {
        if (!requireStudent()) return
        val selectedType = type ?: binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()
        selectedAcademicYear = null
        viewModel.loadFilters(PerformanceFiltersRequest(studentNo = studentNo, type = selectedType))
    }

    private fun requestOverview() {
        if (!requireStudent()) return
        val selectedType = cachedFiltersData?.type
            ?: binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()
        val activeYear = selectedAcademicYear
            ?: cachedFiltersData?.year?.numeric?.toInt()
            ?: cachedFiltersData?.year?.raw?.toIntOrNull()
            ?: cachedFiltersData?.years?.firstOrNull()
        viewModel.loadOverview(
            PerformanceDataRequest(
                studentNo = studentNo,
                type = selectedType,
                year = activeYear
            )
        )
    }

    private fun bindStats(summary: PerformanceSummary?) {
        val empty = getString(R.string.pa_stat_empty_val)
        if (summary == null) {
            binding.statTotal.text = empty
            binding.statTotalNote.text = getString(R.string.pa_stat_total_exams_note)
            binding.statBest.text = empty
            binding.statBestNote.text = empty
            binding.statAvg.text = empty
            binding.statAvgNote.text = empty
            binding.statRank.text = empty
            binding.statRankNote.text = empty
            return
        }

        binding.statTotal.text = summary.totalExams.toString()
        binding.statTotalNote.text = getString(R.string.pa_stat_total_exams_note)

        binding.statBest.text = if (summary.bestScore != null && summary.bestMax != null) {
            getString(
                R.string.pa_stat_best_score_format,
                formatNumber(summary.bestScore),
                formatNumber(summary.bestMax)
            )
        } else {
            empty
        }
        binding.statBestNote.text = summary.bestDate?.takeIf { it.isNotBlank() }?.let {
            getString(R.string.pa_stat_best_score_note_format, it)
        } ?: empty

        binding.statAvg.text = summary.avgScore?.let { formatNumber(it) } ?: empty
        binding.statAvgNote.text = summary.avgPercent?.let {
            getString(R.string.pa_stat_avg_score_note_format, formatDecimal(it))
        } ?: empty

        binding.statRank.text = summary.bestRank?.let { formatIntegerWithCommas(it) } ?: empty
        binding.statRankNote.text = summary.bestRankOutOf?.let {
            getString(R.string.pa_stat_best_rank_note_format, formatIntegerWithCommas(it))
        } ?: empty
    }

    private fun bindRank(rank: PerformanceRank?) {
        val empty = getString(R.string.pa_stat_empty_val)
        if (rank == null) {
            binding.rankPercentile.text = empty
            binding.rankPosition.text = empty
            binding.rankAppeared.text = ""
            binding.rankInsight.text = empty
            return
        }

        binding.rankPercentile.text = rank.percentile?.let {
            getString(R.string.pa_rank_percentile_format, formatDecimal(it))
        } ?: empty

        binding.rankPosition.text = rank.bestRank?.let {
            formatIntegerWithCommas(it)
        } ?: empty

        binding.rankAppeared.text = rank.appeared?.let {
            getString(R.string.pa_rank_appeared_format, formatIntegerWithCommas(it))
        } ?: ""

        if (rank.bestRank != null && rank.appeared != null && rank.percentile != null) {
            binding.rankInsight.text = getString(
                R.string.pa_rank_insight_format,
                formatIntegerWithCommas(rank.bestRank),
                formatIntegerWithCommas(rank.appeared),
                formatDecimal(rank.percentile)
            )
        } else {
            binding.rankInsight.text = empty
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }

    private fun formatDecimal(value: Double): String =
        String.format(Locale.getDefault(), "%.1f", value)

    private fun formatIntegerWithCommas(value: Int): String =
        NumberFormat.getNumberInstance(Locale.getDefault()).format(value)

    // ── Trend Chart ──────────────────────────────────────────────────────

    private fun setupTrendChart() {
        val chart: LineChart = binding.trendChart
        chart.setDescription("")
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setNoDataText("")

        val gridLineColor = ContextCompat.getColor(this, R.color.pa_trend_grid_line)
        val labelColor = ContextCompat.getColor(this, R.color.pa_stat_top_label)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            setGridColor(gridLineColor)
            setTextColor(labelColor)
            textSize = 10f
            isGranularityEnabled = true
            granularity = 1f
            setAvoidFirstLastClipping(true)
        }
        chart.axisLeft.apply {
            setAxisMinValue(0f)
            setAxisMaxValue(100f)
            setDrawGridLines(true)
            setGridColor(gridLineColor)
            setTextColor(labelColor)
            textSize = 10f
        }
        chart.axisRight.isEnabled = false
    }

    private fun attachUserSelectListener(spinner: Spinner, onUserSelected: () -> Unit) {
        var userTouched = false
        spinner.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                userTouched = true
            }
            false
        }
        spinner.setOnKeyListener { _, _, _ ->
            userTouched = true
            false
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (userTouched) {
                    userTouched = false
                    onUserSelected()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTrendFilters() {
        attachUserSelectListener(binding.fMonth) { requestDashboardData() }
        attachUserSelectListener(binding.fYear) { requestDashboardData() }
        attachUserSelectListener(binding.fSubject) { requestDashboardData() }
    }

    private fun populateTrendSpinners(data: PerformanceFiltersData) {
        // Month spinner
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(getMonthName(it)) }
        setSpinnerAdapter(binding.fMonth, monthItems)

        // Year spinner
        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.fYear, yearItems)

        val defaultYear = data.year?.numeric?.toInt()
            ?: data.year?.raw?.toIntOrNull()
            ?: selectedAcademicYear
            ?: data.years.firstOrNull()
        if (defaultYear != null) {
            val yearPos = data.years.indexOf(defaultYear)
            if (yearPos != -1) {
                binding.fYear.setSelection(yearPos + 1)
            }
        }

        // Subject spinner
        val subjectKeys = mutableListOf<String>()
        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (key, label) ->
            subjectKeys.add(key)
            subjectItems.add(label)
        }
        trendSubjectKeys = subjectKeys
        setSpinnerAdapter(binding.fSubject, subjectItems)
    }

    private fun requestDashboardData() {
        if (!requireStudent()) return

        val selectedMonthPos = binding.fMonth.selectedItemPosition
        val selectedMonth = if (selectedMonthPos > 0) cachedFiltersData?.months?.getOrNull(selectedMonthPos - 1) else null

        val selectedYearPos = binding.fYear.selectedItemPosition
        val selectedYear = if (selectedYearPos > 0) cachedFiltersData?.years?.getOrNull(selectedYearPos - 1) else null

        val selectedSubjectPos = binding.fSubject.selectedItemPosition
        val selectedSubject = if (selectedSubjectPos > 0) trendSubjectKeys.getOrNull(selectedSubjectPos - 1) else "totalmarks"

        val selectedType = binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject
        )
        viewModel.loadDashboard(request)
    }

    private fun setSpinnerAdapter(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(
            this,
            R.layout.item_pa_spinner,
            android.R.id.text1,
            items
        )
        adapter.setDropDownViewResource(R.layout.item_pa_spinner_dropdown)
        spinner.adapter = adapter
    }

    private fun bindTrendChart(trend: PerformanceTrend?) {
        if (trend == null || trend.student.isEmpty()) {
            binding.trendChartHolder.visibility = View.GONE
            binding.trendEmpty.visibility = View.VISIBLE
            return
        }

        binding.trendEmpty.visibility = View.GONE
        binding.trendChartHolder.visibility = View.VISIBLE

        val youEntries = trend.student.mapIndexedNotNull { i, v ->
            v?.toFloat()?.let { Entry(i.toFloat(), it) }
        }
        val topperEntries = trend.topper.mapIndexedNotNull { i, v ->
            v?.toFloat()?.let { Entry(i.toFloat(), it) }
        }

        val youColor = ContextCompat.getColor(this, R.color.pa_trend_you)
        val topperColor = ContextCompat.getColor(this, R.color.pa_trend_topper)

        val youDataSet = LineDataSet(youEntries, getString(R.string.pa_trend_your_score)).apply {
            color = youColor
            setCircleColor(youColor)
            circleRadius = 3.5f
            lineWidth = 2f
            setDrawValues(false)
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            setDrawFilled(true)
            fillColor = youColor
            fillAlpha = 25
        }

        val topperDataSet = LineDataSet(topperEntries, getString(R.string.pa_trend_topper_mark)).apply {
            color = topperColor
            setCircleColor(topperColor)
            circleRadius = 3.5f
            lineWidth = 2f
            setDrawValues(false)
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            enableDashedLine(8f, 4f, 0f)
        }

        val chart: LineChart = binding.trendChart
        chart.data = LineData(youDataSet, topperDataSet)

        // x-axis labels
        if (trend.labels.isNotEmpty()) {
            chart.xAxis.apply {
                setGranularity(1f)
                setGranularityEnabled(true)
                val labelCount = if (trend.labels.size <= 6) trend.labels.size else 6
                setLabelCount(labelCount.coerceAtLeast(1), false)

                setValueFormatter(object : AxisValueFormatter {
                    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                        val index = Math.round(value)
                        return if (index in trend.labels.indices) trend.labels[index] else ""
                    }

                    override fun getDecimalDigits(): Int = 0
                })
            }
        }

        chart.invalidate()
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> getString(R.string.month_jan)
            2 -> getString(R.string.month_feb)
            3 -> getString(R.string.month_mar)
            4 -> getString(R.string.month_apr)
            5 -> getString(R.string.month_may)
            6 -> getString(R.string.month_jun)
            7 -> getString(R.string.month_jul)
            8 -> getString(R.string.month_aug)
            9 -> getString(R.string.month_sep)
            10 -> getString(R.string.month_oct)
            11 -> getString(R.string.month_nov)
            12 -> getString(R.string.month_dec)
            else -> month.toString()
        }
    }

    // ── Topper Comparison Leaderboard ─────────────────────────────────────

    private fun setupLeaderboard() {
        attachUserSelectListener(binding.lMonth) { requestLeaderboardData() }
        attachUserSelectListener(binding.lYear) { requestLeaderboardData() }
        attachUserSelectListener(binding.lLimit) { requestLeaderboardData() }
        attachUserSelectListener(binding.lSubject) { requestLeaderboardData() }
    }

    private fun populateLeaderboardSpinners(data: PerformanceFiltersData) {
        // Month spinner
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(getMonthName(it)) }
        setSpinnerAdapter(binding.lMonth, monthItems)

        // Year spinner
        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.lYear, yearItems)

        // Limit spinner
        val limitItems = listOf(
            getString(R.string.pa_limit_last_5),
            getString(R.string.pa_limit_last_10),
            getString(R.string.pa_limit_last_20),
            getString(R.string.pa_limit_all)
        )
        setSpinnerAdapter(binding.lLimit, limitItems)

        // Subject spinner
        val subjectKeys = mutableListOf<String>()
        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (key, label) ->
            subjectKeys.add(key)
            subjectItems.add(label)
        }
        leaderboardSubjectKeys = subjectKeys
        setSpinnerAdapter(binding.lSubject, subjectItems)
    }

    private fun requestLeaderboardData() {
        if (!requireStudent()) return

        val selectedMonthPos = binding.lMonth.selectedItemPosition
        val selectedMonth = if (selectedMonthPos > 0) cachedFiltersData?.months?.getOrNull(selectedMonthPos - 1) else null

        val selectedYearPos = binding.lYear.selectedItemPosition
        val selectedYear = if (selectedYearPos > 0) cachedFiltersData?.years?.getOrNull(selectedYearPos - 1) else null

        val selectedLimit = when (binding.lLimit.selectedItemPosition) {
            0 -> 5
            1 -> 10
            2 -> 20
            3 -> null
            else -> 5
        }

        val selectedSubjectPos = binding.lSubject.selectedItemPosition
        val selectedSubject = if (selectedSubjectPos > 0) leaderboardSubjectKeys.getOrNull(selectedSubjectPos - 1) else "totalmarks"

        val selectedType = binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            limit = selectedLimit
        )
        binding.topperProgress.visibility = View.VISIBLE
        viewModel.loadLeaderboard(request)
    }

    private fun bindLeaderboard(leaderboard: PerformanceLeaderboardData?) {
        binding.topperProgress.visibility = View.GONE
        binding.comparisonSubjectHead.text = leaderboard?.comparisonSubjectLabel ?: getString(R.string.pa_topper_th_bio)

        if (leaderboard == null || leaderboard.data.isEmpty()) {
            binding.topperTableScroll.visibility = View.GONE
            binding.topperEmpty.visibility = View.VISIBLE
            return
        }

        binding.topperEmpty.visibility = View.GONE
        binding.topperTableScroll.visibility = View.VISIBLE

        val childCount = binding.topperTable.childCount
        if (childCount > 1) {
            binding.topperTable.removeViews(1, childCount - 1)
        }

        val emptyDash = getString(R.string.performance_results_empty_value)
        val altRowColor = ContextCompat.getColor(this, R.color.pa_table_row_alt)
        val defaultRowColor = Color.WHITE

        leaderboard.data.forEachIndexed { index, row ->
            val rowBinding = ItemPaTopperRowBinding.inflate(layoutInflater, binding.topperTable, false)

            rowBinding.root.setBackgroundColor(if (index % 2 == 1) altRowColor else defaultRowColor)

            rowBinding.tvDate.text = row.date?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvScore.text = row.score?.let { formatNumber(it) } ?: emptyDash
            rowBinding.tvTopper.text = row.topper?.let { formatNumber(it) } ?: emptyDash
            rowBinding.tvPhy.text = row.physics?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvChe.text = row.chemistry?.raw?.takeIf { it.isNotBlank() } ?: emptyDash

            val compValue = row.comparisonSubject?.raw ?: row.biology?.raw
            rowBinding.tvComparisonSub.text = compValue?.takeIf { it.isNotBlank() } ?: emptyDash

            rowBinding.tvOutOf.text = row.outOf?.let { formatNumber(it) } ?: emptyDash

            val gap = row.gap
            if (gap != null) {
                rowBinding.tvGap.text = formatNumber(gap)
                if (gap > 0) {
                    rowBinding.tvGap.setBackgroundResource(R.drawable.pa_gap_high_bg)
                    rowBinding.tvGap.setTextColor(ContextCompat.getColor(this, R.color.pa_gap_high_text))
                } else {
                    rowBinding.tvGap.setBackgroundResource(R.drawable.pa_gap_low_bg)
                    rowBinding.tvGap.setTextColor(ContextCompat.getColor(this, R.color.pa_gap_low_text))
                }
            } else {
                rowBinding.tvGap.text = emptyDash
                rowBinding.tvGap.setBackgroundResource(R.drawable.pa_badge_neutral_bg)
                rowBinding.tvGap.setTextColor(ContextCompat.getColor(this, R.color.pa_badge_neutral_text))
            }

            val grade = row.grade?.takeIf { it.isNotBlank() }
            if (grade != null && grade != "-") {
                rowBinding.tvGrade.text = grade
            } else {
                rowBinding.tvGrade.text = emptyDash
            }
            rowBinding.tvGrade.setBackgroundResource(R.drawable.pa_badge_success_bg)
            rowBinding.tvGrade.setTextColor(ContextCompat.getColor(this, R.color.pa_badge_success_text))

            rowBinding.tvRank.text = row.rank?.let { formatIntegerWithCommas(it) } ?: emptyDash
            rowBinding.tvAppeared.text = row.appeared?.let { formatIntegerWithCommas(it) } ?: emptyDash

            val progress = row.progress ?: 0.0
            val gapPercent = row.gapPercent ?: 0.0
            rowBinding.pbProgress.progress = progress.toInt().coerceIn(0, 100)
            rowBinding.tvProgressCaption.text = getString(
                R.string.pa_topper_progress_format,
                formatDecimal(progress),
                formatDecimal(gapPercent)
            )

            binding.topperTable.addView(rowBinding.root)
        }
    }

    // ── Subject-Wise Performance Comparison ──────────────────────────────

    private fun setupSubjectBoard() {
        attachUserSelectListener(binding.sMonth) { requestSubjectBoardData() }
        attachUserSelectListener(binding.sYear) { requestSubjectBoardData() }
        attachUserSelectListener(binding.sLimit) { requestSubjectBoardData() }
        attachUserSelectListener(binding.sSubject) { requestSubjectBoardData() }
    }

    private fun populateSubjectBoardSpinners(data: PerformanceFiltersData) {
        // Month spinner
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(getMonthName(it)) }
        setSpinnerAdapter(binding.sMonth, monthItems)

        // Year spinner
        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.sYear, yearItems)

        // Limit spinner
        val limitItems = listOf(
            getString(R.string.pa_limit_last_5),
            getString(R.string.pa_limit_last_10),
            getString(R.string.pa_limit_last_20),
            getString(R.string.pa_limit_all)
        )
        setSpinnerAdapter(binding.sLimit, limitItems)

        // Subject spinner
        val subjectKeys = mutableListOf<String>()
        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (key, label) ->
            subjectKeys.add(key)
            subjectItems.add(label)
        }
        subjectBoardSubjectKeys = subjectKeys
        setSpinnerAdapter(binding.sSubject, subjectItems)
    }

    private fun requestSubjectBoardData() {
        if (!requireStudent()) return

        val selectedMonthPos = binding.sMonth.selectedItemPosition
        val selectedMonth = if (selectedMonthPos > 0) cachedFiltersData?.months?.getOrNull(selectedMonthPos - 1) else null

        val selectedYearPos = binding.sYear.selectedItemPosition
        val selectedYear = if (selectedYearPos > 0) cachedFiltersData?.years?.getOrNull(selectedYearPos - 1) else null

        val selectedLimit = when (binding.sLimit.selectedItemPosition) {
            0 -> 5
            1 -> 10
            2 -> 20
            3 -> null
            else -> 5
        }

        val selectedSubjectPos = binding.sSubject.selectedItemPosition
        val selectedSubject = if (selectedSubjectPos > 0) subjectBoardSubjectKeys.getOrNull(selectedSubjectPos - 1) else "totalmarks"

        val selectedType = binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            limit = selectedLimit
        )
        binding.subjectProgress.visibility = View.VISIBLE
        viewModel.loadSubjectBoard(request)
    }

    private fun bindSubjectBoard(subjectboard: PerformanceSubjectBoardData?) {
        binding.subjectProgress.visibility = View.GONE

        if (subjectboard == null || subjectboard.data.isEmpty()) {
            binding.subjectTableScroll.visibility = View.GONE
            binding.subjectEmpty.visibility = View.VISIBLE
            return
        }

        binding.subjectEmpty.visibility = View.GONE
        binding.subjectTableScroll.visibility = View.VISIBLE
        binding.subjectTable.removeAllViews()

        val columns = subjectboard.columns
        renderSubjectTableHeader(columns)

        val altRowColor = ContextCompat.getColor(this, R.color.pa_table_row_alt)
        val defaultRowColor = Color.WHITE

        subjectboard.data.forEachIndexed { index, row ->
            val dataRow = createSubjectDataRow(
                row,
                columns,
                if (index % 2 == 1) altRowColor else defaultRowColor
            )
            binding.subjectTable.addView(dataRow)
        }
    }

    private fun renderSubjectTableHeader(columns: List<PerformanceSubjectColumn>) {
        val subjectCols = if (columns.isNotEmpty()) columns else listOf(PerformanceSubjectColumn(key = "-", label = "-"))
        val colSpan = subjectCols.size

        val dateWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_date_width)
        val typeWidth = resources.getDimensionPixelSize(R.dimen.pa_col_subject_type_width)
        val subWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_sub_width)
        val rankWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_rank_width)
        val appearedWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_appeared_width)
        val gradeWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_grade_width)
        val gapWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_gap_width)
        val cellPaddingH = resources.getDimensionPixelSize(R.dimen.pa_table_cell_padding_h)
        val cellPaddingV = resources.getDimensionPixelSize(R.dimen.pa_table_cell_padding_v)
        val headerBg = ContextCompat.getColor(this, R.color.pa_table_header_bg)
        val headerTextColor = ContextCompat.getColor(this, R.color.pa_table_header_text)

        // Header Row 1: Group headers
        val rowHead1 = TableRow(this).apply {
            setBackgroundColor(headerBg)
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = resources.getDimensionPixelSize(R.dimen.pa_table_header_sub_min_height)
        }

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(dateWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(typeWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        val tvYourGroup = TextView(this).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
                span = colSpan
            }
            gravity = Gravity.CENTER
            text = getString(R.string.pa_subject_th_your_marks)
            setTextColor(headerTextColor)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        rowHead1.addView(tvYourGroup)

        val tvTopperGroup = TextView(this).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
                span = colSpan
            }
            gravity = Gravity.CENTER
            text = getString(R.string.pa_subject_th_topper_marks)
            setTextColor(headerTextColor)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        rowHead1.addView(tvTopperGroup)

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(rankWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(appearedWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(gradeWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        rowHead1.addView(View(this).apply {
            layoutParams = TableRow.LayoutParams(gapWidth, TableRow.LayoutParams.WRAP_CONTENT)
        })

        binding.subjectTable.addView(rowHead1)

        // Header Row 2: Sub-headers
        val rowHead2 = TableRow(this).apply {
            setBackgroundColor(headerBg)
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = resources.getDimensionPixelSize(R.dimen.pa_table_header_main_min_height)
        }

        fun createHeaderCell(width: Int, title: String, isStartGravity: Boolean = false): TextView {
            return TextView(this).apply {
                layoutParams = TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT)
                gravity = if (isStartGravity) Gravity.CENTER_VERTICAL else Gravity.CENTER
                text = title
                setTextColor(headerTextColor)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
        }

        rowHead2.addView(createHeaderCell(dateWidth, getString(R.string.pa_subject_th_exam_date), true))
        rowHead2.addView(createHeaderCell(typeWidth, getString(R.string.pa_subject_th_exam_type)))

        subjectCols.forEach { col ->
            rowHead2.addView(createHeaderCell(subWidth, col.label ?: col.key ?: getString(R.string.performance_results_empty_value)))
        }

        subjectCols.forEach { col ->
            rowHead2.addView(createHeaderCell(subWidth, col.label ?: col.key ?: getString(R.string.performance_results_empty_value)))
        }

        rowHead2.addView(createHeaderCell(rankWidth, getString(R.string.pa_subject_th_your_rank)))
        rowHead2.addView(createHeaderCell(appearedWidth, getString(R.string.pa_subject_th_total_appeared)))
        rowHead2.addView(createHeaderCell(gradeWidth, getString(R.string.pa_subject_th_grade)))
        rowHead2.addView(createHeaderCell(gapWidth, getString(R.string.pa_subject_th_gap)))

        binding.subjectTable.addView(rowHead2)
    }

    private fun createSubjectDataRow(
        row: PerformanceSubjectBoardRow,
        columns: List<PerformanceSubjectColumn>,
        bgColor: Int
    ): TableRow {
        val subjectCols = if (columns.isNotEmpty()) columns else listOf(PerformanceSubjectColumn(key = "-", label = "-"))
        val emptyDash = getString(R.string.performance_results_empty_value)

        val dateWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_date_width)
        val typeWidth = resources.getDimensionPixelSize(R.dimen.pa_col_subject_type_width)
        val subWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_sub_width)
        val rankWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_rank_width)
        val appearedWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_appeared_width)
        val gradeWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_grade_width)
        val gapWidth = resources.getDimensionPixelSize(R.dimen.pa_col_topper_gap_width)
        val cellPaddingH = resources.getDimensionPixelSize(R.dimen.pa_table_cell_padding_h)
        val cellPaddingV = resources.getDimensionPixelSize(R.dimen.pa_table_cell_padding_v)

        val cellTextColor = ContextCompat.getColor(this, R.color.pa_table_cell_text)
        val cellStrongColor = ContextCompat.getColor(this, R.color.pa_table_cell_strong)

        val rowView = TableRow(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = resources.getDimensionPixelSize(R.dimen.pa_table_row_min_height)
            setBackgroundColor(bgColor)
        }

        fun createTextCell(width: Int, textValue: String, isStrong: Boolean = false, isStartGravity: Boolean = false): TextView {
            return TextView(this).apply {
                layoutParams = TableRow.LayoutParams(width, TableRow.LayoutParams.WRAP_CONTENT)
                gravity = if (isStartGravity) Gravity.CENTER_VERTICAL else Gravity.CENTER
                text = textValue
                setTextColor(if (isStrong) cellStrongColor else cellTextColor)
                if (isStrong) typeface = Typeface.DEFAULT_BOLD
                textSize = 12f
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
        }

        // 1. Date
        rowView.addView(createTextCell(dateWidth, row.date?.takeIf { it.isNotBlank() } ?: emptyDash, isStrong = false, isStartGravity = true))

        // 2. Type (Green Badge)
        val examType = row.type?.takeIf { it.isNotBlank() && it != "-" }
        if (examType != null) {
            val typeFrame = FrameLayout(this).apply {
                layoutParams = TableRow.LayoutParams(typeWidth, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
            }
            val typeBadge = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
                text = examType
                setBackgroundResource(R.drawable.pa_badge_success_bg)
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_badge_success_text))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                val badgePadH = resources.getDimensionPixelSize(R.dimen.pa_badge_padding_h)
                val badgePadV = resources.getDimensionPixelSize(R.dimen.pa_badge_padding_v)
                setPadding(badgePadH, badgePadV, badgePadH, badgePadV)
            }
            typeFrame.addView(typeBadge)
            rowView.addView(typeFrame)
        } else {
            rowView.addView(createTextCell(typeWidth, emptyDash))
        }

        // 3. Your Marks
        subjectCols.forEach { col ->
            val part = col.key?.let { row.parts[it] }
            val value = part?.your?.raw?.takeIf { it.isNotBlank() }
                ?: part?.your?.numeric?.let { formatNumber(it) }
                ?: emptyDash
            rowView.addView(createTextCell(subWidth, value, isStrong = true))
        }

        // 4. Topper Marks
        subjectCols.forEach { col ->
            val part = col.key?.let { row.parts[it] }
            val value = part?.topper?.raw?.takeIf { it.isNotBlank() }
                ?: part?.topper?.numeric?.let { formatNumber(it) }
                ?: emptyDash
            rowView.addView(createTextCell(subWidth, value))
        }

        // 5. Your Rank
        val rankText = row.rank?.let { formatIntegerWithCommas(it) } ?: emptyDash
        rowView.addView(createTextCell(rankWidth, rankText))

        // 6. Total Appeared
        val appearedText = row.appeared?.let { formatIntegerWithCommas(it) } ?: emptyDash
        rowView.addView(createTextCell(appearedWidth, appearedText))

        // 7. Grade (Badge)
        val gradeFrame = FrameLayout(this).apply {
            layoutParams = TableRow.LayoutParams(gradeWidth, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        val gradeBadge = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            val grade = row.grade?.takeIf { it.isNotBlank() }
            text = if (grade != null && grade != "-") grade else emptyDash
            setBackgroundResource(R.drawable.pa_badge_success_bg)
            setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_badge_success_text))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val badgePadH = resources.getDimensionPixelSize(R.dimen.pa_badge_padding_h)
            val badgePadV = resources.getDimensionPixelSize(R.dimen.pa_badge_padding_v)
            setPadding(badgePadH, badgePadV, badgePadH, badgePadV)
        }
        gradeFrame.addView(gradeBadge)
        rowView.addView(gradeFrame)

        // 8. Gap (Badge)
        val gapFrame = FrameLayout(this).apply {
            layoutParams = TableRow.LayoutParams(gapWidth, TableRow.LayoutParams.WRAP_CONTENT)
            setPadding(cellPaddingH, cellPaddingV, cellPaddingH, cellPaddingV)
        }
        val gapBadge = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            val gap = row.gap
            if (gap != null) {
                text = formatNumber(gap)
                if (gap > 0) {
                    setBackgroundResource(R.drawable.pa_gap_high_bg)
                    setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_gap_high_text))
                } else {
                    setBackgroundResource(R.drawable.pa_gap_low_bg)
                    setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_gap_low_text))
                }
            } else {
                text = emptyDash
                setBackgroundResource(R.drawable.pa_badge_neutral_bg)
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_badge_neutral_text))
            }
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val gapPadH = (6 * resources.displayMetrics.density).toInt()
            val gapPadV = (2 * resources.displayMetrics.density).toInt()
            setPadding(gapPadH, gapPadV, gapPadH, gapPadV)
        }
        gapFrame.addView(gapBadge)
        rowView.addView(gapFrame)

        return rowView
    }

    // ── Exam History ──────────────────────────────────────────────────────

    private fun setupHistory() {
        binding.btnHistoryPrev.setOnClickListener {
            if (currentHistoryPage > 1) {
                requestHistoryPage(currentHistoryPage - 1)
            }
        }
        binding.btnHistoryNext.setOnClickListener {
            if (currentHistoryPage < totalHistoryPages) {
                requestHistoryPage(currentHistoryPage + 1)
            }
        }

        attachUserSelectListener(binding.hMonth) {
            currentHistoryPage = 1
            requestHistoryPage(1)
        }
        attachUserSelectListener(binding.hYear) {
            currentHistoryPage = 1
            requestHistoryPage(1)
        }
        attachUserSelectListener(binding.hSubject) {
            currentHistoryPage = 1
            requestHistoryPage(1)
        }
    }

    private fun populateHistorySpinners(data: PerformanceFiltersData) {
        // Month spinner
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(getMonthName(it)) }
        setSpinnerAdapter(binding.hMonth, monthItems)

        // Year spinner
        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.hYear, yearItems)

        // Subject spinner
        val subjectKeys = mutableListOf<String>()
        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (key, label) ->
            subjectKeys.add(key)
            subjectItems.add(label)
        }
        historySubjectKeys = subjectKeys
        setSpinnerAdapter(binding.hSubject, subjectItems)
    }

    private fun requestHistoryPage(page: Int) {
        if (!requireStudent()) return

        val selectedMonthPos = binding.hMonth.selectedItemPosition
        val selectedMonth = if (selectedMonthPos > 0) cachedFiltersData?.months?.getOrNull(selectedMonthPos - 1) else null

        val selectedYearPos = binding.hYear.selectedItemPosition
        val selectedYear = if (selectedYearPos > 0) cachedFiltersData?.years?.getOrNull(selectedYearPos - 1) else null

        val selectedSubjectPos = binding.hSubject.selectedItemPosition
        val selectedSubject = if (selectedSubjectPos > 0) historySubjectKeys.getOrNull(selectedSubjectPos - 1) else "totalmarks"

        val selectedType = binding.testTabs.getTabAt(binding.testTabs.selectedTabPosition)?.text?.toString()

        val request = PerformanceDataRequest(
            studentNo = studentNo,
            type = selectedType,
            year = selectedYear,
            month = selectedMonth,
            subject = selectedSubject,
            page = page,
            length = historyPageSize
        )
        binding.historyProgress.visibility = View.VISIBLE
        viewModel.loadHistory(request)
    }

    private fun bindHistory(history: PerformanceHistoryData?) {
        binding.historyProgress.visibility = View.GONE
        if (history == null || history.data.isEmpty()) {
            binding.paTableScroll.visibility = View.GONE
            binding.historyEmpty.visibility = View.VISIBLE
            binding.historyInfo.text = getString(R.string.pa_history_info_empty)
            binding.tvHistoryPage.text = "1"
            binding.btnHistoryPrev.isEnabled = false
            binding.btnHistoryPrev.alpha = 0.4f
            binding.btnHistoryNext.isEnabled = false
            binding.btnHistoryNext.alpha = 0.4f
            return
        }

        binding.historyEmpty.visibility = View.GONE
        binding.paTableScroll.visibility = View.VISIBLE

        // Remove previous data rows, keeping the header row at index 0
        val childCount = binding.historyTable.childCount
        if (childCount > 1) {
            binding.historyTable.removeViews(1, childCount - 1)
        }

        val emptyDash = getString(R.string.performance_results_empty_value)
        val altRowColor = ContextCompat.getColor(this, R.color.pa_table_row_alt)
        val defaultRowColor = Color.WHITE

        history.data.forEachIndexed { index, row ->
            val rowBinding = ItemPaHistoryRowBinding.inflate(layoutInflater, binding.historyTable, false)

            rowBinding.root.setBackgroundColor(if (index % 2 == 1) altRowColor else defaultRowColor)

            rowBinding.tvDate.text = row.date?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvExam.text = row.examName?.takeIf { it.isNotBlank() } ?: emptyDash

            rowBinding.tvPhy.text = row.physics?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvChe.text = row.chemistry?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvBio.text = row.biology?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvMat.text = row.maths?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvApt.text = row.aptitude?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvDrw.text = row.drawing?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvP1.text = row.p1?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvP2.text = row.p2?.raw?.takeIf { it.isNotBlank() } ?: emptyDash

            rowBinding.tvTotal.text = row.total?.let { formatNumber(it) } ?: emptyDash
            rowBinding.tvMax.text = row.maxMarks?.let { formatNumber(it) } ?: emptyDash
            rowBinding.tvHighest.text = row.highest?.let { formatNumber(it) } ?: emptyDash
            rowBinding.tvPercent.text = row.percent?.let { formatDecimal(it) } ?: emptyDash

            val grade = row.grade?.takeIf { it.isNotBlank() }
            if (grade != null && grade != "-") {
                rowBinding.tvGrade.text = grade
            } else {
                rowBinding.tvGrade.text = emptyDash
            }
            rowBinding.tvGrade.setBackgroundResource(R.drawable.pa_badge_success_bg)
            rowBinding.tvGrade.setTextColor(ContextCompat.getColor(this, R.color.pa_badge_success_text))

            rowBinding.tvRank.text = row.rank?.let { formatIntegerWithCommas(it) } ?: emptyDash
            rowBinding.tvAppeared.text = row.appeared?.let { formatIntegerWithCommas(it) } ?: emptyDash
            val hasOmr = (!row.omrUrl.isNullOrBlank() && row.omrUrl != "-") ||
                (!row.omr.isNullOrBlank() && row.omr != "-")
            if (hasOmr) {
                rowBinding.tvOmr.text = "📄"
                rowBinding.tvOmr.setTextColor(ContextCompat.getColor(this, R.color.primary))
            } else {
                rowBinding.tvOmr.text = emptyDash
                rowBinding.tvOmr.setTextColor(ContextCompat.getColor(this, R.color.pa_table_cell_text))
            }
            rowBinding.tvOmr.setOnClickListener(null)
            rowBinding.tvOmr.isClickable = false

            val hasResult = (!row.eRight.isNullOrBlank() && row.eRight != "-") ||
                (!row.eWrong.isNullOrBlank() && row.eWrong != "-") ||
                (!row.eAnswer.isNullOrBlank() && row.eAnswer != "-") ||
                (!row.eUnanswered.isNullOrBlank() && row.eUnanswered != "-") ||
                (!row.eCancelled.isNullOrBlank() && row.eCancelled != "-") ||
                (!row.eIncorrect.isNullOrBlank() && row.eIncorrect != "-")
            if (hasResult) {
                rowBinding.tvResult.text = getString(R.string.pa_history_view_result)
                rowBinding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.pa_stat_best_icon_stroke))
                rowBinding.tvResult.setOnClickListener {
                    showExamResultModal(row)
                }
            } else {
                rowBinding.tvResult.text = emptyDash
                rowBinding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.pa_table_cell_text))
                rowBinding.tvResult.setOnClickListener(null)
            }

            binding.historyTable.addView(rowBinding.root)
        }

        currentHistoryPage = history.page
        totalHistoryPages = history.pages.coerceAtLeast(1)
        binding.tvHistoryPage.text = currentHistoryPage.toString()

        val from = history.from ?: if (history.total > 0) ((history.page - 1) * history.length + 1) else 0
        val to = history.to ?: (from + history.data.size - 1).coerceAtMost(history.total)
        binding.historyInfo.text = getString(R.string.pa_history_info_format, from, to, history.total)

        binding.btnHistoryPrev.isEnabled = currentHistoryPage > 1
        binding.btnHistoryPrev.alpha = if (binding.btnHistoryPrev.isEnabled) 1.0f else 0.4f

        binding.btnHistoryNext.isEnabled = currentHistoryPage < totalHistoryPages
        binding.btnHistoryNext.alpha = if (binding.btnHistoryNext.isEnabled) 1.0f else 0.4f
    }

    // ── Focus Areas & Improvement Plan ───────────────────────────────────

    private fun bindFocus(focusList: List<PerformanceFocus>) {
        if (focusList.isEmpty()) {
            binding.focusGrid.visibility = View.GONE
            binding.focusEmpty.visibility = View.VISIBLE
            return
        }

        binding.focusEmpty.visibility = View.GONE
        binding.focusGrid.visibility = View.VISIBLE
        binding.focusGrid.removeAllViews()

        focusList.forEach { focus ->
            val cardView = createFocusCardView(focus)
            binding.focusGrid.addView(cardView)
        }
    }

    private fun createFocusCardView(focus: PerformanceFocus): View {
        val density = resources.displayMetrics.density
        val emptyDash = getString(R.string.performance_results_empty_value)

        val priority = focus.priority?.lowercase(Locale.ROOT) ?: "medium"
        val priorityColor: Int
        val badgeBgColor: Int
        val badgeTextColor: Int
        val noteBgColor: Int
        val noteBorderColor: Int
        val noteTextColor: Int
        val priorityIcon: String

        when (priority) {
            "high" -> {
                priorityColor = Color.parseColor("#EF4444")
                badgeBgColor = Color.parseColor("#FEE2E2")
                badgeTextColor = Color.parseColor("#DC2626")
                noteBgColor = Color.parseColor("#FEF2F2")
                noteBorderColor = Color.parseColor("#FECACA")
                noteTextColor = Color.parseColor("#B91C1C")
                priorityIcon = "⚠"
            }
            "low" -> {
                priorityColor = Color.parseColor("#10B981")
                badgeBgColor = Color.parseColor("#DCFCE7")
                badgeTextColor = Color.parseColor("#166534")
                noteBgColor = Color.parseColor("#F0FDF4")
                noteBorderColor = Color.parseColor("#BBF7D0")
                noteTextColor = Color.parseColor("#15803D")
                priorityIcon = "✔"
            }
            else -> { // medium
                priorityColor = Color.parseColor("#F59E0B")
                badgeBgColor = Color.parseColor("#FEF3C7")
                badgeTextColor = Color.parseColor("#D97706")
                noteBgColor = Color.parseColor("#FFFBEB")
                noteBorderColor = Color.parseColor("#FDE68A")
                noteTextColor = Color.parseColor("#B45309")
                priorityIcon = "⚠"
            }
        }

        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (14 * density).toInt()
            }
            radius = 12 * density
            cardElevation = 0f
            strokeWidth = (1 * density).toInt()
            strokeColor = ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_card_border)
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // ── 1. pa-focus-top ──────────────────────────────────
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Left: pa-focus-name (Icon + Title + Subtitles)
        val nameCol = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvIcon = TextView(this).apply {
            text = priorityIcon
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(priorityColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (8 * density).toInt()
                topMargin = (1 * density).toInt()
            }
        }
        nameCol.addView(tvIcon)

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvSubject = TextView(this).apply {
            text = focus.displayName.takeIf { it.isNotBlank() } ?: emptyDash
            setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_val_text))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        titleCol.addView(tvSubject)

        val scoreStr = if (focus.avg != null && focus.avgMax != null) {
            "Score ${formatNumber(focus.avg)}/${formatNumber(focus.avgMax)} (${formatDecimal(focus.percent ?: 0.0)}%)"
        } else if (focus.percent != null) {
            "Score (${formatDecimal(focus.percent)}%)"
        } else ""

        if (scoreStr.isNotBlank()) {
            val tvScore = TextView(this).apply {
                text = scoreStr
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_top_label))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (2 * density).toInt()
                }
            }
            titleCol.addView(tvScore)
        }

        val topperPercentStr = formatDecimal(focus.topperPercent ?: 0.0)
        val topperScoreHtml = if (focus.topperAvg != null && focus.avgMax != null) {
            "Topper Score ${formatNumber(focus.topperAvg)}/${formatNumber(focus.avgMax)} (<b>$topperPercentStr%</b>)"
        } else if (focus.topperPercent != null) {
            "Topper Score (<b>$topperPercentStr%</b>)"
        } else ""

        if (topperScoreHtml.isNotBlank()) {
            val tvTopper = TextView(this).apply {
                text = HtmlCompat.fromHtml(topperScoreHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_top_label))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (2 * density).toInt()
                }
            }
            titleCol.addView(tvTopper)
        }

        nameCol.addView(titleCol)
        topRow.addView(nameCol)

        // Right: Priority Badge
        val priorityBadge = TextView(this).apply {
            val label = focus.priorityLabel?.takeIf { it.isNotBlank() }
                ?: when (priority) {
                    "high" -> getString(R.string.pa_focus_priority_high)
                    "low" -> getString(R.string.pa_focus_priority_low)
                    else -> getString(R.string.pa_focus_priority_medium)
                }
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(badgeTextColor)
            val padH = (10 * density).toInt()
            val padV = (4 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(badgeBgColor)
                cornerRadius = 12 * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = (8 * density).toInt()
            }
        }
        topRow.addView(priorityBadge)
        container.addView(topRow)

        // ── 2. pa-focus-mid ──────────────────────────────────
        val midRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (14 * density).toInt()
            }
        }

        val noteText = focus.note?.takeIf { it.isNotBlank() }
        val tvNote = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = noteText ?: ""
            setTextColor(noteTextColor)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            val padH = (12 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(noteBgColor)
                setStroke((1 * density).toInt(), noteBorderColor)
                cornerRadius = 8 * density
            }
        }
        midRow.addView(tvNote)

        val donutView = DonutProgressView(this).apply {
            val size = (64 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = (12 * density).toInt()
                marginEnd = (4 * density).toInt()
            }
            progress = (focus.percent ?: 0.0).toFloat()
            progressColor = priorityColor
            text = "${formatDecimal(focus.percent ?: 0.0)}%"
        }
        midRow.addView(donutView)
        container.addView(midRow)

        // ── 3. pa-focus-chartlbl & pa-focus-chart ────────────
        if (focus.points.isNotEmpty()) {
            val tvChartLbl = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * density).toInt()
                    bottomMargin = (4 * density).toInt()
                }
                text = "Score Trend (${focus.points.size} Exams)"
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_top_label))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            }
            container.addView(tvChartLbl)

            val miniChart = LineChart(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (100 * density).toInt()
                )
                setDescription("")
                legend.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setNoDataText("")
                setViewPortOffsets(28 * density, 8 * density, 16 * density, 22 * density)

                val gridColor = Color.parseColor("#F1F5F9")
                val labelColor = Color.parseColor("#94A3B8")

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    setAxisLineColor(Color.parseColor("#E2E8F0"))
                    setTextColor(labelColor)
                    textSize = 9f
                    isGranularityEnabled = true
                    granularity = 1f
                    setAvoidFirstLastClipping(true)
                    setValueFormatter(object : AxisValueFormatter {
                        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                            val idx = Math.round(value)
                            return if (idx in focus.points.indices) focus.points[idx].label ?: "" else ""
                        }
                        override fun getDecimalDigits(): Int = 0
                    })
                    setLabelCount(focus.points.size.coerceAtLeast(1), false)
                }

                axisLeft.apply {
                    setAxisMinValue(0f)
                    setAxisMaxValue(100f)
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawAxisLine(false)
                    setTextColor(labelColor)
                    textSize = 9f
                    setLabelCount(3, false)
                }

                axisRight.isEnabled = false
            }

            val studentEntries = focus.points.mapIndexedNotNull { i, pt ->
                pt.value?.let { Entry(i.toFloat(), it.toFloat()) }
            }
            val topperEntries = focus.points.mapIndexedNotNull { i, pt ->
                pt.topper?.let { Entry(i.toFloat(), it.toFloat()) }
            }

            val dataSets = mutableListOf<ILineDataSet>()

            if (topperEntries.isNotEmpty()) {
                val topperDs = LineDataSet(topperEntries, "Topper").apply {
                    val grayColor = Color.parseColor("#94A3B8")
                    color = grayColor
                    setCircleColor(grayColor)
                    circleRadius = 2.5f
                    lineWidth = 1.5f
                    enableDashedLine(8f, 6f, 0f)
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.15f
                    setDrawFilled(false)
                }
                dataSets.add(topperDs)
            }

            if (studentEntries.isNotEmpty()) {
                val studentDs = LineDataSet(studentEntries, "You").apply {
                    color = priorityColor
                    setCircleColor(priorityColor)
                    circleRadius = 3f
                    lineWidth = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.15f
                    setDrawFilled(true)
                    fillColor = priorityColor
                    fillAlpha = 20
                }
                dataSets.add(studentDs)
            }

            if (dataSets.isNotEmpty()) {
                miniChart.data = LineData(dataSets)
                miniChart.invalidate()
            }

            container.addView(miniChart)
        }

        card.addView(container)
        return card
    }

    private class DonutProgressView(context: Context) : View(context) {
        var progress: Float = 0f
        var strokeWidthPx: Float = 6.5f * resources.displayMetrics.density
        var progressColor: Int = Color.parseColor("#F59E0B")
        var trackColor: Int = Color.parseColor("#F1F5F9")
        var text: String = ""

        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        private val rectF = RectF()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val d = Math.min(width, height).toFloat()
            if (d <= 0f) return
            val pad = strokeWidthPx / 2f + 2f
            rectF.set(pad, pad, d - pad, d - pad)

            trackPaint.color = trackColor
            trackPaint.strokeWidth = strokeWidthPx
            canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

            progressPaint.color = progressColor
            progressPaint.strokeWidth = strokeWidthPx
            val sweep = (progress / 100f).coerceIn(0f, 1f) * 360f
            canvas.drawArc(rectF, -90f, sweep, false, progressPaint)

            textPaint.color = progressColor
            textPaint.textSize = d * 0.22f
            val yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(text, width / 2f, yPos, textPaint)
        }
    }

    private fun showExamResultModal(row: PerformanceHistoryRow) {
        val emptyDash = getString(R.string.performance_results_empty_value)
        binding.tvRightAnswers.text = row.eRight?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.tvWrongAnswers.text = row.eWrong?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.tvAnswered.text = row.eAnswer?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.tvUnanswered.text = row.eUnanswered?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.tvCancelled.text = row.eCancelled?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.tvIncorrectQuestions.text = row.eIncorrect?.takeIf { it.isNotBlank() } ?: emptyDash
        binding.notifymessageModal.visibility = View.VISIBLE
    }

    @Suppress("unused")
    private fun openOmrSheet(row: PerformanceHistoryRow) {
        val rawUrl = row.omrUrl?.takeIf { it.isNotBlank() && it != "-" }
            ?: row.omr?.takeIf { it.isNotBlank() && it != "-" }
            ?: return

        val fullUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            val base = BuildConfig.BASE_URL.trimEnd('/')
            val path = rawUrl.trimStart('/')
            "$base/$path"
        }

        try {
            UIUtils.openInBrowser(this, fullUrl)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Could not open OMR sheet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private fun requireStudent(): Boolean {
        if (studentNo.isNotBlank()) return true
        Toast.makeText(this, R.string.performance_results_no_student, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun getStudentNo(): String {
        val manager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts: Array<Account> = manager.getAccountsByType(APPLICATION_ID)
        return accounts.firstOrNull()?.name ?: ""
    }
}

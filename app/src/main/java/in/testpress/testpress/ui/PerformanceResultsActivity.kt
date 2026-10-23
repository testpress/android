package `in`.testpress.testpress.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.github.testpress.mikephil.charting.charts.LineChart
import com.github.testpress.mikephil.charting.components.AxisBase
import com.github.testpress.mikephil.charting.components.XAxis
import com.github.testpress.mikephil.charting.data.Entry
import com.github.testpress.mikephil.charting.data.LineData
import com.github.testpress.mikephil.charting.data.LineDataSet
import com.github.testpress.mikephil.charting.formatter.AxisValueFormatter
import com.github.testpress.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.BuildConfig.APPLICATION_ID
import `in`.testpress.testpress.R
import `in`.testpress.testpress.databinding.ActivityPerformanceResultsBinding
import `in`.testpress.testpress.databinding.ItemPaHistoryRowBinding
import `in`.testpress.testpress.databinding.ItemPaTopperRowBinding
import `in`.testpress.testpress.models.pojo.PerformanceFiltersData
import `in`.testpress.testpress.models.pojo.PerformanceFocus
import `in`.testpress.testpress.models.pojo.PerformanceHistoryData
import `in`.testpress.testpress.models.pojo.PerformanceHistoryRow
import `in`.testpress.testpress.models.pojo.PerformanceLeaderboardData
import `in`.testpress.testpress.models.pojo.PerformanceRank
import `in`.testpress.testpress.models.pojo.PerformanceSubjectBoardData
import `in`.testpress.testpress.models.pojo.PerformanceSummary
import `in`.testpress.testpress.models.pojo.PerformanceTrend
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.repository.PerformanceAnalysisRepository
import `in`.testpress.testpress.repository.PerformanceApiState
import `in`.testpress.testpress.ui.utils.PerformanceFormatter
import `in`.testpress.testpress.ui.utils.PerformanceSubjectTableHelper
import `in`.testpress.testpress.ui.view.DonutProgressView
import `in`.testpress.testpress.util.UIUtils
import `in`.testpress.testpress.viewmodel.PerformanceAnalysisViewModel
import `in`.testpress.testpress.viewmodel.PerformanceViewModelFactory
import javax.inject.Inject

class PerformanceResultsActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: PerformanceAnalysisRepository

    private lateinit var binding: ActivityPerformanceResultsBinding
    private lateinit var viewModel: PerformanceAnalysisViewModel
    private var tabSelectedListener: TabLayout.OnTabSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        binding = ActivityPerformanceResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        viewModel = ViewModelProvider(
            this,
            PerformanceViewModelFactory(repository)
        )[PerformanceAnalysisViewModel::class.java]
        viewModel.setStudent(getStudentNo())

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

        if (viewModel.studentNo.isNotBlank()) {
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
                viewModel.onFiltersLoaded(state.data)

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
        viewModel.requestFilters(selectedType)
    }

    private fun requestOverview() {
        if (!requireStudent()) return
        viewModel.requestOverview()
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
                PerformanceFormatter.formatNumber(summary.bestScore),
                PerformanceFormatter.formatNumber(summary.bestMax)
            )
        } else {
            empty
        }
        binding.statBestNote.text = summary.bestDate?.takeIf { it.isNotBlank() }?.let {
            getString(R.string.pa_stat_best_score_note_format, it)
        } ?: empty

        binding.statAvg.text = summary.avgScore?.let { PerformanceFormatter.formatNumber(it) } ?: empty
        binding.statAvgNote.text = summary.avgPercent?.let {
            getString(R.string.pa_stat_avg_score_note_format, PerformanceFormatter.formatDecimal(it))
        } ?: empty

        binding.statRank.text = summary.bestRank?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: empty
        binding.statRankNote.text = summary.bestRankOutOf?.let {
            getString(R.string.pa_stat_best_rank_note_format, PerformanceFormatter.formatIntegerWithCommas(it))
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
            getString(R.string.pa_rank_percentile_format, PerformanceFormatter.formatDecimal(it))
        } ?: empty

        binding.rankPosition.text = rank.bestRank?.let {
            PerformanceFormatter.formatIntegerWithCommas(it)
        } ?: empty

        binding.rankAppeared.text = rank.appeared?.let {
            getString(R.string.pa_rank_appeared_format, PerformanceFormatter.formatIntegerWithCommas(it))
        } ?: ""

        if (rank.bestRank != null && rank.appeared != null && rank.percentile != null) {
            binding.rankInsight.text = getString(
                R.string.pa_rank_insight_format,
                PerformanceFormatter.formatIntegerWithCommas(rank.bestRank),
                PerformanceFormatter.formatIntegerWithCommas(rank.appeared),
                PerformanceFormatter.formatDecimal(rank.percentile)
            )
        } else {
            binding.rankInsight.text = empty
        }
    }

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
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(PerformanceFormatter.getMonthName(this, it)) }
        setSpinnerAdapter(binding.fMonth, monthItems)

        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.fYear, yearItems)

        val defaultYear = viewModel.selectedAcademicYear ?: data.years.firstOrNull()
        if (defaultYear != null) {
            val yearPos = data.years.indexOf(defaultYear)
            if (yearPos != -1) {
                binding.fYear.setSelection(yearPos + 1)
            }
        }

        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (_, label) -> subjectItems.add(label) }
        setSpinnerAdapter(binding.fSubject, subjectItems)
    }

    private fun requestDashboardData() {
        if (!requireStudent()) return
        viewModel.requestDashboard(
            binding.fMonth.selectedItemPosition,
            binding.fYear.selectedItemPosition,
            binding.fSubject.selectedItemPosition
        )
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

    // ── Topper Comparison Leaderboard ─────────────────────────────────────

    private fun setupLeaderboard() {
        attachUserSelectListener(binding.lMonth) { requestLeaderboardData() }
        attachUserSelectListener(binding.lYear) { requestLeaderboardData() }
        attachUserSelectListener(binding.lLimit) { requestLeaderboardData() }
        attachUserSelectListener(binding.lSubject) { requestLeaderboardData() }
    }

    private fun populateLeaderboardSpinners(data: PerformanceFiltersData) {
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(PerformanceFormatter.getMonthName(this, it)) }
        setSpinnerAdapter(binding.lMonth, monthItems)

        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.lYear, yearItems)

        val limitItems = listOf(
            getString(R.string.pa_limit_last_5),
            getString(R.string.pa_limit_last_10),
            getString(R.string.pa_limit_last_20),
            getString(R.string.pa_limit_all)
        )
        setSpinnerAdapter(binding.lLimit, limitItems)

        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (_, label) -> subjectItems.add(label) }
        setSpinnerAdapter(binding.lSubject, subjectItems)
    }

    private fun requestLeaderboardData() {
        if (!requireStudent()) return
        binding.topperProgress.visibility = View.VISIBLE
        viewModel.requestLeaderboard(
            binding.lMonth.selectedItemPosition,
            binding.lYear.selectedItemPosition,
            binding.lLimit.selectedItemPosition,
            binding.lSubject.selectedItemPosition
        )
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
            rowBinding.tvScore.text = row.score?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash
            rowBinding.tvTopper.text = row.topper?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash
            rowBinding.tvPhy.text = row.physics?.raw?.takeIf { it.isNotBlank() } ?: emptyDash
            rowBinding.tvChe.text = row.chemistry?.raw?.takeIf { it.isNotBlank() } ?: emptyDash

            val compValue = row.comparisonSubject?.raw ?: row.biology?.raw
            rowBinding.tvComparisonSub.text = compValue?.takeIf { it.isNotBlank() } ?: emptyDash

            rowBinding.tvOutOf.text = row.outOf?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash

            val gap = row.gap
            if (gap != null) {
                rowBinding.tvGap.text = PerformanceFormatter.formatNumber(gap)
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

            rowBinding.tvRank.text = row.rank?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash
            rowBinding.tvAppeared.text = row.appeared?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash

            val progress = row.progress ?: 0.0
            val gapPercent = row.gapPercent ?: 0.0
            rowBinding.pbProgress.progress = progress.toInt().coerceIn(0, 100)
            rowBinding.tvProgressCaption.text = getString(
                R.string.pa_topper_progress_format,
                PerformanceFormatter.formatDecimal(progress),
                PerformanceFormatter.formatDecimal(gapPercent)
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
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(PerformanceFormatter.getMonthName(this, it)) }
        setSpinnerAdapter(binding.sMonth, monthItems)

        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.sYear, yearItems)

        val limitItems = listOf(
            getString(R.string.pa_limit_last_5),
            getString(R.string.pa_limit_last_10),
            getString(R.string.pa_limit_last_20),
            getString(R.string.pa_limit_all)
        )
        setSpinnerAdapter(binding.sLimit, limitItems)

        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (_, label) -> subjectItems.add(label) }
        setSpinnerAdapter(binding.sSubject, subjectItems)
    }

    private fun requestSubjectBoardData() {
        if (!requireStudent()) return
        binding.subjectProgress.visibility = View.VISIBLE
        viewModel.requestSubjectBoard(
            binding.sMonth.selectedItemPosition,
            binding.sYear.selectedItemPosition,
            binding.sLimit.selectedItemPosition,
            binding.sSubject.selectedItemPosition
        )
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
        PerformanceSubjectTableHelper.renderSubjectTableHeader(this, binding.subjectTable, columns)

        val altRowColor = ContextCompat.getColor(this, R.color.pa_table_row_alt)
        val defaultRowColor = Color.WHITE

        subjectboard.data.forEachIndexed { index, row ->
            val dataRow = PerformanceSubjectTableHelper.createSubjectDataRow(
                this,
                row,
                columns,
                if (index % 2 == 1) altRowColor else defaultRowColor
            )
            binding.subjectTable.addView(dataRow)
        }
    }

    // ── Exam History ──────────────────────────────────────────────────────

    private fun setupHistory() {
        binding.btnHistoryPrev.setOnClickListener {
            if (viewModel.currentHistoryPage > 1) {
                requestHistoryPage(viewModel.currentHistoryPage - 1)
            }
        }
        binding.btnHistoryNext.setOnClickListener {
            if (viewModel.currentHistoryPage < viewModel.totalHistoryPages) {
                requestHistoryPage(viewModel.currentHistoryPage + 1)
            }
        }

        attachUserSelectListener(binding.hMonth) {
            requestHistoryPage(1)
        }
        attachUserSelectListener(binding.hYear) {
            requestHistoryPage(1)
        }
        attachUserSelectListener(binding.hSubject) {
            requestHistoryPage(1)
        }
    }

    private fun populateHistorySpinners(data: PerformanceFiltersData) {
        val monthItems = mutableListOf(getString(R.string.pa_trend_filter_month))
        data.months.forEach { monthItems.add(PerformanceFormatter.getMonthName(this, it)) }
        setSpinnerAdapter(binding.hMonth, monthItems)

        val yearItems = mutableListOf(getString(R.string.pa_trend_filter_all_years))
        data.years.forEach { yearItems.add(it.toString()) }
        setSpinnerAdapter(binding.hYear, yearItems)

        val subjectItems = mutableListOf(getString(R.string.pa_trend_filter_subject))
        data.subjects.forEach { (_, label) -> subjectItems.add(label) }
        setSpinnerAdapter(binding.hSubject, subjectItems)
    }

    private fun requestHistoryPage(page: Int) {
        if (!requireStudent()) return
        binding.historyProgress.visibility = View.VISIBLE
        viewModel.requestHistoryPage(
            page,
            binding.hMonth.selectedItemPosition,
            binding.hYear.selectedItemPosition,
            binding.hSubject.selectedItemPosition
        )
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

            rowBinding.tvTotal.text = row.total?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash
            rowBinding.tvMax.text = row.maxMarks?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash
            rowBinding.tvHighest.text = row.highest?.let { PerformanceFormatter.formatNumber(it) } ?: emptyDash
            rowBinding.tvPercent.text = row.percent?.let { PerformanceFormatter.formatDecimal(it) } ?: emptyDash

            val grade = row.grade?.takeIf { it.isNotBlank() }
            if (grade != null && grade != "-") {
                rowBinding.tvGrade.text = grade
            } else {
                rowBinding.tvGrade.text = emptyDash
            }
            rowBinding.tvGrade.setBackgroundResource(R.drawable.pa_badge_success_bg)
            rowBinding.tvGrade.setTextColor(ContextCompat.getColor(this, R.color.pa_badge_success_text))

            rowBinding.tvRank.text = row.rank?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash
            rowBinding.tvAppeared.text = row.appeared?.let { PerformanceFormatter.formatIntegerWithCommas(it) } ?: emptyDash
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

        viewModel.updateHistoryPagination(history)
        binding.tvHistoryPage.text = viewModel.currentHistoryPage.toString()

        val from = history.from ?: if (history.total > 0) ((history.page - 1) * history.length + 1) else 0
        val to = history.to ?: (from + history.data.size - 1).coerceAtMost(history.total)
        binding.historyInfo.text = getString(R.string.pa_history_info_format, from, to, history.total)

        binding.btnHistoryPrev.isEnabled = viewModel.currentHistoryPage > 1
        binding.btnHistoryPrev.alpha = if (binding.btnHistoryPrev.isEnabled) 1.0f else 0.4f

        binding.btnHistoryNext.isEnabled = viewModel.currentHistoryPage < viewModel.totalHistoryPages
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
        val style = PerformanceFormatter.getFocusStyle(focus.priority)

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

        val nameCol = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvIcon = TextView(this).apply {
            text = style.priorityIcon
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(style.priorityColor)
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
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        titleCol.addView(tvSubject)

        val scoreStr = PerformanceFormatter.formatFocusScore(this, focus.avg, focus.avgMax, focus.percent)
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

        val topperScoreHtml = PerformanceFormatter.formatFocusTopperScoreHtml(this, focus.topperAvg, focus.avgMax, focus.topperPercent)
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
                ?: getString(style.defaultPriorityLabelRes)
            text = label
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(style.badgeTextColor)
            val padH = (10 * density).toInt()
            val padV = (4 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(style.badgeBgColor)
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
            setTextColor(style.noteTextColor)
            textSize = 12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val padH = (12 * density).toInt()
            val padV = (10 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = GradientDrawable().apply {
                setColor(style.noteBgColor)
                setStroke((1 * density).toInt(), style.noteBorderColor)
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
            progressColor = style.priorityColor
            text = "${PerformanceFormatter.formatDecimal(focus.percent ?: 0.0)}%"
        }
        midRow.addView(donutView)
        container.addView(midRow)

        // ── 3. pa-focus-chartlbl & pa-focus-chart ────────────
        if (focus.points.isNotEmpty()) {
            val tvChartLbl = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (14 * density).toInt()
                    bottomMargin = (8 * density).toInt()
                }
                text = PerformanceFormatter.formatFocusTrendLabel(this@PerformanceResultsActivity, focus.points.size)
                setTextColor(ContextCompat.getColor(this@PerformanceResultsActivity, R.color.pa_stat_top_label))
                textSize = 11f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            container.addView(tvChartLbl)

            val miniChart = LineChart(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (74 * density).toInt()
                )
                setDescription("")
                legend.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setViewPortOffsets(0f, (6 * density), 0f, (16 * density))

                val gridColor = Color.parseColor("#F1F5F9")
                val labelColor = Color.parseColor("#94A3B8")

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawAxisLine(false)
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
                val topperDs = LineDataSet(topperEntries, getString(R.string.pa_focus_topper_series)).apply {
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
                val studentDs = LineDataSet(studentEntries, getString(R.string.pa_focus_you_series)).apply {
                    color = style.priorityColor
                    setCircleColor(style.priorityColor)
                    circleRadius = 3f
                    lineWidth = 2f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.15f
                    setDrawFilled(true)
                    fillColor = style.priorityColor
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
                Toast.makeText(this, R.string.pa_omr_error_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private fun requireStudent(): Boolean {
        if (viewModel.studentNo.isNotBlank()) return true
        Toast.makeText(this, R.string.performance_results_no_student, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun getStudentNo(): String {
        val manager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts: Array<Account> = manager.getAccountsByType(APPLICATION_ID)
        return accounts.firstOrNull()?.name ?: ""
    }
}

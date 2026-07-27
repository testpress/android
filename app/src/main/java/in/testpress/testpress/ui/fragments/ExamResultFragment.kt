package `in`.testpress.testpress.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import `in`.testpress.testpress.R
import `in`.testpress.testpress.network.ExamType
import `in`.testpress.testpress.repository.ExamResultRepository
import `in`.testpress.testpress.ui.adapters.ExamResultAdapter
import `in`.testpress.testpress.viewmodel.ExamResultViewModel


class ExamResultFragment : Fragment() {

    companion object {
        private const val ARG_STUDENT_NO = "student_no"

        fun newInstance(studentNo: String): ExamResultFragment {
            return ExamResultFragment().apply {
                arguments = Bundle().also { it.putString(ARG_STUDENT_NO, studentNo) }
            }
        }
    }

    private lateinit var viewModel: ExamResultViewModel
    private lateinit var adapter: ExamResultAdapter

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerResults: RecyclerView
    private lateinit var loadingContainer: FrameLayout
    private lateinit var emptyContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnPrevious: TextView
    private lateinit var btnNext: TextView
    private lateinit var pageNumbersContainer: LinearLayout
    private lateinit var paginationBar: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_exam_result, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        setupTabs()
        setupPagination()

        viewModel = ViewModelProvider(this)[ExamResultViewModel::class.java]
        observeViewModel()

        val studentNo = arguments?.getString(ARG_STUDENT_NO) ?: ""

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val limit = calculateOptimalPageLimit(view)
                if (!viewModel.isInitialized()) {
                    viewModel.initialize(studentNo, limit)
                } else {
                    viewModel.updatePageLimit(limit)
                }
            }
        })
    }

    private fun calculateOptimalPageLimit(rootView: View): Int {
        val density = resources.displayMetrics.density

        val tabH    = tabLayout.height.takeIf { it > 0 } ?: (48 * density).toInt()
        val paginH  = paginationBar.height.takeIf { it > 0 } ?: (56 * density).toInt()
        val headerH = (46 * density).toInt()
        val margins = (24 * density).toInt()
        val rowH    = (48 * density).toInt()

        val available = (rootView.height - tabH - headerH - paginH - margins)
            .coerceAtLeast(rowH)
        val limit = (available / rowH).coerceIn(5, 20)

        return limit
    }

    private fun bindViews(view: View) {
        tabLayout = view.findViewById(R.id.tab_layout)
        recyclerResults = view.findViewById(R.id.recycler_results)
        loadingContainer = view.findViewById(R.id.loading_container)
        emptyContainer = view.findViewById(R.id.empty_container)
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message)
        btnRetry = view.findViewById(R.id.btn_retry)
        btnPrevious = view.findViewById(R.id.btn_previous)
        btnNext = view.findViewById(R.id.btn_next)
        pageNumbersContainer = view.findViewById(R.id.page_numbers_container)
        paginationBar = view.findViewById(R.id.pagination_bar)
    }

    private fun setupRecyclerView() {
        adapter = ExamResultAdapter()
        recyclerResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExamResultFragment.adapter
            isNestedScrollingEnabled = true
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.exam_results_model_tab))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.exam_results_weekly_tab))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val examType = if (tab.position == 0) ExamType.MODEL else ExamType.WEEKLY
                viewModel.changeExamType(examType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupPagination() {
        btnPrevious.setOnClickListener { viewModel.goToPreviousPage() }
        btnNext.setOnClickListener { viewModel.goToNextPage() }
        btnRetry.setOnClickListener { viewModel.retry() }
    }


    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            loadingContainer.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.results.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
            if (results.isEmpty() && viewModel.isLoading.value != true) {
                showEmptyState(getString(R.string.no_results))
            } else {
                emptyContainer.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                showEmptyState(getString(R.string.error_loading_results))
            }
        }

        viewModel.currentPage.observe(viewLifecycleOwner) { _ ->
            refreshPaginationBar()
        }

        viewModel.totalPages.observe(viewLifecycleOwner) { _ ->
            refreshPaginationBar()
        }
    }

    private fun refreshPaginationBar() {
        val current = viewModel.currentPage.value ?: 1
        val total = viewModel.totalPages.value ?: 1

        btnPrevious.isEnabled = current > 1
        btnPrevious.alpha = if (current > 1) 1f else 0.4f

        btnNext.isEnabled = current < total
        btnNext.alpha = if (current < total) 1f else 0.4f

        paginationBar.visibility = if (total > 1) View.VISIBLE else View.GONE

        pageNumbersContainer.removeAllViews()
        val pages = buildPageList(current, total)
        pages.forEach { page ->
            if (page == -1) {
                pageNumbersContainer.addView(buildEllipsisLabel())
            } else {
                pageNumbersContainer.addView(buildPageChip(page, page == current))
            }
        }
    }

    private fun buildPageList(current: Int, total: Int): List<Int> {
        if (total <= 5) return (1..total).toList()

        val pages = mutableListOf<Int>()
        
        pages.add(1)

        if (current <= 3) {
            pages.add(2)
            pages.add(3)
            pages.add(-1)
            pages.add(total)
        } else if (current >= total - 2) {
            pages.add(-1) 
            pages.add(total - 2)
            pages.add(total - 1)
            pages.add(total)
        } else {
            pages.add(current - 1)
            pages.add(current)
            pages.add(current + 1)
            pages.add(-1) 
            pages.add(total)
        }

        return pages
    }

    private fun buildPageChip(page: Int, isActive: Boolean): TextView {
        val ctx = requireContext()
        val chipSize = (36 * resources.displayMetrics.density).toInt()
        
        return TextView(ctx).apply {
            text = page.toString()
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            includeFontPadding = false
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setOnClickListener { if (!isActive) viewModel.goToPage(page) }

            val layoutParams = LinearLayout.LayoutParams(chipSize, chipSize).also {
                it.setMargins(6, 0, 6, 0)
            }
            this.layoutParams = layoutParams

            if (isActive) {
                setBackgroundResource(R.drawable.bg_page_number_active)
                setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            } else {
                setBackgroundResource(R.drawable.bg_pagination_button)
                setTextColor(ContextCompat.getColor(ctx, R.color.primary))
            }
        }
    }

    private fun buildEllipsisLabel(): TextView {
        return TextView(requireContext()).apply {
            text = "…"
            textSize = 13f
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(4, 0, 4, 0) }
            setTextColor(ContextCompat.getColor(requireContext(), R.color.exam_result_cell_text))
        }
    }

    private fun showEmptyState(message: String) {
        tvEmptyMessage.text = message
        emptyContainer.visibility = View.VISIBLE
        paginationBar.visibility = View.GONE
    }
}

package `in`.testpress.testpress.ui.fragments

import `in`.testpress.enums.Status
import `in`.testpress.network.Resource
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.models.pojo.DashboardSection
import `in`.testpress.testpress.repository.DashBoardRepository
import `in`.testpress.testpress.ui.adapters.DashboardAdapter
import `in`.testpress.testpress.util.PreferenceManager
import `in`.testpress.testpress.viewmodel.DashBoardViewModel
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.ButterKnife
import butterknife.InjectView
import com.facebook.shimmer.ShimmerFrameLayout
import io.sentry.Sentry
import io.sentry.protocol.User
import javax.inject.Inject

class DashBoardFragment : Fragment() {

    @InjectView(R.id.recycler_View)
    lateinit var recyclerView: RecyclerView

    @InjectView(R.id.empty_container)
    lateinit var emptyView: LinearLayout

    @InjectView(R.id.empty_title)
    lateinit var emptyTitleView: TextView

    @InjectView(R.id.empty_description)
    lateinit var emptyDescView: TextView

    @InjectView(R.id.retry_button)
    lateinit var retryButton: Button

    @InjectView(R.id.swipe_container)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @InjectView(R.id.shimmer_view_container)
    lateinit var loadingPlaceholder: ShimmerFrameLayout

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    private lateinit var adapter: DashboardAdapter
    lateinit var exception: Exception
    lateinit var dashboardResponse: DashboardResponse
    private lateinit var viewModel: DashBoardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.inject(this)
        super.onCreate(savedInstanceState)

        initViewModel()
        setUsernameInSentry()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashBoardViewModel(DashBoardRepository(requireContext())) as T
            }
        }).get(DashBoardViewModel::class.java)
    }

    private fun setUsernameInSentry() {
        val manager = activity!!.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val account = manager.getAccountsByType(BuildConfig.APPLICATION_ID)
        if (account.isNotEmpty()) {
            val user = User()
            user.username = account[0].name
            Sentry.setUser(user)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Injector.inject(this)
        activity!!.invalidateOptionsMenu()
        return inflater.inflate(R.layout.dashboard_view, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.inject(this, view)

        setRecycleViewAdapter()
        showDataFromCacheIfAvailable()
        addOnClickListeners()
    }

    private fun setRecycleViewAdapter() {
        adapter = DashboardAdapter(context, DashboardResponse(), serviceProvider)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        swipeRefreshLayout.isEnabled = true
    }

    private fun showDataFromCacheIfAvailable() {
        if (getSections().isNotEmpty()) {
            adapter.setResponse(PreferenceManager.getDashboardDataPreferences(requireContext()))
        } else {
            loadData()
        }
    }

    private fun getSections(): List<DashboardSection> {
        return PreferenceManager.getDashboardDataPreferences(context).availableSections
    }

    private fun addOnClickListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isEnabled = true
            refresh()
        }
        retryButton.setOnClickListener {
            swipeRefreshLayout.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = true
            emptyView.visibility = View.GONE
            refresh()
        }
    }

    fun refresh() {
        if (activity != null) {
            loadData()
        }
    }

    private fun loadData() {
        viewModel.loadData().observe(
            viewLifecycleOwner,
            Observer<Resource<DashboardResponse>> { dashBoard ->
                showLoadingImage()
                when (dashBoard.status) {
                    Status.SUCCESS -> {
                        swipeRefreshLayout.isRefreshing = false
                        adapter.setResponse(dashBoard.data)
                        hideShimmer()
                    }
                    Status.ERROR -> {
                        hideShimmer()
                        setEmptyText()
                    }
                    else -> {}
                }
            })
    }

    private fun showLoadingImage() {
        loadingPlaceholder.visibility = View.VISIBLE
        loadingPlaceholder.startShimmer()
    }

    private fun hideShimmer() {
        loadingPlaceholder.stopShimmer()
        loadingPlaceholder.visibility = View.GONE
    }

    private fun setEmptyText() {
        setEmptyText(
            R.string.no_data_available, R.string.try_after_some_time,
            R.drawable.ic_error_outline_black_18dp
        )
    }

    private fun setEmptyText(title: Int, description: Int, icon: Int) {
        emptyView.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        emptyDescView.setText(description)
        retryButton.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}
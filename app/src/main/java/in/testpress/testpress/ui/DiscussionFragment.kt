package `in`.testpress.testpress.ui

import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.ui.adapters.DiscussionsAdapter
import android.accounts.AccountsException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.discussion_list.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

public class DiscussionFragment: Fragment() {
    @Inject
    public lateinit var testpressService: TestpressService
    @Inject
    public lateinit var serviceProvider: TestpressServiceProvider

    private val adapter = DiscussionsAdapter()

    val viewModel: DiscussionViewModel by viewModels {
        DiscussionViewModelFactory(requireActivity().application, testpressService)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.discussion_list, container, false)
        ButterKnife.inject(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        fetchPosts()
    }

    private fun fetchPosts() {
        Log.d("TAG", "fetchPosts: ")
        lifecycleScope.launch {
            viewModel.fetchPosts().collectLatest { pagingData ->
                Log.d("TAG", "fetchPosts: ${pagingData}")
                adapter.submitData(pagingData)
            }
        }
    }

    private fun setupViews() {
        rvPosts.adapter = adapter
    }

    private fun getTestpressApiService(): TestpressService {
        try {
            testpressService = serviceProvider.getService(activity)
        } catch (e: AccountsException) {
        } catch (e: IOException) {
        }
        return testpressService
    }
}
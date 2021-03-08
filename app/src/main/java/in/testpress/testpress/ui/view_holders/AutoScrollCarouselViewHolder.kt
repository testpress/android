package `in`.testpress.testpress.ui.view_holders

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.models.pojo.DashboardSection
import `in`.testpress.testpress.ui.adapters.ImageOnlyBannerCarouselAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper


class AutoScrollCarouselViewHolder(itemView: View, context: Context): BaseCarouselViewHolder(itemView, context) {
    private val backgroundTaskRunner: Handler
    private val SCROLL_DELAY = 3000L
    private var isScrollStarted = false

    init {
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        backgroundTaskRunner = Handler(Looper.getMainLooper())
    }

    fun display(response: DashboardResponse, context: Context) {
        val section = response.availableSections[adapterPosition]
        initRecyclerView(section, context, response)
        displayTitle(section.displayName)
        if (section.items.size > 2) {
            showPageIndicator()
        }
    }

    private fun initRecyclerView(section: DashboardSection, context: Context, response: DashboardResponse) {
        val adapter = ImageOnlyBannerCarouselAdapter(response, section, context)
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        enableAutoScroll()
    }

    private fun enableAutoScroll() {
        if (!isScrollStarted) {
            backgroundTaskRunner.post(object : Runnable {
                override fun run() {
                    scrollBanner()
                    backgroundTaskRunner.postDelayed(this, SCROLL_DELAY)
                }
            })
            isScrollStarted = true
        }
    }

    private fun scrollBanner() {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val adapter = recyclerView.adapter!!

        if (linearLayoutManager.findLastCompletelyVisibleItemPosition() < adapter.itemCount - 1) {
            recyclerView.smoothScrollToPosition(linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1)
        } else if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
            recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun displayTitle(displayName: String) {
        title.text = displayName
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_news_black, 0, 0, 0)
    }
}

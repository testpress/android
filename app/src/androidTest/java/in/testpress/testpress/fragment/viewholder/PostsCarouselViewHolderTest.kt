package `in`.testpress.testpress.fragment.viewholder

import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.ui.adapters.DashboardAdapter
import `in`.testpress.testpress.ui.fragments.DashboardFragment
import `in`.testpress.testpress.ui.view_holders.PostsCarouselViewHolder
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.testpress.R
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostsCarouselViewHolderTest {

    private val testActivityRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Rule
    fun rule() = testActivityRule

    private lateinit var dashBoardFragment: DashboardFragment

    private var context: Context? = null

    @Before
    fun setUp() {
        dashBoardFragment = DashboardFragment()
        context = testActivityRule.activity
    }

    @Test
    fun testCreateViewHolderShouldInflatePostViewHolder() {
        val adapter = DashboardAdapter(context, DashboardResponse(), null)
        val parent = LinearLayout(context)

        val viewHolder = adapter.onCreateViewHolder(parent,3)
        TestCase.assertTrue(viewHolder is PostsCarouselViewHolder)
    }

    @Test
    fun testPostViewHolderShouldDisplayPostTitle() {
        val adapter = DashboardAdapter(context, DashboardResponse(), null)
        val parent = LinearLayout(context)
        parent.orientation = LinearLayout.VERTICAL
        val viewHolder = adapter.onCreateViewHolder(parent, 3) as PostsCarouselViewHolder
        val titleView = viewHolder.itemView.findViewById<View>(R.id.title)
        Assert.assertNotNull(titleView)
    }

    @Test
    fun testPostViewHolderDisplaysPageIndicator() {
        val adapter = DashboardAdapter(context, DashboardResponse(), null)
        val parent = LinearLayout(context)
        parent.orientation = LinearLayout.VERTICAL
        val viewHolder = adapter.onCreateViewHolder(parent, 3) as PostsCarouselViewHolder
        val indicatorView = viewHolder.itemView.findViewById<View>(R.id.indicator)
        Assert.assertNotNull(indicatorView)
    }

    @Test
    fun testPostViewHolderDisplaysInGrid() {
        val adapter = DashboardAdapter(context, DashboardResponse(), null)
        val parent = LinearLayout(context)
        parent.orientation = LinearLayout.VERTICAL
        val viewHolder = adapter.onCreateViewHolder(parent, 3) as PostsCarouselViewHolder
        val recyclerView = viewHolder.itemView.findViewById<RecyclerView>(R.id.inner_recyclerView)
        Assert.assertNotNull(recyclerView)
        Assert.assertTrue(recyclerView.layoutManager is GridLayoutManager)
    }
}

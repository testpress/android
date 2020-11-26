package `in`.testpress.testpress.fragment.viewholder

import `in`.testpress.testpress.models.pojo.DashboardResponse
import `in`.testpress.testpress.ui.MainActivity
import `in`.testpress.testpress.ui.adapters.DashboardAdapter
import `in`.testpress.testpress.ui.fragments.DashboardFragment
import android.view.View
import android.widget.LinearLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
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

    @Before
    fun setUp() {
        dashBoardFragment = DashboardFragment()
    }

    @Test
    fun whenDashboardResponseHasPostPostShouldBeVisible() {
        dashBoardFragment.adapter = DashboardAdapter(testActivityRule.activity, DashboardResponse(), null)
        val result = dashBoardFragment.adapter.createViewHolder(LinearLayout(testActivityRule.activity), 3).itemView
        Assert.assertEquals(View.VISIBLE, result.visibility)
    }
}

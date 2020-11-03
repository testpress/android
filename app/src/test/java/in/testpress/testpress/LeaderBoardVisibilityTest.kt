package `in`.testpress.testpress

import `in`.testpress.testpress.ui.fragments.DashboardFragment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [18])
@RunWith(AndroidJUnit4::class)
class LeaderBoardVisibilityTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    (expected = NoMatchingViewException::class)
    fun whenLeaderBoardIsDisabled_LeaderBoardViewWillBeInVisible() {
        launchFragmentInContainer<DashboardFragment>()
        onView(withId(R.id.see_all)).check(matches(isDisplayed()))
    }

    @Test
    fun whenLeaderBoardIsEnabled_LeaderBoardViewWillBeVisible() {
        launchFragmentInContainer<DashboardFragment>()
        onView(withId(R.id.see_all)).check(matches(isDisplayed()))
    }
}
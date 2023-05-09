package `in`.testpress.testpress.fragment

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import `in`.testpress.testpress.R
import `in`.testpress.testpress.ui.fragments.WebViewFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewFragmentTest {

    @Test
    fun testWebViewFragment() {
        val url = "https://www.google.com"
        val bundle = WebViewFragment.createArguments(url)

        // Launch the fragment
        val scenario: FragmentScenario<WebViewFragment> = launchFragmentInContainer(
            bundle, R.style.AppTheme
        )

        // Check that the web view is displayed
        Espresso.onView(ViewMatchers.withId(R.id.web_view))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    private fun WebViewFragment.Companion.createArguments(url: String): Bundle {
        return Bundle().apply {
            putString(URL_TO_OPEN, url)
        }
    }
}
package `in`.testpress.testpress.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.databinding.ContainerLayoutBinding
import `in`.testpress.testpress.models.SsoUrl
import `in`.testpress.testpress.util.SafeAsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import androidx.appcompat.widget.Toolbar
import java.io.IOException
import javax.inject.Inject

class QotdActivity : TestpressFragmentActivity(), EmptyViewListener {

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var binding: ContainerLayoutBinding
    private var redrawRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        binding = ContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.root.findViewById<Toolbar>(R.id.toolbar_actionbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daily Questions"
        initializeEmptyViewFragment()
        showLoading()
        fetchSsoLink()
    }

    private fun fetchSsoLink() {
        object : SafeAsyncTask<SsoUrl?>() {
            @Throws(Exception::class)
            override fun call(): SsoUrl? {
                return serviceProvider.getService(this@QotdActivity)?.getSsoUrl()
            }

            override fun onException(exception: java.lang.Exception?) {
                super.onException(exception)
                hideLoading()
                showErrorView(exception)
            }

            override fun onSuccess(ssoLink: SsoUrl?) {
                super.onSuccess(ssoLink)
                hideLoading()
                val ssoUrlPath = ssoLink?.ssoUrl
                if (ssoUrlPath.isNullOrEmpty()) {
                    showErrorView(Exception("Failed to retrieve a valid SSO URL"))
                } else {
                    val url = BuildConfig.BASE_URL + ssoUrlPath + "&next=/qotd/"
                    loadWebView(url)
                }
            }
        }.execute()
    }

    private fun loadWebView(url: String) {
        val fragment = WebViewFragment()
        fragment.arguments = Bundle().apply {
            putString(WebViewFragment.URL_TO_OPEN, url)
            putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, false)
            putBoolean(WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, false)
            putBoolean(WebViewFragment.ALLOW_VALIDATION_ERRORS, true)
            putInt(WebViewFragment.CACHE_MODE, WebSettings.LOAD_NO_CACHE)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        forceChartRedraw()
    }

    private fun forceChartRedraw() {
        val runnable = object : Runnable {
            var attempts = 0
            override fun run() {
                if (attempts >= 8) return // Try for 8 seconds
                attempts++
                val webView = findWebView(binding.root)
                webView?.evaluateJavascript("window.dispatchEvent(new Event('resize'));", null)
                handler.postDelayed(this, 1000)
            }
        }
        redrawRunnable = runnable
        handler.postDelayed(runnable, 1500)
    }

    private fun findWebView(view: View): android.webkit.WebView? {
        if (view is android.webkit.WebView) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findWebView(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    private fun showLoading() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.pbLoading.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
    }

    private fun showErrorView(exception: java.lang.Exception?) {
        if (exception?.cause is IOException) {
            val testpressException = TestpressException.networkError(exception.cause as IOException)
            emptyViewFragment.displayError(testpressException)
        } else {
            val testpressException = TestpressException.unexpectedError(exception)
            emptyViewFragment.displayError(testpressException)
        }
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, emptyViewFragment)
            .commit()
    }

    override fun onRetryClick() {
        showLoading()
        fetchSsoLink()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        redrawRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
}

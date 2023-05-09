package `in`.testpress.testpress.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.testpress.BuildConfig.BASE_URL
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.SsoUrl
import `in`.testpress.testpress.ui.fragments.WebViewFragment
import `in`.testpress.testpress.util.SafeAsyncTask
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.container_layout.*
import java.io.IOException
import javax.inject.Inject

class StudentReportActivity: TestpressFragmentActivity(), EmptyViewListener {

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    lateinit var emptyViewFragment: EmptyViewFragment
    lateinit var webViewFragment: WebViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.container_layout)
        showLoading()
        initializeEmptyViewFragment()
        initializeWebViewFragment()
        fetchSsoLink()
        supportActionBar?.title = "Report"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showLoading() {
        pb_loading.visibility = View.VISIBLE
        fragment_container.visibility = View.GONE
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, emptyViewFragment)
        transaction.commit()
    }

    private fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment()
    }

    private fun fetchSsoLink() {
        object : SafeAsyncTask<SsoUrl?>() {
            @Throws(Exception::class)
            override fun call(): SsoUrl {
                return serviceProvider.getService(this@StudentReportActivity).ssoUrl
            }

            override fun onException(exception: java.lang.Exception?) {
                super.onException(exception)
                hideLoading()
                showErrorView(exception)
            }

            override fun onSuccess(ssoLink: SsoUrl?) {
                super.onSuccess(ssoLink)
                emptyViewFragment.hide()
                hideLoading()
                openTicketsInWebview(ssoLink)
            }
        }.execute()
    }

    private fun hideLoading() {
        pb_loading.visibility = View.GONE
        fragment_container.visibility = View.VISIBLE
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

    private fun openTicketsInWebview(ssoLink: SsoUrl?) {
        val arguments = Bundle()
        arguments.putString(WebViewFragment.URL_TO_OPEN, "$BASE_URL${ssoLink?.ssoUrl}&next=/report/")
        webViewFragment.arguments = arguments
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, webViewFragment)
        transaction.commit()
    }

    override fun onRetryClick() {
        fetchSsoLink()
    }

    override fun onBackPressed() {
        if (webViewFragment.canGoBack()) {
            webViewFragment.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
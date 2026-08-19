package `in`.testpress.testpress.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.databinding.ContainerLayoutBinding
import `in`.testpress.testpress.models.SsoUrl
import `in`.testpress.testpress.util.SafeAsyncTask
import android.content.Intent
import android.os.Bundle
import android.view.View
import java.io.IOException
import javax.inject.Inject

class SSOWebViewRedirectActivity: TestpressFragmentActivity(), EmptyViewListener {
    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var binding: ContainerLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        binding = ContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchSsoLink()
        initializeEmptyViewFragment()
        showLoading()
    }

    fun fetchSsoLink() {
        object : SafeAsyncTask<SsoUrl?>() {
            @Throws(Exception::class)
            override fun call(): SsoUrl {
                return serviceProvider.getService(this@SSOWebViewRedirectActivity).getSsoUrl()
            }

            override fun onException(exception: java.lang.Exception?) {
                super.onException(exception)
                hideLoading()
                showErrorView(exception)
            }

            override fun onSuccess(ssoLink: SsoUrl?) {
                super.onSuccess(ssoLink)
                openTicketsInWebview(ssoLink)
            }
        }.execute()
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

    private fun openTicketsInWebview(ssoLink: SsoUrl?) {
        val webviewIntent = Intent(this@SSOWebViewRedirectActivity, WebViewActivity::class.java)
        webviewIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Doubts"
        val nextPath = intent.getStringExtra(EXTRA_NEXT_PATH) ?: "/tickets/mobile/"
        val allowExternal = intent.getBooleanExtra(EXTRA_ALLOW_EXTERNAL, false)
        webviewIntent.putExtra(WebViewActivity.ACTIVITY_TITLE, title)
        webviewIntent.putExtra(WebViewActivity.ENABLE_BACK, true)
        webviewIntent.putExtra(WebViewActivity.SHOW_LOADING, false)
        webviewIntent.putExtra(WebViewActivity.ALLOW_EXTERNAL_LINK, allowExternal)
        webviewIntent.putExtra(
            WebViewActivity.URL_TO_OPEN,
            BuildConfig.WHITE_LABELED_HOST_URL + ssoLink?.ssoUrl + "&next=" + nextPath
        )
        startActivity(webviewIntent)
        finish()
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, emptyViewFragment)
        transaction.commit()
    }

    override fun onRetryClick() {
        fetchSsoLink()
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_NEXT_PATH = "nextPath"
        const val EXTRA_ALLOW_EXTERNAL = "allowExternal"
    }
}
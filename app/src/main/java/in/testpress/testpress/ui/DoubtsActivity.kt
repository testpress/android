package `in`.testpress.testpress.ui

import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.SsoUrl
import `in`.testpress.testpress.util.SafeAsyncTask
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.container_layout.*
import javax.inject.Inject

class DoubtsActivity: TestpressFragmentActivity() {
    @Inject
    lateinit var serviceProvider: TestpressServiceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.container_layout)
        fetchSsoLink()
    }

    fun fetchSsoLink() {
        object : SafeAsyncTask<SsoUrl?>() {
            @Throws(Exception::class)
            override fun call(): SsoUrl {
                return serviceProvider.getService(this@DoubtsActivity).getSsoUrl()
            }

            override fun onSuccess(ssoLink: SsoUrl?) {
                super.onSuccess(ssoLink)
                val intent = Intent(this@DoubtsActivity, WebViewActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;
                intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Doubts")
                intent.putExtra(WebViewActivity.ENABLE_BACK, true)
                intent.putExtra(
                    WebViewActivity.URL_TO_OPEN,
                    BuildConfig.BASE_URL + ssoLink?.ssoUrl + "&next=/tickets/mobile/"
                )
                startActivity(intent)
                finish()
            }
        }.execute()
    }
}
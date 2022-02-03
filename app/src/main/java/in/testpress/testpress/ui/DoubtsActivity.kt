package `in`.testpress.testpress.ui

import `in`.testpress.testpress.BuildConfig
import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.models.SsoUrl
import `in`.testpress.testpress.util.SafeAsyncTask
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.kevinsawicki.wishlist.Toaster
import java.lang.Exception
import java.lang.RuntimeException
import java.net.UnknownHostException
import javax.inject.Inject

class DoubtsActivity: AppCompatActivity() {
    @Inject
    lateinit var serviceProvider: TestpressServiceProvider



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
        setContentView(R.layout.fragment_layout)
        fetchSsoLink()
    }

    fun fetchSsoLink() {
        Log.d("TAG", "fetchSsoLink: ")
        object : SafeAsyncTask<SsoUrl?>() {
            @Throws(Exception::class)
            override fun call(): SsoUrl {
                return serviceProvider.getService(this@DoubtsActivity).getSsoUrl()
            }

            @Throws(RuntimeException::class)
            protected override fun onException(exception: Exception) {
                super.onException(exception)
                Log.d("TAG", "onException: ")
            }

            @Throws(Exception::class)
            protected fun onSuccess(ssoLink: SsoUrl) {
                Log.d("TAG", "onSuccess: ${ssoLink.getSsoUrl()}")

//                runOnUiThread {
                    val intent = Intent(this@DoubtsActivity, WebViewActivity::class.java)
                    intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Doubts")
                    intent.putExtra(
                        WebViewActivity.URL_TO_OPEN,
                        BuildConfig.BASE_URL + ssoLink.getSsoUrl() + "&next=/tickets/mobile/"
                    )
                    Log.d("TAG", "onSuccess: 1")
                    startActivity(intent)
                    Log.d("TAG", "onSuccess: 2")
//                }
            }
        }.execute()
    }
}
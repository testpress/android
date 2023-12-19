package `in`.testpress.testpress.ui

import `in`.testpress.testpress.Injector
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.authenticator.LogoutService
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.os.Bundle
import android.webkit.JavascriptInterface
import javax.inject.Inject

class AccountDeleteActivity : AbstractWebViewActivity() {

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    @Inject
    lateinit var testpressService: TestpressService
    @Inject
    lateinit var logoutService: LogoutService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")
    }

    inner class JavaScriptInterface : BaseJavaScriptInterface(this) {

        @JavascriptInterface
        fun onAccountDeletionSuccess() {
            serviceProvider.logout(
                this@AccountDeleteActivity,
                testpressService,
                serviceProvider,
                logoutService
            )
        }

    }

}
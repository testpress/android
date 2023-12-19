package `in`.testpress.testpress.ui

import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.authenticator.LogoutService
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.util.Log
import android.webkit.JavascriptInterface
import javax.inject.Inject

class AccountDeleteActivity: AbstractWebViewActivity() {

    @Inject lateinit var serviceProvider: TestpressServiceProvider
    @Inject lateinit var testpressService: TestpressService
    @Inject lateinit var logoutService: LogoutService

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

    fun logout() {
        serviceProvider.logout(this, testpressService, serviceProvider, logoutService)
    }

}

class JavaScriptInterface(val activity: AccountDeleteActivity): BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onAccountDeletionSuccess() {
        Log.d("TAG", "onAccountDeletionSuccess: ")
        activity.logout()
    }

}
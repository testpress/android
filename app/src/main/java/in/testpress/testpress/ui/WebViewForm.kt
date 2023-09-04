package `in`.testpress.testpress.ui

import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface


class WebViewForm: AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this),"AndroidInterface")
    }

}

class JavaScriptInterface(val activity: WebViewForm): BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onSubmit() {
        activity.finish()
    }

}
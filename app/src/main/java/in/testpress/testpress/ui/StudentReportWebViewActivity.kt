package `in`.testpress.testpress.ui

import `in`.testpress.ui.AbstractWebViewActivity

class StudentReportWebViewActivity : AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
    }

    override fun shouldOverrideUrlLoading(url: String?): Boolean {
        // Return false so CustomWebViewClient loads institute URLs (like report detail pages)
        // inside the WebView without finishing/closing the activity.
        return false
    }
}

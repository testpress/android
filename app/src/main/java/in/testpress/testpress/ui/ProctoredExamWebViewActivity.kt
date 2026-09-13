package `in`.testpress.testpress.ui

import `in`.testpress.ui.AbstractWebViewActivity

class ProctoredExamWebViewActivity : AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
    }

    override fun shouldOverrideUrlLoading(url: String?): Boolean {
        // Return false so CustomWebViewClient loads institute URLs (like review results and comments)
        // inside the WebView without finishing/closing the activity.
        return false
    }
}

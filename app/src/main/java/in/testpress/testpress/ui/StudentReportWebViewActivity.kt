package `in`.testpress.testpress.ui

import android.content.Intent
import android.net.Uri
import `in`.testpress.testpress.BuildConfig
import `in`.testpress.ui.AbstractWebViewActivity

class StudentReportWebViewActivity : AbstractWebViewActivity() {

    override fun onWebViewInitializationSuccess() {
    }

    override fun shouldOverrideUrlLoading(url: String?): Boolean {
        if (url == null) return false

        // Load institute URLs in-app
        if (isInstituteUrl(url)) {
            return false
        }

        // Open external non-institute links in system browser
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isInstituteUrl(url: String): Boolean {
        return url.contains(BuildConfig.BASE_URL) || url.contains(BuildConfig.WHITE_LABELED_HOST_URL)
    }
}

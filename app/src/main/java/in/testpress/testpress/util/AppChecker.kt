package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import android.content.Context
import `in`.testpress.ui.WebViewWithSSOActivity

object AppChecker {

    fun isEduportApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "eduport"
    }

    fun isLmsDemoApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "lmsdemo"
    }

    fun isCatkingApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "catking"
    }
}
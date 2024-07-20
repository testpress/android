package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import android.content.Context

object AppChecker {

    fun isEduportApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "eduport"
    }

    fun isLmsDemoApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "lmsdemo"
    }

    fun isBrilliantPalaClassesApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "brilliantpalaelearn"
    }
}
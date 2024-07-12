package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import android.content.Context

object AppChecker {

    fun isEduportApp(context: Context): Boolean {
        return context.getString(R.string.testpress_site_subdomain) == "eduport"
    }

}
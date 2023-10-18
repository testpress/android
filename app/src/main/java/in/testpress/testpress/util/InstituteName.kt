package `in`.testpress.testpress.util

import android.content.Context

object InstituteName {

    fun isEPratibhaApp(context: Context) = context.packageName == "net.epratibha.www"

}
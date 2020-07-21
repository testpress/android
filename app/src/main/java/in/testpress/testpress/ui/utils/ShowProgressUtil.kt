package `in`.testpress.testpress.ui.utils

import `in`.testpress.testpress.R
import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

object ShowProgressUtil {

    lateinit var progressDialog: MaterialDialog

    fun showProgressDialog(context: Context) {
        progressDialog = MaterialDialog.Builder(context)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .cancelable(false)
                .show()
    }
}
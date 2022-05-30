package `in`.testpress.testpress.ui.view

import `in`.testpress.testpress.R
import android.app.Activity
import android.app.Dialog
import android.view.Window


class LoadingDialog(val activity: Activity) {
    private lateinit var dialog: Dialog

    fun showDialog() {
        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.custom_loading_layout)
        dialog.show()
    }

    fun hideDialog() {
        dialog.dismiss()
    }
}
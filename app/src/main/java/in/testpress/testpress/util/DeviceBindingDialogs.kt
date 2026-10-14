package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.InstituteSettings
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog

object DeviceBindingDialogs {

    private val defaultDialogBuilder: (Context, Int, Int, Runnable, Runnable?) -> AlertDialog = { context, titleRes, messageRes, onConfirm, onCancel ->
        val dialog = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onConfirm.run()
            }
            .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onCancel?.run()
            }
            .setOnCancelListener {
                onCancel?.run()
            }
            .create()
        dialog.show()
        dialog
    }

    private var customDialogBuilder: ((Context, Int, Int, Runnable, Runnable?) -> AlertDialog)? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setDialogBuilderForTesting(builder: ((Context, Int, Int, Runnable, Runnable?) -> AlertDialog)?) {
        customDialogBuilder = builder
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun resetDialogBuilderForTesting() {
        customDialogBuilder = null
    }

    private fun buildAndShowDialog(
        context: Context,
        titleRes: Int,
        messageRes: Int,
        onConfirm: Runnable,
        onCancel: Runnable?
    ): AlertDialog {
        val builder = customDialogBuilder ?: defaultDialogBuilder
        return builder(context, titleRes, messageRes, onConfirm, onCancel)
    }

    @JvmStatic
    @JvmOverloads
    fun confirmIfBindingEnabled(
        context: Context,
        instituteSettings: InstituteSettings?,
        onProceed: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog? {
        return if (instituteSettings?.isDeviceBindingEnabled == true) {
            showConfirmation(context, onProceed, onCancel)
        } else {
            onProceed.run()
            null
        }
    }

    @JvmStatic
    @JvmOverloads
    fun showConfirmation(
        context: Context,
        onConfirm: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog {
        return buildAndShowDialog(
            context,
            R.string.device_binding_confirm_title,
            R.string.device_binding_confirm_message,
            onConfirm,
            onCancel
        )
    }

    @JvmStatic
    @JvmOverloads
    fun handleLogout(
        context: Context,
        instituteSettings: InstituteSettings?,
        onProceedToLogout: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog {
        return if (instituteSettings?.isDeviceBindingEnabled == true) {
            showLogoutWarning(context, onConfirm = {
                showRegularLogoutConfirmation(context, onProceedToLogout, onCancel)
            }, onCancel = onCancel)
        } else {
            showRegularLogoutConfirmation(context, onProceedToLogout, onCancel)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun showLogoutWarning(
        context: Context,
        onConfirm: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog {
        return buildAndShowDialog(
            context,
            R.string.device_binding_logout_warning_title,
            R.string.device_binding_logout_warning_message,
            onConfirm,
            onCancel
        )
    }

    @JvmStatic
    @JvmOverloads
    fun showRegularLogoutConfirmation(
        context: Context,
        onConfirm: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog {
        return buildAndShowDialog(
            context,
            R.string.logout,
            R.string.logout_confirm_message,
            onConfirm,
            onCancel
        )
    }
}

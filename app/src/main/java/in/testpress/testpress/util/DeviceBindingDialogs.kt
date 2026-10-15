package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.InstituteSettings
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import java.util.function.Consumer

object DeviceBindingDialogs {

    private var customDialogBuilder: ((Context, Int, Int, Runnable, Runnable?) -> AlertDialog)? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setDialogBuilderForTesting(builder: ((Context, Int, Int, Runnable, Runnable?) -> AlertDialog)?) {
        customDialogBuilder = builder
    }

    private fun buildAndShowDialog(
        context: Context,
        titleRes: Int,
        messageRes: Int,
        onConfirm: Runnable,
        onCancel: Runnable?
    ): AlertDialog {
        val custom = customDialogBuilder
        if (custom != null) {
            return custom(context, titleRes, messageRes, onConfirm, onCancel)
        }
        val dialog = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.ok) { _, _ -> onConfirm.run() }
            .setNegativeButton(R.string.cancel) { _, _ -> onCancel?.run() }
            .setOnCancelListener { onCancel?.run() }
            .create()
        dialog.show()
        return dialog
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

    private fun showConfirmation(
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
        onCancel: Runnable? = null,
        onDialogShown: Consumer<AlertDialog>? = null
    ): AlertDialog {
        return if (instituteSettings?.isDeviceBindingEnabled == true) {
            val warningDialog = showLogoutWarning(context, onConfirm = {
                val confirmDialog = showRegularLogoutConfirmation(context, onProceedToLogout, onCancel)
                onDialogShown?.accept(confirmDialog)
            }, onCancel = onCancel)
            onDialogShown?.accept(warningDialog)
            warningDialog
        } else {
            val confirmDialog = showRegularLogoutConfirmation(context, onProceedToLogout, onCancel)
            onDialogShown?.accept(confirmDialog)
            confirmDialog
        }
    }

    @JvmStatic
    fun handleLogout(
        context: Context,
        instituteSettings: InstituteSettings?,
        onProceedToLogout: Runnable,
        onDialogShown: Consumer<AlertDialog>
    ): AlertDialog {
        return handleLogout(context, instituteSettings, onProceedToLogout, null, onDialogShown)
    }

    private fun showLogoutWarning(
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

    private fun showRegularLogoutConfirmation(
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

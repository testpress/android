package `in`.testpress.testpress.util

import `in`.testpress.testpress.R
import `in`.testpress.testpress.models.InstituteSettings
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog

object DeviceBindingDialogs {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var dialogBuilder: (Context, Int, Int, Runnable, Runnable?) -> AlertDialog = { context, titleRes, messageRes, onConfirm, onCancel ->
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
            .create()
        dialog.show()
        dialog
    }

    @JvmStatic
    @JvmOverloads
    fun confirmIfBindingEnabled(
        context: Context,
        instituteSettings: InstituteSettings?,
        onProceed: Runnable,
        onCancel: Runnable? = null
    ): AlertDialog? {
        val settings = instituteSettings ?: InstituteSettings.getInstance()
        return if (settings?.isDeviceBindingEnabled == true) {
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
        return dialogBuilder(
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
        val settings = instituteSettings ?: InstituteSettings.getInstance()
        return if (settings?.isDeviceBindingEnabled == true) {
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
        return dialogBuilder(
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
        return dialogBuilder(
            context,
            R.string.logout,
            R.string.logout_confirm_message,
            onConfirm,
            onCancel
        )
    }
}

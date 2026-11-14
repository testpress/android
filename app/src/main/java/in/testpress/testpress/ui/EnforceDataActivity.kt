package `in`.testpress.testpress.ui

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.core.TestpressSdk
import `in`.testpress.testpress.repository.ForceDataRepository
import `in`.testpress.testpress.viewmodel.ForceDataViewModel
import android.widget.Toast
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.authenticator.LogoutService
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.testpress.models.InstituteSettings
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import `in`.testpress.testpress.util.DeviceBindingDialogs
import javax.inject.Inject

class EnforceDataActivity : AbstractWebViewActivity() {

    private lateinit var completionViewModel: ForceDataViewModel
    private var isLoggingOut = false

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    @Inject
    lateinit var testpressService: TestpressService
    @Inject
    lateinit var logoutService: LogoutService
    @Inject
    lateinit var forceDataRepository: ForceDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
        completionViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ForceDataViewModel(forceDataRepository) as T
            }
        })[ForceDataViewModel::class.java]
        completionViewModel.state.observe(this) { state ->
            if (isLoggingOut) return@observe
            title = getString(if (state == ForceDataViewModel.State.CHECKING)
                R.string.please_wait else R.string.mandatory_update_title)
            when (state) {
                ForceDataViewModel.State.COMPLETE -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                ForceDataViewModel.State.INCOMPLETE -> {
                    Toast.makeText(this, R.string.mandatory_update_pending, Toast.LENGTH_LONG).show()
                    completionViewModel.consumeIncompleteMessage()
                }
                else -> Unit
            }
        }
        if(!isPermissionGranted(Manifest.permission.CAMERA)){
            askCameraPermission()
        }
    }

    private fun isPermissionGranted(permissionName: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permissionName
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.logout) {
            logout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private var logoutDialog: AlertDialog? = null

    private fun verifyCompletion() {
        if (isFinishing || isDestroyed || isLoggingOut) return
        val token = TestpressSdk.getTestpressSession(applicationContext)?.token
        if (token.isNullOrBlank()) {
            Toast.makeText(this, R.string.mandatory_update_session_missing, Toast.LENGTH_LONG).show()
            return
        }
        completionViewModel.verifyCompletion(token)
    }

    fun logout() {
        if (logoutDialog?.isShowing == true) return
        DeviceBindingDialogs.handleLogout(
            this,
            InstituteSettings.getInstance(),
            onProceedToLogout = { performLogout() },
            onDialogShown = { logoutDialog = it }
        )
    }

    private fun performLogout() {
        if (isFinishing || isDestroyed) return
        isLoggingOut = true
        completionViewModel.cancelVerification()
        serviceProvider.logout(
            this@EnforceDataActivity,
            testpressService,
            serviceProvider,
            logoutService
        )
    }

    override fun onDestroy() {
        logoutDialog?.dismiss()
        logoutDialog = null
        super.onDestroy()
    }

    inner class JavaScriptInterface : BaseJavaScriptInterface(this) {

        @JavascriptInterface
        fun onSubmit() {
            runOnUiThread {
                verifyCompletion()
            }
        }

    }

}

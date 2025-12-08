package `in`.testpress.testpress.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.authenticator.LogoutService
import `in`.testpress.testpress.core.TestpressService
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import javax.inject.Inject

class EnforceDataActivity : AbstractWebViewActivity() {

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    @Inject
    lateinit var testpressService: TestpressService
    @Inject
    lateinit var logoutService: LogoutService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestpressApplication.getAppComponent().inject(this)
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

    fun logout() {
        AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm_message)
            .setPositiveButton(
                R.string.ok
            ) { dialogInterface, i ->
                dialogInterface.dismiss()
                serviceProvider.logout(
                    this@EnforceDataActivity,
                    testpressService,
                    serviceProvider,
                    logoutService
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    inner class JavaScriptInterface : BaseJavaScriptInterface(this) {

        @JavascriptInterface
        fun onSubmit() {
            finish()
        }

    }

}
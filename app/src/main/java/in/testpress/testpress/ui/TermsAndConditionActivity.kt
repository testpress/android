package `in`.testpress.testpress.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.models.ProfileDetails
import `in`.testpress.testpress.TestpressApplication
import `in`.testpress.testpress.R
import `in`.testpress.testpress.TestpressServiceProvider
import `in`.testpress.testpress.authenticator.LogoutService
import `in`.testpress.testpress.core.TestpressService
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.io.IOException
import javax.inject.Inject

const val TERMS_AND_CONDITIONS = "terms&condition"


class TermsAndConditionActivity : BaseToolBarActivity() {

    @Inject
    lateinit var serviceProvider: TestpressServiceProvider
    @Inject
    lateinit var testpressService: TestpressService
    @Inject
    lateinit var logoutService: LogoutService

    lateinit var emptyView: LinearLayout
    lateinit var emptyTitleView: TextView
    lateinit var emptyDescView: TextView
    lateinit var retryButton: Button
    lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    lateinit var profileDetails: ProfileDetails

    var activity = this

    companion object {
        fun createIntent(activity: Activity): Intent {
            return Intent(activity, TermsAndConditionActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TestpressApplication.getAppComponent().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_and_condition_activity)
        bindViews()
        supportActionBar?.title = getString(R.string.terms_and_conditions)
    }

    private fun bindViews() {
        emptyView = findViewById(R.id.empty_container)
        emptyTitleView = findViewById(R.id.empty_title)
        emptyDescView = findViewById(R.id.empty_description)
        retryButton = findViewById(R.id.retry_button)
        progressBar = findViewById(R.id.terms_and_condition_pb_loading)
        webView = findViewById(R.id.terms_and_condition_web_view)
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        webView.visibility =View.GONE
        fetchProfileDetails()
    }

    private fun fetchProfileDetails() {
        TestpressUserDetails.getInstance().load(this, object : TestpressCallback<ProfileDetails>() {
            override fun onSuccess(result: ProfileDetails) {
                this@TermsAndConditionActivity.profileDetails = result
                displayWebView()
            }

            override fun onException(exception: TestpressException) {
                if (exception.cause is IOException){
                    setEmptyText(
                        R.string.network_error, R.string.no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp
                    )
                }
                webView.visibility = View.GONE
                progressBar.visibility = View.GONE
                retryButton.setOnClickListener {
                    emptyView.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    fetchProfileDetails()
                }

            }
        })
    }

    private fun displayWebView() {
        webView.visibility = View.GONE
        val url = "<iframe data-tally-src=" +
                "\"https://tally.so/embed/n9qLPp?name=${profileDetails.username}&email=${profileDetails.email}&alignLeft=1&hideTitle=1&transparentBackground=1&dynamicHeight=1\"" +
                " width=\"100%\" height=\"100%\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\" " +
                "title=\"Veranda Race - SH\"></iframe><script>var d=document,w=\"https://tally.so/widgets/embed.js\"," +
                "v=function(){\"undefined\"!=typeof Tally?Tally.loadEmbeds():d.querySelectorAll(\"iframe[data-tally-src]:not([src])\")." +
                "forEach((function(e){e.src=e.dataset.tallySrc}))};if(d.querySelector('script[src=\"'+w+'\"]'))v();else{var s=d.createElement(\"script\");" +
                "s.src=w,s.onload=v,s.onerror=v,d.body.appendChild(s);" +
                "window.addEventListener('message', (e) => {" +
                "    if (e && e.data && e.data.includes('Tally.FormSubmitted')) {" +
                "        const data = JSON.parse(e.data);" +
                "        Android.onUrlChange('Callback');    }" +
                "});" +
                "}</script>"

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(MyJavaScriptInterface(this), "Android")
        webView.loadData(url, "text/html", null)
        webView.webViewClient =object :WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()){
            progressBar.visibility = View.VISIBLE
            webView.visibility = View.GONE
            webView.goBack()
        } else {
            finishAffinity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout_terms_and_conditions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.logout_terms_and_conditions_button) {
            logout()
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
                    this@TermsAndConditionActivity, testpressService,
                    serviceProvider, logoutService
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun setEmptyText(title: Int, description: Int, left: Int) {
        emptyView.visibility = View.VISIBLE
        emptyTitleView.setText(title)
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0)
        emptyDescView.setText(description)
    }

}

internal class MyJavaScriptInterface(val activity: Activity) {
    @JavascriptInterface
    fun onUrlChange(url: String) {
        saveData()
        Toast.makeText(activity,"Terms and conditions accepted",Toast.LENGTH_SHORT).show()
        activity.finish()
    }

    private fun saveData(){
        val editor: SharedPreferences.Editor = activity.getSharedPreferences(TERMS_AND_CONDITIONS, Context.MODE_PRIVATE).edit()
        editor.putBoolean(TERMS_AND_CONDITIONS, true)
        editor.apply()
    }
}
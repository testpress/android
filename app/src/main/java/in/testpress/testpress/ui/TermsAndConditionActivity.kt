package `in`.testpress.testpress.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressUserDetails
import `in`.testpress.models.ProfileDetails
import `in`.testpress.testpress.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

const val TERMS_AND_CONDITIONS = "terms&condition"


class TermsAndConditionActivity : BaseToolBarActivity() {

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_and_condition_activity)
        supportActionBar?.title = "Terms And Conditions"
        progressBar = findViewById(R.id.terms_and_condition_pb_loading)

        fetchProfileDetails()
    }

    override fun onResume() {
        super.onResume()
        fetchProfileDetails()
    }


    private fun fetchProfileDetails() {
        TestpressUserDetails.getInstance().load(this, object : TestpressCallback<ProfileDetails>() {
            override fun onSuccess(result: ProfileDetails) {
                progressBar.visibility = View.GONE
                this@TermsAndConditionActivity.profileDetails = result
                displayWebView()
            }

            override fun onException(exception: TestpressException?) {
            }
        })
    }

    private fun displayWebView() {
        webView = findViewById(R.id.terms_and_condition_web_view)
        val url = "<iframe data-tally-src=" +
                "\"https://tally.so/embed/n9qLPp?alignLeft=1&hideTitle=1&transparentBackground=1&dynamicHeight=1&name${profileDetails.username}&ref=${profileDetails.email}\"" +
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
        webView.webViewClient = WebViewClient()
        webView.loadData(url, "text/html", null)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

}

internal class MyJavaScriptInterface(val activity: Activity) {
    @JavascriptInterface
    fun onUrlChange(url: String) {
        saveData()
        activity.finish()
    }

    private fun saveData(){
        val editor: SharedPreferences.Editor = activity.getSharedPreferences(TERMS_AND_CONDITIONS, Context.MODE_PRIVATE).edit()
        editor.putBoolean(TERMS_AND_CONDITIONS, true)
        editor.apply()
    }
}
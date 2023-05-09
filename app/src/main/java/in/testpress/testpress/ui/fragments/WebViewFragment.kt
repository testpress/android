package `in`.testpress.testpress.ui.fragments

import `in`.testpress.testpress.databinding.WebviewFragmentBinding
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment

class WebViewFragment : Fragment() {

    companion object {
        const val URL_TO_OPEN = "URL"
    }

    private var _binding: WebviewFragmentBinding? = null
    private val binding: WebviewFragmentBinding get() = _binding!!
    private lateinit var webView: WebView
    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WebviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = binding.webView
        setupWebView()
        loadWebView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun parseArguments() {
        url = arguments!!.getString(URL_TO_OPEN) ?: ""
    }

    private fun setupWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = getWebViewClient()
    }

    private fun loadWebView() {
        webView.loadUrl(url)
    }

    private fun getWebViewClient(): WebViewClient {

        return object : WebViewClient() {
            @Deprecated("Deprecated in Java", ReplaceWith("false"))
            override fun shouldOverrideUrlLoading(
                view: WebView,
                url: String
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.pbLoading.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(webView, url)
                webView.visibility = View.VISIBLE
                binding.pbLoading.visibility = View.GONE
            }
        }
    }

    fun canGoBack():Boolean{
        return webView.canGoBack()
    }

    fun goBack(){
        webView.goBack()
    }
}
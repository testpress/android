package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.R;

public class InAppBrowserActivity extends TestpressFragmentActivity {

    public static final String URL = "url";
    public static final String TITLE = "title";

    @InjectView(R.id.web_view) WebView webView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeRefresh;
    @InjectView(R.id.empty_container) LinearLayout emptyContainer;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;

    private boolean hasError = false;
    private String url;

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_browser);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final String title = getIntent().getStringExtra(TITLE);
        if (title != null) {
            getSupportActionBar().setTitle(title);
        }
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUrl();
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                swipeRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(true);
                    }
                });
                emptyContainer.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
                super.onPageFinished(view, url);
                InAppBrowserActivity.this.url = url;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().flush();
                } else {
                    //noinspection deprecation
                    CookieSyncManager.getInstance().sync();
                }
                if(hasError) {
                    swipeRefresh.setVisibility(View.GONE);
                    emptyContainer.setVisibility(View.VISIBLE);
                } else {
                    swipeRefresh.setVisibility(View.VISIBLE);
                    emptyContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {

                super.onReceivedError(view, request, error);
                setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);

                hasError = true;
                swipeRefresh.setRefreshing(false);
            }

        });
        loadUrl();
    }

    @OnClick(R.id.retry_button) protected void loadUrl() {
        hasError = false;
        if (url == null) {
            url = getIntent().getStringExtra(URL);
        }
        if (url == null) {
            throw new IllegalStateException("Url must not be null");
        }
        Pattern pattern = Pattern.compile("^(https?:\\/\\/)?www\\.([\\da-z\\.-]+)\\.([a-z\\.]{2,6})\\/[\\w \\.-]+?\\.pdf$");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
        } else {
            webView.loadUrl(url);
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyContainer.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
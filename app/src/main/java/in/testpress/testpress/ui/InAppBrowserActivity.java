package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
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
import in.testpress.util.PermissionsUtils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static in.testpress.testpress.core.Constants.RequestCode.RECEIVE_SMS_PERMISSION_REQUEST_CODE;

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
    private String downloadUrl;
    private PermissionsUtils permissionsUtils;
    private String cookies = "";

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
        webSettings.setBuiltInZoomControls(true);
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
                cookies = CookieManager.getInstance().getCookie(url);
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

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.endsWith(".pdf")){
                    // Load pdf in google docs
                    String googleDocs = "https://docs.google.com/gview?embedded=true&url=";
                    view.loadUrl(googleDocs + url);
                } else {
                    // Load all other urls normally.
                    view.loadUrl(url);
                }
                return true;
            }
        });
        String[] permissions = new String[] { WRITE_EXTERNAL_STORAGE };
        final PermissionsUtils.PermissionRequestResultHandler permissionRequestResultHandler =
                new PermissionsUtils.PermissionRequestResultHandler() {
                    @Override
                    public void onPermissionGranted() {
                        downloadFile();
                    }
                };
        permissionsUtils = new PermissionsUtils(this, swipeRefresh, permissions);

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                downloadUrl = url;
                permissionsUtils.requestPermissions(permissionRequestResultHandler);
            }
        });
        loadUrl();
    }

    void downloadFile() {
        Uri uri = Uri.parse(downloadUrl);
        String fileName;
        if (uri.getQueryParameterNames().contains("pdfname")) {
            fileName = uri.getQueryParameter("pdfname");
            fileName += ".pdf";
        } else {
            fileName = uri.getLastPathSegment();
        }
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.allowScanningByMediaScanner();
        request.addRequestHeader("Cookie", cookies);
        request.setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setTitle(fileName);
        request.setDescription("Downloading...");
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //noinspection ConstantConditions
        dm.enqueue(request);
        Snackbar.make(webView, "Started Downloading File", Snackbar.LENGTH_SHORT).show();
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
        permissionsUtils.onResume();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsUtils.onRequestPermissionsResult(requestCode, grantResults);
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
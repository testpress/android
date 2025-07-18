package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.databinding.ActivityRssFeedDetailBinding;
import in.testpress.testpress.models.RssItem;
import in.testpress.testpress.models.RssItemDao;
import in.testpress.testpress.util.ShareUtil;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

public class RssFeedDetailActivity extends BaseToolBarActivity {

    public static final String LINK_URL = "linkUrl";

    RssItemDao rssItemDao;
    RssItem rssItem;
    String url;

    @Inject protected TestpressService testpressService;

    private WebView content;
    private TextView title;
    private TextView date;
    private Button viewInWebsiteButton;
    private LinearLayout postDetails;
    private RelativeLayout progressBar;
    private ActivityRssFeedDetailBinding binding;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRssFeedDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TestpressApplication.getAppComponent().inject(this);
        bindViews();
        postDetails.setVisibility(View.GONE);
        rssItemDao = TestpressApplication.getDaoSession().getRssItemDao();
        date.setTypeface(TestpressSdk.getRubikRegularFont(this));
        ViewUtils.setTypeface(new TextView[] { title, viewInWebsiteButton },
                TestpressSdk.getRubikMediumFont(this));

        url = getIntent().getStringExtra(LINK_URL);
        rssItem = rssItemDao.queryBuilder()
                .where(RssItemDao.Properties.Link.eq(url)).list().get(0);

        displayPost();
    }

    private void bindViews() {
        content = binding.content;
        title = binding.title;
        date = binding.date;
        viewInWebsiteButton = binding.viewInWebsite;
        postDetails = binding.postDetails;
        progressBar = binding.pbLoading;

        viewInWebsiteButton.setOnClickListener(v -> viewInWebsite());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void displayPost() {
        postDetails.setVisibility(View.VISIBLE);
        title.setText(rssItem.getTitle());
        date.setText(DateUtils.getRelativeTimeSpanString(rssItem.getPublishDate()));
        WebSettings settings = content.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        content.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                UIUtils.openInBrowser(getApplicationContext(), url);
                return true;
            }
        });
        content.loadDataWithBaseURL("file:///android_asset/", getHeader() + rssItem.getDescription(),
                "text/html", "UTF-8", null);
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    }

    private void viewInWebsite() {
        UIUtils.openInBrowser(this, rssItem.getLink());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == R.id.share) {
            ShareUtil.shareUrl(this, rssItem.getTitle(), rssItem.getLink());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

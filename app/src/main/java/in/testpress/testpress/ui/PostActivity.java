package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.ShareUtil;

import info.hoang8f.widget.FButton;

public class PostActivity extends DeepLinkHandlerActivity {

    String shortWebUrl;
    PostDao postDao;
    Post post;
    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.content) WebView content;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.summary) TextView summary;
    @InjectView(R.id.date) TextView date;
    @InjectView(R.id.content_empty_view) TextView contentEmptyView;
    @InjectView(R.id.postDetails) RelativeLayout postDetails;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) FButton retryButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_details_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postDetails.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        postDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getPostDao();
        shortWebUrl = getIntent().getStringExtra("shortWebUrl");
        if(shortWebUrl != null) {
            List<Post> posts = postDao.queryBuilder().where(PostDao.Properties.Short_web_url.eq(shortWebUrl)).list();
            if (!posts.isEmpty()) {
                post = posts.get(0);
                if (post.getContentHtml() != null) {
                    displayPost(post);
                    return;
                }
            }
            // If there is no post in this url in db or
            // If it content_html is null then fetch the post
            fetchPost();
        } else {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    private void fetchPost() {
        new SafeAsyncTask<Post>() {
            @Override
            public Post call() throws Exception {
                AccountManager manager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
                final Account[] account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                if (account.length > 0) {
                    testpressService = serviceProvider.getService(PostActivity.this);
                }
                Map<String, Boolean> queryParams = new LinkedHashMap<>();
                queryParams.put("short_link", true);
                return testpressService.getPostDetail(shortWebUrl.replace(Constants.Http.URL_BASE +"/p/", ""), queryParams);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                progressBar.setVisibility(View.GONE);
                if (e.getCause() instanceof UnknownHostException) {
                    setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
                } else if (e.getMessage().equals("404 NOT FOUND")) {
                    setEmptyText(R.string.access_denied, R.string.post_authentication_failed, R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.error_loading_content, R.drawable.ic_error_outline_black_18dp);
                }
            }

            @Override
            protected void onSuccess(final Post post) throws Exception {
                PostActivity.this.post = post;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                post.setPublished(simpleDateFormat.parse(post.getPublishedDate()).getTime());
                if (postDao.queryBuilder().where(PostDao.Properties.Id.eq(post.getId())).count() != 0) {
                    post.setModifiedDate(simpleDateFormat.parse(post.getModified()).getTime());
                    if (post.category != null) {
                        post.setCategory(post.category);
                        CategoryDao categoryDao = ((TestpressApplication) getApplicationContext())
                                .getDaoSession().getCategoryDao();
                        categoryDao.insertOrReplace(post.category);
                    }
                    postDao.insertOrReplace(post);
                }
                displayPost(post);
            }
        }.execute();
    }

    private void displayPost(Post post) {
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        getSupportActionBar().setTitle(post.getTitle());
        title.setText(post.getTitle());
        summary.setText(post.getSummary());
        FormatDate formatter = new FormatDate();
        date.setText(DateUtils.getRelativeTimeSpanString(post.getPublished()));
        if (post.getContentHtml() != null) {
            WebSettings settings = content.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            settings.setJavaScriptEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setSupportZoom(true);
            content.loadData(post.getContentHtml(), "text/html; charset=utf-8", null);
        } else {
            content.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == R.id.share) {
            if (post != null) {
                ShareUtil.shareUrl(this, post.getTitle(), post.getShort_web_url());
            } else {
                ShareUtil.shareUrl(this, "Check out this article", shortWebUrl);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        if (post != null) {
            contentEmptyView.setText(description);
            contentEmptyView.setVisibility(View.VISIBLE);
            displayPost(post);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyTitleView.setText(title);
            emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
            emptyDescView.setText(description);
            retryButton.setVisibility(View.GONE);
        }
    }

}

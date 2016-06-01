package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.SafeAsyncTask;

public class PostActivity extends TestpressFragmentActivity {

    String url;
    String urlWithBase;
    PostDao postDao;
    Post post;
    @Inject protected TestpressService testpressService;
    @InjectView(R.id.content) WebView content;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.summary) TextView summary;
    @InjectView(R.id.date) TextView date;
    @InjectView(R.id.empty_view) TextView emptyView;
    @InjectView(R.id.postDetails) RelativeLayout postDetails;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;

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
        urlWithBase = getIntent().getStringExtra("urlWithBase");
        if(urlWithBase != null) {
            post = postDao.queryBuilder().where(PostDao.Properties.Url.eq(urlWithBase)).list().get(0);
            if(post.getContentHtml() == null) {
                url = urlWithBase.replace(Constants.Http.URL_BASE + "/", "");
                fetchPost();
            } else {
                displayPost(post);
            }
        } else {
            url = getIntent().getStringExtra("url");
            fetchPost();
        }
    }

    private void fetchPost() {
        new SafeAsyncTask<Post>() {
            @Override
            public Post call() throws Exception {
                return testpressService.getPostDetail(url);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                e.printStackTrace();
                if (e.getCause() instanceof UnknownHostException) {
                    emptyView.setText(getResources().getString(R.string.no_internet));
                } else {
                    emptyView.setText(getResources().getString(R.string.error_loading_content));
                }
                content.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                displayPost(post);
            }

            @Override
            protected void onSuccess(final Post post) throws Exception {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(urlWithBase != null) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(this, PostsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("parentIsNotification", true);
            startActivity(intent);
            finish();
        }
    }
}

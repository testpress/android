package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.Session;
import in.testpress.testpress.models.SessionDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.ListTagHandler;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;

public class PostActivity extends TestpressFragmentActivity {

    String url;
    String urlWithBase;
    PostDao postDao;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.content) TextView content;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.date) TextView date;
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
            InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
            if(internetConnectivityChecker.isConnected()) {
                url = urlWithBase.replace(Constants.Http.URL_BASE + "/", "");
                fetchPost();
            } else {
                Ln.e(postDao.queryBuilder().where(PostDao.Properties.Url.eq(urlWithBase)).list().size() +":ssssssssssssssssssssssss");
                displayPost(postDao.queryBuilder().where(PostDao.Properties.Url.eq(urlWithBase)).list().get(0));
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
                return serviceProvider.getService(PostActivity.this).getPostDetail(url);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
            }

            @Override
            protected void onSuccess(final Post post) throws Exception {
                displayPost(post);
                postDao.insertOrReplace(post);
            }
        }.execute();
    }

    private void displayPost(Post post) {
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        getSupportActionBar().setTitle(post.getTitle());
        title.setText(post.getTitle());
        FormatDate formatter = new FormatDate();
        date.setText(formatter.formatDateTime(post.getModified()));
        if(post.getContentHtml() != null) {
            Spanned htmlSpan = Html.fromHtml(post.getContentHtml(), new UILImageGetter(content, PostActivity.this), new ListTagHandler());
            ZoomableImageString zoomableImageQuestion = new ZoomableImageString(PostActivity.this);
            content.setText(zoomableImageQuestion.convertString(htmlSpan));
            content.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            content.setText("Please check your internet connection to load the content");
            content.setTextColor(getResources().getColor(R.color.red));
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

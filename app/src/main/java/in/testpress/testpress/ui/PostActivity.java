package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.ListTagHandler;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;

public class PostActivity extends TestpressFragmentActivity {

    String url;
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
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        url = data.getString("url");
        fetchPost();
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
                postDetails.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                getSupportActionBar().setTitle(post.getTitle());
                title.setText(post.getTitle());
                FormatDate formatter = new FormatDate();
                date.setText(formatter.formatDateTime(post.getModified()));
                Spanned htmlSpan = Html.fromHtml(post.getContentHtml(), new UILImageGetter(content, PostActivity.this), new ListTagHandler());
                ZoomableImageString zoomableImageQuestion = new ZoomableImageString(PostActivity.this);
                content.setText(zoomableImageQuestion.convertString(htmlSpan));
                content.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }.execute();
    }
}

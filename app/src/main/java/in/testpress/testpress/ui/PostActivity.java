package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;

public class PostActivity extends Activity {

    String url;
    WebView webview;
    @Inject protected TestpressServiceProvider serviceProvider;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        ButterKnife.inject(this);
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        url = data.getString("url");
        webview = new WebView(this);
        setContentView(webview);
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
                webview.loadData(post.getContent_html(), "text/html", null);
            }
        }.execute();

    }
}

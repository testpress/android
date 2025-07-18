package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.util.ImageUtils;
import in.testpress.models.FileDetails;
import in.testpress.network.TestpressApiClient;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.CommentsPager;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.Comment;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.ShareUtil;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.EventsTrackerFacade;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.StringUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;

import static in.testpress.testpress.util.CommonUtils.getException;

public class PostActivity extends TestpressFragmentActivity implements
        LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String SHORT_WEB_URL = "shortWebUrl";
    public static final String DETAIL_URL = "detail_url";
    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;

    String shortWebUrl;
    String detailUrl;
    PostDao postDao;
    Post post;
    CommentsPager previousCommentsPager;
    CommentsPager newCommentsPager;
    CommentsListAdapter commentsAdapter;
    ProgressDialog progressDialog;
    SimpleDateFormat simpleDateFormat;
    boolean postedNewComment;
    ImageUtils imagePickerUtils;
    private FullScreenChromeClient fullScreenChromeClient;

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    WebView content;
    TextView title;
    TextView summary;
    LinearLayout summaryLayout;
    TextView date;
    TextView contentEmptyView;
    RelativeLayout postDetails;
    ProgressBar progressBar;
    LinearLayout emptyView;
    TextView emptyTitleView;
    TextView emptyDescView;
    Button retryButton;
    LinearLayout commentsLayout;
    LinearLayout previousCommentsLoadingLayout;
    LinearLayout newCommentsLoadingLayout;
    RecyclerView listView;
    LinearLayout loadPreviousCommentsLayout;
    TextView loadPreviousCommentsText;
    LinearLayout loadNewCommentsLayout;
    TextView loadNewCommentsText;
    TextView commentsLabel;
    TextView commentsEmptyView;
    EditText commentsEditText;
    LinearLayout commentBoxLayout;
    NestedScrollView scrollView;
    View activityRootLayout;
    LinearLayout newCommentsAvailableLabel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_details_layout);
        TestpressApplication.getAppComponent().inject(this);
        bindViews();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postDetails.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        postDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getPostDao();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        shortWebUrl = getIntent().getStringExtra(SHORT_WEB_URL);
        detailUrl = getIntent().getStringExtra(DETAIL_URL);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        fullScreenChromeClient = new FullScreenChromeClient(this);
        in.testpress.util.UIUtils.setIndeterminateDrawable(this, progressDialog, 4);
        ViewUtils.setTypeface(new TextView[] {loadPreviousCommentsText, commentsLabel,
                loadNewCommentsText, title}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {date, summary, commentsEmptyView, commentsEditText},
                TestpressSdk.getRubikRegularFont(this));

        if(shortWebUrl == null && detailUrl == null) {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
        } else {
            List<Post> posts = getPostFromDB();

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
        }
    }

    private void bindViews() {
        content = findViewById(R.id.content);
        title = findViewById(R.id.title);
        summary = findViewById(R.id.summary);
        summaryLayout = findViewById(R.id.summary_layout);
        date = findViewById(R.id.date);
        contentEmptyView = findViewById(R.id.content_empty_view);
        postDetails = findViewById(R.id.postDetails);
        progressBar = findViewById(R.id.pb_loading);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        retryButton = findViewById(R.id.retry_button);
        commentsLayout = findViewById(R.id.comments_layout);
        previousCommentsLoadingLayout = findViewById(R.id.loading_previous_comments_layout);
        newCommentsLoadingLayout = findViewById(R.id.loading_new_comments_layout);
        listView = findViewById(R.id.comments_list_view);
        loadPreviousCommentsLayout = findViewById(R.id.load_previous_comments_layout);
        loadPreviousCommentsText = findViewById(R.id.load_previous_comments);
        loadNewCommentsLayout = findViewById(R.id.load_new_comments_layout);
        loadNewCommentsText = findViewById(R.id.load_new_comments_text);
        commentsLabel = findViewById(R.id.comments_label);
        commentsEmptyView = findViewById(R.id.comments_empty_view);
        commentsEditText = findViewById(R.id.comment_box);
        commentBoxLayout = findViewById(R.id.comment_box_layout);
        scrollView = findViewById(R.id.scroll_view);
        activityRootLayout = findViewById(android.R.id.content);
        newCommentsAvailableLabel = findViewById(R.id.new_comments_available_label);

        findViewById(R.id.send).setOnClickListener(v -> sendComment());
        findViewById(R.id.image_comment_button).setOnClickListener(v -> pickImage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    private List<Post> getPostFromDB() {
        List<Post> posts;
        if (shortWebUrl != null) {
            posts = postDao.queryBuilder().where(PostDao.Properties.Short_web_url.eq(shortWebUrl)).list();
        } else {
            List<String> pathSegments = Uri.parse(detailUrl).getPathSegments();
            String slug = pathSegments.get(1);
            posts = postDao.queryBuilder().where(PostDao.Properties.Slug.eq(slug)).list();
        }
        return posts;
    }

    private void fetchPost() {
        new SafeAsyncTask<Post>() {
            @Override
            public Post call() throws Exception {
                Map<String, Boolean> queryParams = new LinkedHashMap<>();
                String url = StringUtils.isNullOrEmpty(detailUrl) ? shortWebUrl : detailUrl;
                if (shortWebUrl != null) {
                    queryParams.put("short_link", true);
                }
                Uri uri = Uri.parse(url);
                return getService().getPostDetail(uri.getLastPathSegment(), queryParams);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                progressBar.setVisibility(View.GONE);
                if (e.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again, R.drawable.ic_error_outline_black_18dp);
                } else if (e.getMessage().equals("404 NOT FOUND")) {
                    setEmptyText(R.string.access_denied, R.string.post_authentication_failed, R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.error_loading_content, R.drawable.ic_error_outline_black_18dp);
                }
            }

            @Override
            protected void onSuccess(final Post post) throws Exception {
                PostActivity.this.post = post;
                post.setPublished(simpleDateFormat.parse(post.getPublishedDate()).getTime());
                if (postDao.queryBuilder().where(PostDao.Properties.Id.eq(post.getId())).count() != 0) {
                    post.setModifiedDate(simpleDateFormat.parse(post.getModified()).getTime());
                    if (post.getRawCategory() != null) {
                        post.setCategory(post.getRawCategory());
                        CategoryDao categoryDao = ((TestpressApplication) getApplicationContext())
                                .getDaoSession().getCategoryDao();
                        categoryDao.insertOrReplace(post.getRawCategory());
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
        logEvent();
        getSupportActionBar().setTitle(R.string.app_name);
        title.setText(post.getTitle());
        if (post.getSummary().trim().isEmpty()) {
            summaryLayout.setVisibility(View.GONE);
        } else {
            summary.setText(post.getSummary());
            summaryLayout.setVisibility(View.VISIBLE);
        }
        date.setText(DateUtils.getRelativeTimeSpanString(post.getPublished()));
        if (post.getContentHtml() != null) {
            WebViewUtils webViewUtils = new WebViewUtils(content) {
                @Override
                protected void onPageStarted() {
                    super.onPageStarted();
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onLoadFinished() {
                    super.onLoadFinished();
                    progressBar.setVisibility(View.GONE);
                    displayComments();
                }

                @Override
                public String getJavascript(Context context) {
                    String iFrameVideoWrapper = in.testpress.util.CommonUtils
                            .getStringFromAsset(PostActivity.this, "IFrameVideoWrapper.js");

                    return super.getJavascript(context) + iFrameVideoWrapper;
                }

                @Override
                protected void onNetworkError() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                protected boolean shouldOverrideUrlLoading(Activity activity, String url) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(activity, R.color.primary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    try {
                        customTabsIntent.launchUrl(activity, Uri.parse(url));
                    } catch (ActivityNotFoundException e) {
                        boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
                        int message = wrongUrl ? R.string.wrong_url : R.string.browser_not_available;
                        UIUtils.getAlertDialog(PostActivity.this, R.string.not_supported, message)
                                .show();
                    }
                    return true;
                }
            };
            webViewUtils.initWebView(getHeader() + post.getContentHtml(), this);
            content.setWebChromeClient(fullScreenChromeClient);
        } else {
            content.setVisibility(View.GONE);
            commentsLayout.setVisibility(View.GONE);
        }
    }

    private void logEvent() {
        EventsTrackerFacade eventsTrackerFacade = new EventsTrackerFacade(getApplicationContext());
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", post.getId());
        params.put("title", post.getTitle());
        eventsTrackerFacade.logEvent(EventsTrackerFacade.VIEWED_POST_EVENT, params);
    }

    void displayComments() {
        commentsAdapter = new CommentsListAdapter(this);
        listView.setNestedScrollingEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(commentsAdapter);
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                getSupportLoaderManager()
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, PostActivity.this);
            }
        });
        loadNewCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewCommentsLayout.setVisibility(View.GONE);
                getSupportLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, PostActivity.this);
            }
        });
        newCommentsAvailableLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCommentsAvailableLabel.setVisibility(View.GONE);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY,
                                       int oldScrollX, int oldScrollY) {

                int scrollViewHeight = scrollView.getHeight();
                int totalScrollViewChildHeight = scrollView.getChildAt(0).getHeight();
                // Let's assume end has reached at 50 pixels before itself(on partial visible of last item)
                boolean endHasBeenReached =
                        (scrollY + scrollViewHeight + 50) >= totalScrollViewChildHeight;

                if (endHasBeenReached) {
                    newCommentsAvailableLabel.setVisibility(View.GONE);
                }
            }
        });
        imagePickerUtils = new ImageUtils(activityRootLayout, this);
        commentsLayout.setVisibility(View.VISIBLE);
        getSupportLoaderManager().initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, PostActivity.this);
    }

    @Override
    public Loader<List<Comment>> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                previousCommentsLoadingLayout.setVisibility(View.VISIBLE);
                return new ThrowableLoader<List<Comment>>(this, null) {
                    @Override
                    public List<Comment> loadData() throws IOException {
                        getPreviousCommentsPager().clearResources().next();
                        return getPreviousCommentsPager().getResources();
                    }
                };
            case NEW_COMMENTS_LOADER_ID:
                if (postedNewComment) {
                    newCommentsLoadingLayout.setVisibility(View.VISIBLE);
                }
                return new ThrowableLoader<List<Comment>>(this, null) {
                    @Override
                    public List<Comment> loadData() throws IOException {
                        do {
                            getNewCommentsPager().next();
                        } while (getNewCommentsPager().hasNext());
                        return getNewCommentsPager().getResources();
                    }
                };
            default:
                //An invalid id was passed
                return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    CommentsPager getPreviousCommentsPager() {
        if (previousCommentsPager == null) {
            previousCommentsPager = new CommentsPager(getService(), post.getId());
            previousCommentsPager.queryParams.put(Constants.Http.ORDER, "-created");
            previousCommentsPager.queryParams.put(Constants.Http.UNTIL,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").format(new Date()));
        }
        return previousCommentsPager;
    }

    CommentsPager getNewCommentsPager() {
        if (newCommentsPager == null) {
            newCommentsPager = new CommentsPager(getService(), post.getId());
        }
        List<Comment> comments = commentsAdapter.getComments();
        if (newCommentsPager.queryParams.isEmpty() && comments.size() != 0) {
            Comment latestComment = comments.get(comments.size() - 1);
            //noinspection ConstantConditions
            newCommentsPager.queryParams.put(Constants.Http.SINCE, latestComment.getCreated());
        }
        return newCommentsPager;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Comment>> loader, List<Comment> comments) {
        getSupportLoaderManager().destroyLoader(loader.getId());
        switch (loader.getId()) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                onPreviousCommentsLoadFinished(loader, comments);
                break;
            case NEW_COMMENTS_LOADER_ID:
                onNewCommentsLoadFinished(loader, comments);
                break;
        }
    }

    void onPreviousCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = getException(loader);
        if (previousCommentsPager == null || (exception == null && comments == null)) {
            return;
        }
        if (exception != null) {
            previousCommentsLoadingLayout.setVisibility(View.GONE);
            if (post.getCommentsCount() == 0) {
                commentBoxLayout.setVisibility(View.VISIBLE);
            } else if (exception.getCause() instanceof IOException) {
                loadPreviousCommentsText.setText(R.string.load_comments);
                loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
                Snackbar.make(activityRootLayout, R.string.no_internet_connection,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(activityRootLayout, R.string.network_error,
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        if (!comments.isEmpty()) {
            commentsAdapter.addPreviousComments(comments);
        } else {
            commentsEmptyView.setVisibility(View.VISIBLE);
        }
        if (post.getCommentsCount() < getPreviousCommentsPager().getCommentsCount()) {
            updateCommentsCount(getPreviousCommentsPager().getCommentsCount());
        }
        if (getPreviousCommentsPager().hasNext()) {
            loadPreviousCommentsText.setText(R.string.load_previous_comments);
            loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
        } else {
            loadPreviousCommentsLayout.setVisibility(View.GONE);
        }
        if (commentBoxLayout.getVisibility() == View.GONE) {
            commentBoxLayout.setVisibility(View.VISIBLE);
        }
        previousCommentsLoadingLayout.setVisibility(View.GONE);
    }

    void onNewCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = getException(loader);
        if (exception != null) {
            newCommentsLoadingLayout.setVisibility(View.GONE);
            if (postedNewComment) {
                if (exception.getCause() instanceof IOException) {
                    Snackbar.make(activityRootLayout, R.string.no_internet_connection,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(activityRootLayout, R.string.network_error,
                            Snackbar.LENGTH_SHORT).show();
                }
                loadNewCommentsLayout.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (!comments.isEmpty()) {
            commentsAdapter.addComments(comments);
            int noOfComments = post.getCommentsCount() + getNewCommentsPager().getCommentsCount();
            updateCommentsCount(noOfComments);
        }
        if (commentsAdapter.getItemCount() != 0 && commentsEmptyView.getVisibility() == View.VISIBLE) {
            commentsEmptyView.setVisibility(View.GONE);
        }
        newCommentsLoadingLayout.setVisibility(View.GONE);
        if (postedNewComment) {
            // if user posted a comment scroll to the bottom
            postedNewComment = false;
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        } else {
            int scrollY = scrollView.getScrollY();
            int scrollViewHeight = scrollView.getHeight();
            int totalScrollViewChildHeight = scrollView.getChildAt(0).getHeight();
            boolean endHasBeenReached = (scrollY + scrollViewHeight) >= totalScrollViewChildHeight;
            if (!comments.isEmpty() && !endHasBeenReached) {
                newCommentsAvailableLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    private void sendComment() {
        final String comment = commentsEditText.getText().toString().trim();
        if (comment.isEmpty()) {
            return;
        }
        if (!CommonUtils.isUserAuthenticated(this)) {
            showLoginScreen();
            return;
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        UIUtils.hideSoftKeyboard(this);
        //noinspection deprecation
        postComment(Html.toHtml(new SpannableString(comment))); // Convert to html to support line breaks
    }
    
    void showLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constants.DEEP_LINK_TO, Constants.DEEP_LINK_TO_POST);
        intent.putExtra(SHORT_WEB_URL, shortWebUrl);
        startActivity(intent);
    }
    
    void postComment(final String comment) {
        new SafeAsyncTask<Comment>() {
            public Comment call() throws Exception {
                return getService().sendComments(post.getId(), comment);
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                super.onException(exception);
                handleExceptionOnSendComment(exception);
            }

            @Override
            public void onSuccess(final Comment comments) {
                commentsEditText.setText("");
                listView.requestLayout();
                progressDialog.dismiss();
                Snackbar.make(activityRootLayout, R.string.comment_posted,
                        Snackbar.LENGTH_SHORT).show();

                postedNewComment = true;
                getNewCommentsPager().reset();
                getSupportLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, PostActivity.this);
            }
        }.execute();
    }

    private void pickImage() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtils.onActivityResult(requestCode, resultCode, data,
                new ImageUtils.ImagePickerResultHandler() {
                    @Override
                    public void onSuccessfullyImageCropped(CropImage.ActivityResult result) {
                        uploadImage(result.getUri().getPath());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        
        imagePickerUtils.permissionsUtils.onRequestPermissionsResult(requestCode, grantResults);
    }

    void uploadImage(String imagePath) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        if (!CommonUtils.isUserAuthenticated(this)) {
            showLoginScreen();
            return;
        }
        //noinspection ConstantConditions
        new TestpressApiClient(this, TestpressSdk.getTestpressSession(this))
                .upload(imagePath).enqueue(new TestpressCallback<FileDetails>() {
                    @Override
                    public void onSuccess(FileDetails fileDetails) {
                        postComment(WebViewUtils.appendImageTags(fileDetails.getUrl()));
                    }
        
                    @Override
                    public void onException(TestpressException exception) {
                        handleExceptionOnSendComment(exception);
                    }
        });
    }

    /**
     * Call this method only from async task
     *
     * @return TestpressService
     */
    TestpressService getService() {
        if (CommonUtils.isUserAuthenticated(this)) {
            try {
                testpressService = serviceProvider.getService(PostActivity.this);
            } catch (IOException | AccountsException e) {
                e.printStackTrace();
            }
        }
        return testpressService;
    }

    @SuppressLint("DefaultLocale")
    void updateCommentsCount(int count) {
        List<Post> posts = postDao.queryBuilder()
                .where(PostDao.Properties.Id.eq(post.getId())).list();

        if (!posts.isEmpty()) {
            Post post = posts.get(0);
            post.setCommentsCount(count);
            post.update();
        }
        post.setCommentsCount(count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imagePickerUtils != null) {
            imagePickerUtils.permissionsUtils.onResume();
        }
        content.onResume();
    }

    String getHeader() {
        return "<link rel='stylesheet' type='text/css' href='typebase.css' />";
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

    void handleExceptionOnSendComment(Exception exception) {
        progressDialog.dismiss();
        if (exception.getCause() instanceof IOException) {
            Snackbar.make(activityRootLayout, R.string.no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(activityRootLayout, R.string.network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        content.onPause();
    }

    @Override
    public void onLoaderReset(Loader<List<Comment>> loader) {
    }

}

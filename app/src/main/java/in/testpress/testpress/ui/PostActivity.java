package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.models.FileDetails;
import in.testpress.network.TestpressApiClient;
import in.testpress.testpress.Injector;
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
import in.testpress.testpress.util.ImagePickerUtil;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.ShareUtil;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import info.hoang8f.widget.FButton;

import static in.testpress.testpress.util.CommonUtils.getException;

public class PostActivity extends TestpressFragmentActivity implements
        LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String SHORT_WEB_URL = "url";
    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    public static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;

    String shortWebUrl;
    PostDao postDao;
    Post post;
    CommentsPager previousCommentsPager;
    CommentsPager newCommentsPager;
    CommentsListAdapter commentsAdapter;
    ProgressDialog progressDialog;
    SimpleDateFormat simpleDateFormat;
    boolean postedNewComment;
    ImagePickerUtil imagePickerUtil;
    Uri selectedCommentImageUri;

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.content) WebView content;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.summary) TextView summary;
    @InjectView(R.id.summary_layout) LinearLayout summaryLayout;
    @InjectView(R.id.date) TextView date;
    @InjectView(R.id.content_empty_view) TextView contentEmptyView;
    @InjectView(R.id.postDetails) RelativeLayout postDetails;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) FButton retryButton;
    @InjectView(R.id.comments_layout) LinearLayout commentsLayout;
    @InjectView(R.id.loading_previous_comments_layout) LinearLayout previousCommentsLoadingLayout;
    @InjectView(R.id.loading_new_comments_layout) LinearLayout newCommentsLoadingLayout;
    @InjectView(R.id.comments_list_view) RecyclerView listView;
    @InjectView(R.id.load_previous_comments_layout) LinearLayout loadPreviousCommentsLayout;
    @InjectView(R.id.load_previous_comments) TextView loadPreviousCommentsText;
    @InjectView(R.id.load_new_comments_layout) LinearLayout loadNewCommentsLayout;
    @InjectView(R.id.load_new_comments_text) TextView loadNewCommentsText;
    @InjectView(R.id.comments_label) TextView commentsLabel;
    @InjectView(R.id.comments_empty_view) TextView commentsEmptyView;
    @InjectView(R.id.comment_box) EditText commentsEditText;
    @InjectView(R.id.comment_box_layout) LinearLayout commentBoxLayout;
    @InjectView(R.id.scroll_view) NestedScrollView scrollView;
    @InjectView(android.R.id.content) View activityRootLayout;
    @InjectView(R.id.new_comments_available_label) LinearLayout newCommentsAvailableLabel;

    private Handler newCommentsHandler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //noinspection ArraysAsListWithZeroOrOneArgument
            commentsAdapter.notifyItemRangeChanged(0, commentsAdapter.getItemCount(),
                    UPDATE_TIME_SPAN); // Update the time in comments

            getNewCommentsPager().reset();
            getSupportLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null, PostActivity.this);
        }
    };

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
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        shortWebUrl = getIntent().getStringExtra(SHORT_WEB_URL);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        in.testpress.util.UIUtils.setIndeterminateDrawable(this, progressDialog, 4);
        ViewUtils.setTypeface(new TextView[] {loadPreviousCommentsText, commentsLabel,
                loadNewCommentsText, title}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {date, summary, commentsEmptyView, commentsEditText},
                TestpressSdk.getRubikRegularFont(this));

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
                Map<String, Boolean> queryParams = new LinkedHashMap<>();
                queryParams.put("short_link", true);
                Uri uri = Uri.parse(shortWebUrl);
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

    class ImageHandler {
        @JavascriptInterface
        public void onClickImage(String url) {
            Intent intent = new Intent(PostActivity.this, ZoomableImageActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }

    private void displayPost(Post post) {
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        getSupportActionBar().setTitle(post.getTitle());
        title.setText(post.getTitle());
        if (post.getSummary().trim().isEmpty()) {
            summaryLayout.setVisibility(View.GONE);
        } else {
            summary.setText(post.getSummary());
            summaryLayout.setVisibility(View.VISIBLE);
        }
        date.setText(DateUtils.getRelativeTimeSpanString(post.getPublished()));
        if (post.getContentHtml() != null) {
            WebSettings settings = content.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            settings.setJavaScriptEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setSupportZoom(true);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            content.addJavascriptInterface(new ImageHandler(), "ImageHandler");
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
                    String javascript = "javascript:var images = document.getElementsByTagName(\"img\");" +
                            "for (i = 0; i < images.length; i++) {" +
                            "   images[i].onclick = (" +
                            "       function() {" +
                            "           var src = images[i].src;" +
                            "           return function() {" +
                            "               ImageHandler.onClickImage(src);" +
                            "           }" +
                            "       }" +
                            "   )();" +
                            "}";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        content.evaluateJavascript(javascript, null);
                    } else {
                        content.loadUrl(javascript, null);
                    }
                    progressBar.setVisibility(View.GONE);
                    displayComments();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(PostActivity.this, R.color.primary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    try {
                        customTabsIntent.launchUrl(PostActivity.this, Uri.parse(url));
                    } catch (ActivityNotFoundException e) {
                        boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
                        int message = wrongUrl ? R.string.wrong_url : R.string.browser_not_available;
                        UIUtils.getAlertDialog(PostActivity.this, R.string.not_supported, message)
                                .show();
                    }
                    return true;
                }
            });
            content.loadDataWithBaseURL("file:///android_asset/", getHeader() + post.getContentHtml(), "text/html", "UTF-8", null);
        } else {
            content.setVisibility(View.GONE);
            commentsLayout.setVisibility(View.GONE);
        }
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
        imagePickerUtil = new ImagePickerUtil(activityRootLayout, this);
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
            previousCommentsPager.queryParams.put(Constants.Http.ORDER, "-submit_date");
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
            newCommentsPager.queryParams.put(Constants.Http.SINCE, latestComment.getSubmitDate());
        }
        return newCommentsPager;
    }

    @Override
    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
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
        if (newCommentsHandler == null) {
            newCommentsHandler = new Handler();
            newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
        }
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
            } else {
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
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
        newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
    }

    @OnClick(R.id.send) void sendComment() {
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

                if (newCommentsHandler != null) {
                    newCommentsHandler.removeCallbacks(runnable);
                }
                postedNewComment = true;
                getNewCommentsPager().reset();
                getSupportLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, PostActivity.this);
            }
        }.execute();
    }

    @OnClick(R.id.image_comment_button) void pickImage() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtil.onActivityResult(requestCode, resultCode, data,
                new ImagePickerUtil.ImagePickerActivityResultHandler() {
                    @Override
                    public void onStoragePermissionsRequired(Uri selectedImageUri) {
                        selectedCommentImageUri = selectedImageUri;
                    }

                    @Override
                    public void onSuccessfullyImageCropped(String imagePath) {
                        uploadImage(imagePath);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        
        imagePickerUtil.onRequestPermissionsResult(requestCode, grantResults, selectedCommentImageUri);
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

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
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
            Snackbar.make(activityRootLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(activityRootLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy () {
        if (newCommentsHandler != null) {
            newCommentsHandler.removeCallbacks(runnable);
        }
        super.onDestroy ();
    }

    @Override
    public void onLoaderReset(Loader<List<Comment>> loader) {
    }

}

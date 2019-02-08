package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.text.ParseException;
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
import in.testpress.exam.models.Vote;
import in.testpress.exam.util.ImageUtils;
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
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.ForumDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.testpress.ui.view.RoundedImageView;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.ShareUtil;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.FullScreenChromeClient;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import retrofit.RetrofitError;

import static in.testpress.testpress.util.CommonUtils.getException;

public class ForumActivity extends TestpressFragmentActivity implements
        LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String URL = "Url";
    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    public static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;
    private static final int DOWNVOTE = -1;
    private static final int UPVOTE = 1;

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.content) WebView content;
    @InjectView(R.id.title) TextView title;
    @InjectView(R.id.date) TextView date;
    @InjectView((R.id.send)) Button sendButton;
    @InjectView(R.id.upvote_button) ImageView upvoteButton;
    @InjectView(R.id.downvote_button) ImageView downButton;
    @InjectView(R.id.upvote_layout) LinearLayout upvoteLayout;
    @InjectView(R.id.downvote_layout) LinearLayout downvoteLayout;
    @InjectView(R.id.views_count) TextView viewsCount;
    @InjectView(R.id.votes_count) TextView votesCount;
    @InjectView(R.id.user_name) TextView userName;
    @InjectView(R.id.display_picture) RoundedImageView roundedImageView;
    @InjectView(R.id.content_empty_view) TextView contentEmptyView;
    @InjectView(R.id.postDetails) RelativeLayout postDetails;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;
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

    private String url;
    private ForumDao forumDao;
    private UserDao userDao;
    private Forum forum;
    private CommentsPager previousCommentsPager;
    private CommentsPager newCommentsPager;
    private CommentsListAdapter commentsAdapter;
    private ProgressDialog progressDialog;
    private SimpleDateFormat simpleDateFormat;
    private boolean postedNewComment;
    private ImageUtils imagePickerUtils;
    private FullScreenChromeClient fullScreenChromeClient;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int netVote;
    private int grayColor;
    private int primaryColor;
    private Activity activity;

    private Handler newCommentsHandler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            commentsAdapter.notifyItemRangeChanged(0, commentsAdapter.getItemCount(),
                    UPDATE_TIME_SPAN); // Update the time in comments

            getNewCommentsPager().reset();
            getSupportLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null, ForumActivity.this);
        }
    };

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_details_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        activity = this;
        grayColor = ContextCompat.getColor(this, R.color.testpress_text_gray_medium);
        primaryColor = ContextCompat.getColor(this, R.color.testpress_vote_indicator);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postDetails.setVisibility(View.GONE);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.profile_image_place_holder)
                .showImageOnLoading(R.drawable.profile_image_place_holder).build();
        in.testpress.util.UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        forumDao = TestpressApplication.getDaoSession().getForumDao();
        userDao = TestpressApplication.getDaoSession().getUserDao();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        url = getIntent().getStringExtra(URL);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        in.testpress.util.UIUtils.setIndeterminateDrawable(this, progressDialog, 4);
        fullScreenChromeClient = new FullScreenChromeClient(this);
        ViewUtils.setTypeface(new TextView[] {loadPreviousCommentsText, commentsLabel,
                loadNewCommentsText, title}, TestpressSdk.getRubikMediumFont(this));
        ViewUtils.setTypeface(new TextView[] {date, commentsEmptyView, commentsEditText},
                TestpressSdk.getRubikRegularFont(this));

        if(url != null) {
            List<Forum> forums = forumDao.queryBuilder().where(ForumDao.Properties.Url.eq(url)).list();
            if (!forums.isEmpty()) {
                forum = forums.get(0);
                if (forum.getContentHtml() != null) {
                    displayForum(forum);
                    return;
                }
            }
            // If there is no post in this url in db or
            // If it content_html is null then fetch the post
            fetchForum();
        } else {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime,
                    R.drawable.ic_error_outline_black_18dp);
        }
    }

    private void fetchForum() {
        new SafeAsyncTask<Forum>() {
            @Override
            public Forum call() {
                Map<String, Boolean> queryParams = new LinkedHashMap<>();
                queryParams.put("short_link", true);
                Uri uri = Uri.parse(url);
                return getService().getForumDetail(uri.getLastPathSegment(), queryParams);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                progressBar.setVisibility(View.GONE);
                if (e.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else if (e.getMessage().equals("404 NOT FOUND")) {
                    setEmptyText(R.string.access_denied, R.string.post_authentication_failed,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.error_loading_content,
                            R.drawable.ic_error_outline_black_18dp);
                }
            }

            @Override
            protected void onSuccess(final Forum forum) throws Exception {
                forum.setPublished(simpleDateFormat.parse(forum.getPublishedDate()).getTime());
                if (forumDao.queryBuilder()
                        .where(ForumDao.Properties.Id.eq(forum.getId())).count() != 0) {

                    forum.setModified(ForumActivity.this.forum.getModified());
                    forum.setModifiedDate(simpleDateFormat.parse(forum.getModified()).getTime());
                    if (forum.category != null) {
                        forum.setCategory(forum.category);
                        CategoryDao categoryDao =
                                TestpressApplication.getDaoSession().getCategoryDao();
                        categoryDao.insertOrReplace(forum.category);
                    }
                    User user = forum.createdBy;
                    userDao.insertOrReplace(user);
                    forum.setCreatorId(user.getId());
                    user = forum.lastCommentedBy;
                    if (user != null) {
                        userDao.insertOrReplace(user);
                        forum.setCommentorId(user.getId());
                    }
                    forumDao.update(forum);
                }
                ForumActivity.this.forum = forum;
                displayForum(forum);
            }
        }.execute();
    }

    private void displayForum(Forum forum) {
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        getSupportActionBar().setTitle("Discussions");
        title.setText(forum.getTitle());
        try {
            date.setText(DateUtils.getRelativeTimeSpanString(
                    simpleDateFormat.parse(forum.getPublishedDate()).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewsCount.setText("" + forum.getViewsCount() + " views");
        votesCount.setText("" + (forum.getUpvotes() - forum.getDownvotes()));
        if (forum.getTypeOfVote() == null) {
            upvoteButton.setColorFilter(grayColor);
            votesCount.setTextColor(grayColor);
            downButton.setColorFilter(grayColor);
        } else if (forum.getTypeOfVote() == -1) {
            upvoteButton.setColorFilter(grayColor);
            votesCount.setTextColor(primaryColor);
            downButton.setColorFilter(primaryColor);
        } else {
            upvoteButton.setColorFilter(primaryColor);
            votesCount.setTextColor(primaryColor);
            downButton.setColorFilter(grayColor);
        }
        userName.setText(forum.getCreatedBy().getFirstName() + " " + forum.getCreatedBy().getLastName());
        imageLoader.displayImage(forum.getCreatedBy().getMediumImage(), roundedImageView, options);
        if (forum.getContentHtml() != null) {
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
                            .getStringFromAsset(ForumActivity.this, "IFrameVideoWrapper.js");

                    return super.getJavascript(context) + iFrameVideoWrapper;
                }

                @Override
                protected void onNetworkError() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                protected boolean shouldOverrideUrlLoading(Activity activity, String url) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(ForumActivity.this, R.color.primary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    try {
                        customTabsIntent.launchUrl(ForumActivity.this, Uri.parse(url));
                    } catch (ActivityNotFoundException e) {
                        boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
                        int message = wrongUrl ? R.string.wrong_url : R.string.browser_not_available;
                        UIUtils.getAlertDialog(ForumActivity.this, R.string.not_supported, message)
                                .show();
                    }
                    return true;
                }
            };
            webViewUtils.initWebView(getHeader() + forum.getContentHtml(), this);
            content.setWebChromeClient(fullScreenChromeClient);
        } else {
            content.setVisibility(View.GONE);
            commentsLayout.setVisibility(View.GONE);
        }
        ViewUtils.setTypeface(new TextView[] { title, userName },
                TestpressSdk.getRubikMediumFont(activity));
        ViewUtils.setTypeface(new TextView[] { date, viewsCount },
                TestpressSdk.getRubikRegularFont(activity));
        sendButton.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        commentsEditText.setTypeface(TestpressSdk.getRubikRegularFont(activity));
        upvoteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voteForumPost(v, UPVOTE);
            }
        });
        downvoteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voteForumPost(v, DOWNVOTE);
            }
        });
    }

    private void voteForumPost(final View view, final int typeOfVote) {
        if (isSelfVote(forum.getCreatedBy().getId())) {
            showSnackBar(view, R.string.testpress_self_vote_error);
            return;
        }
        progressDialog.show();
        if (forum.getVoteId() == null) {
            castVote(view, typeOfVote);
        } else {
            if (forum.getTypeOfVote() == typeOfVote) {
                deleteVote(view);
            } else {
                updateVote(view, typeOfVote);
            }
        }
    }

    private void castVote(final View view, final int typeOfVote) {
        new SafeAsyncTask<Vote<Forum>>() {
            @Override
            public Vote<Forum> call() {
                return getService().castVote(forum, typeOfVote);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(Vote<Forum> vote) {
                progressDialog.dismiss();
                onVoteCasted(view, vote);
            }
        }.execute();
    }

    private void deleteVote(final View view) {
        new SafeAsyncTask<String>() {
            @Override
            public String call() {
                return getService().deleteCommentVote(forum);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(String response) {
                //Handle vote delete manually here
                if (forum.getTypeOfVote() == 1) {
                    forum.setUpvotes(forum.getUpvotes() - 1);
                } else {
                    forum.setDownvotes(forum.getDownvotes() - 1);
                }
                forum.setTypeOfVote(null);
                forum.setVoteId(null);
                forumDao.update(forum);
                forum = forumDao.queryBuilder().where(ForumDao.Properties.Id.eq(forum.getId())).list().get(0);
                netVote = (forum.getUpvotes() - forum.getDownvotes());
                votesCount.setText("" + netVote);
                if (forum.getTypeOfVote() == null) {
                    upvoteButton.setColorFilter(grayColor);
                    votesCount.setTextColor(grayColor);
                    downButton.setColorFilter(grayColor);
                } else if (forum.getTypeOfVote() == -1) {
                    upvoteButton.setColorFilter(grayColor);
                    votesCount.setTextColor(primaryColor);
                    downButton.setColorFilter(primaryColor);
                } else {
                    upvoteButton.setColorFilter(primaryColor);
                    votesCount.setTextColor(primaryColor);
                    downButton.setColorFilter(grayColor);
                }
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void updateVote(final View view, final int typeOfVote) {
        new SafeAsyncTask<Vote<Forum>>() {
            @Override
            public Vote<Forum> call() {
                return getService().updateCommentVote(forum, typeOfVote);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(Vote<Forum> vote) {
                progressDialog.dismiss();
                onVoteCasted(view, vote);
            }
        }.execute();
    }

    private void makeForumAlive(Vote<Forum> vote) {
        forum = vote.getContentObject();
        forum.setVoteId((long) vote.getId());
        forum.setTypeOfVote(vote.getTypeOfVote());
        User user = vote.getContentObject().createdBy;
        userDao.insertOrReplace(user);
        forum.setCreatorId(user.getId());
        user = vote.getContentObject().lastCommentedBy;
        if (user != null) {
            userDao.insertOrReplace(user);
            forum.setCommentorId(user.getId());
        }
        forumDao.update(forum);
        forum = forumDao.queryBuilder().where(ForumDao.Properties.Id.eq(forum.getId())).list().get(0);
    }

    private void onVoteCasted(View view, Vote<Forum> vote) {
        showSnackBar(view, R.string.testpress_vote_casted);
        makeForumAlive(vote);
        netVote = (forum.getUpvotes() - forum.getDownvotes());
        votesCount.setText("" + netVote);
        if (forum.getTypeOfVote() == null) {
            upvoteButton.setColorFilter(grayColor);
            votesCount.setTextColor(grayColor);
            downButton.setColorFilter(grayColor);
        } else if (forum.getTypeOfVote() == -1) {
            upvoteButton.setColorFilter(grayColor);
            votesCount.setTextColor(primaryColor);
            downButton.setColorFilter(primaryColor);
        } else {
            upvoteButton.setColorFilter(primaryColor);
            votesCount.setTextColor(primaryColor);
            downButton.setColorFilter(grayColor);
        }
        progressDialog.dismiss();
    }

    private void handleException(Exception exception, Forum forum, View view) {
        int error = R.string.testpress_some_thing_went_wrong_try_again;
        if (exception.getCause() instanceof IOException) {
            error = R.string.no_internet_try_again;
        } else if (exception instanceof RetrofitError) {
            if (((RetrofitError) exception).getResponse().getStatus() == 400) {
                error = R.string.testpress_self_vote_error;
                if (TestpressSdk.getTestpressUserId(activity) != forum.getCreatedBy().getId()) {
                    TestpressSdk.setTestpressUserId(activity,
                            Integer.parseInt(forum.getCreatedBy().getId() + ""));
                }
            }
        }
        showSnackBar(view, error);
        progressDialog.dismiss();
    }

    private boolean isSelfVote(long id) {
        return TestpressSdk.isTestpressUserIdExist(getBaseContext()) &&
                (id == TestpressSdk.getTestpressUserId(getBaseContext()));
    }

    public static void showSnackBar(View view, @StringRes int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /*

        There is a bug in RecyclerView which causes views that
        are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
        adapter size has decreased since the ViewHolder was recycled.

        RecyclerView.dispatchLayout() can try to pull items from the scrap before calling
        mRecycler.clearOldPositions(). The consequence being, is that it was pulling items from the
        common pool that had positions heigher than the adapter size.

        Fortunately, it only does this if PredictiveAnimations are enabled, so my solution was to
        subclass GridLayoutManager (LinearLayoutManager has the same problem and 'fix'), and
        override supportsPredictiveItemAnimations() to return false :

        https://stackoverflow.com/questions/30220771/recyclerview-inconsistency-detected-invalid-item-position

     */

    private static class listViewCustomManager extends LinearLayoutManager {

        listViewCustomManager(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    void displayComments() {
        commentsAdapter = new CommentsListAdapter(this);
        listView.setNestedScrollingEnabled(false);
        listView.setLayoutManager(new listViewCustomManager(this));
        listView.setAdapter(commentsAdapter);
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                getSupportLoaderManager()
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, ForumActivity.this);
            }
        });
        loadNewCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewCommentsLayout.setVisibility(View.GONE);
                getSupportLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, ForumActivity.this);
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
        getSupportLoaderManager().initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, ForumActivity.this);
    }

    @NonNull
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
            previousCommentsPager = new CommentsPager(getService(), forum.getId());
            previousCommentsPager.queryParams.put(Constants.Http.ORDER, "-created");
            previousCommentsPager.queryParams.put(Constants.Http.UNTIL,
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").format(new Date()));
        }
        return previousCommentsPager;
    }

    CommentsPager getNewCommentsPager() {
        if (newCommentsPager == null) {
            newCommentsPager = new CommentsPager(getService(), forum.getId());
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
            if (forum.getCommentsCount() == 0) {
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
        if (forum.getCommentsCount() < getPreviousCommentsPager().getCommentsCount()) {
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
            int noOfComments = forum.getCommentsCount() + getNewCommentsPager().getCommentsCount();
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
        intent.putExtra(URL, url);
        startActivity(intent);
    }

    void postComment(final String comment) {
        new SafeAsyncTask<Comment>() {
            public Comment call() {
                return getService().sendComments(forum.getId(), comment);
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
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, ForumActivity.this);
            }
        }.execute();
    }

    @OnClick(R.id.image_comment_button) void pickImage() {
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
                testpressService = serviceProvider.getService(ForumActivity.this);
            } catch (IOException | AccountsException e) {
                e.printStackTrace();
            }
        }
        return testpressService;
    }

    void updateCommentsCount(int count) {
        List<Forum> forums = forumDao.queryBuilder()
                .where(ForumDao.Properties.Id.eq(forum.getId())).list();

        if (!forums.isEmpty()) {
            Forum forum = forums.get(0);
            forum.setCommentsCount(count);
            forum.update();
        }
        forum.setCommentsCount(count);
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

    protected void setEmptyText(final int title, final int description, final int left){
        if (forum != null) {
            contentEmptyView.setText(description);
            contentEmptyView.setVisibility(View.VISIBLE);
            displayForum(forum);
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
    public void onLoaderReset(@NonNull Loader<List<Comment>> loader) {
    }

}
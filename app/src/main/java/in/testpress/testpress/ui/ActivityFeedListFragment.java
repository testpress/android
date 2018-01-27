package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.ActivityFeedPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Activity;
import in.testpress.testpress.models.ActivityDao;
import in.testpress.testpress.models.ActivityFeedResponse;
import in.testpress.testpress.models.AssessmentDao;
import in.testpress.testpress.models.AttachmentContent;
import in.testpress.testpress.models.AttachmentContentDao;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.ChapterContent;
import in.testpress.testpress.models.ChapterContentAttempt;
import in.testpress.testpress.models.ChapterContentAttemptDao;
import in.testpress.testpress.models.ChapterContentDao;
import in.testpress.testpress.models.ContentType;
import in.testpress.testpress.models.ContentTypeDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.FeedAttachment;
import in.testpress.testpress.models.FeedAttachmentDao;
import in.testpress.testpress.models.FeedChapter;
import in.testpress.testpress.models.FeedChapterDao;
import in.testpress.testpress.models.FeedContentDao;
import in.testpress.testpress.models.FeedExam;
import in.testpress.testpress.models.FeedExamDao;
import in.testpress.testpress.models.FeedHtmlContent;
import in.testpress.testpress.models.FeedHtmlContentDao;
import in.testpress.testpress.models.FeedPost;
import in.testpress.testpress.models.FeedPostDao;
import in.testpress.testpress.models.FeedVideo;
import in.testpress.testpress.models.FeedVideoDao;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Ln;
import in.testpress.ui.*;
import in.testpress.ui.ExploreSpinnerAdapter;
import in.testpress.util.ViewUtils;
import info.hoang8f.widget.FButton;

import static com.facebook.FacebookSdk.getApplicationContext;
import static in.testpress.testpress.util.CommonUtils.getLoaderException;

public class ActivityFeedListFragment  extends Fragment implements
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, LoaderManager
        .LoaderCallbacks<ActivityFeedResponse> {

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(android.R.id.list) ListView listView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) FButton retryButton;
    ActivityFeedPager pager;
    ActivityFeedListAdapter adapter;
    boolean authorizationChecked = false;
    View loadingLayout;
    ActivityDao activityDao;
    UserDao userDao;
    FeedAttachmentDao attachmentDao;
    AttachmentContentDao attachmentContentDao;
    CategoryDao categoryDao;
    FeedVideoDao videoDao;
    FeedPostDao postDao;
    FeedChapterDao chapterDao;
    ContentTypeDao contentTypeDao;
    FeedHtmlContentDao htmlContentDao;
    FeedExamDao examDao;
    ChapterContentAttemptDao chapterContentAttemptDao;
    ChapterContentDao chapterContentDao;
    AssessmentDao assessmentDao;
    FeedContentDao contentDao;
    private DaoSession daoSession;
    protected in.testpress.ui.ExploreSpinnerAdapter spinnerAdapter;
    private boolean spinnerDefaultCallback;
    private int selectedItemPosition;
    private Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Activity Feed");
        Injector.inject(this);
        activityDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getActivityDao();
        userDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getUserDao();
        attachmentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedAttachmentDao();
        attachmentContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getAttachmentContentDao();
        categoryDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getCategoryDao();
        videoDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedVideoDao();
        postDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedPostDao();
        chapterDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedChapterDao();
        contentTypeDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getContentTypeDao();
        htmlContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedHtmlContentDao();
        examDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedExamDao();
        chapterContentAttemptDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getChapterContentAttemptDao();
        chapterContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getChapterContentDao();
        assessmentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getAssessmentDao();
        contentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedContentDao();
        daoSession = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh_list, null);
        ButterKnife.inject(this, view);
        spinnerAdapter = new ExploreSpinnerAdapter(inflater, getResources(), true);
        spinnerAdapter.hideSpinner(true);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        adapter = new ActivityFeedListAdapter(getActivity(), R.layout.testpress_activity_feed_list_item);
        adapter.setFilterVerb("all");
        listView.setAdapter(adapter);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
            }
        });
    }

//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
//        // Inflate the clear; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.clear, menu);
//        Drawable drawable = menu.getItem(menu.size() - 1).getIcon();
//        if(drawable != null) {
//            drawable.mutate();
//            drawable.setColorFilter(getResources().getColor(R.color.testpress_white),
//                    PorterDuff.Mode.SRC_ATOP);
//        }
//        super.onCreateOptionsMenu(menu, menuInflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.clear) {
//            clearActivityFeed(daoSession);
//            displayDataFromDB();
//            Toast.makeText(getActivity(), "Feed cleared!", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        try {
            Log.e("Inside", "onCreateOptionsMenu1");
            menuInflater.inflate(R.menu.testpress_filter, menu);
            MenuItem filterMenu = menu.findItem(R.id.filter);
            filterMenu.setVisible(true);
            View actionView = MenuItemCompat.getActionView(filterMenu);
            final View circle = actionView.findViewById(in.testpress.R.id.filter_applied_sticky_tick);
            spinner = (Spinner) actionView.findViewById(in.testpress.R.id.spinner);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                spinner.setPopupBackgroundResource(R.color.testpress_white);
            }
            spinnerDefaultCallback = true;
            spinner.setAdapter(spinnerAdapter);
            Log.e("Inside", "onCreateOptionsMenu2");
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                    if (position == 0) {
                        circle.setVisibility(View.GONE);
                    } else {
                        circle.setVisibility(View.VISIBLE);
                    }
                    if (spinnerDefaultCallback) {
                        spinnerDefaultCallback = false;
                    } else if ((selectedItemPosition != position)) { // Omit callback if position is already selected position
                        selectedItemPosition = position;
                        //Use tag here
//                        switch (selectedItemPosition) {
//                            case 0 :
//                                adapter.setFilterVerb("all");
//                                break;
//                            case 1 :
//                                adapter.setFilterVerb("added");
//                                break;
//                            case 2 :
//                                adapter.setFilterVerb("attempted");
//                                break;
//                        }
                        adapter.setFilterVerb(spinnerAdapter.getTag(selectedItemPosition));
                        adapter.notifyDataSetChanged();
                        if (adapter.getCount() == 0) {
                            setEmptyText(R.string.no_activities, R.string.no_activities_in_filter_description,
                                    R.drawable.ic_error_outline_black_18dp);
                            retryButton.setVisibility(View.GONE);
                        } else  {
                            showList();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            Log.e("Inside", "onCreateOptionsMenu3");
        } catch (Exception exp) {
            Log.e("Exception", exp.toString());
        }
        addFilterItemsInSpinner();
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.measure(View.MEASURED_SIZE_MASK,View.MEASURED_HEIGHT_STATE_SHIFT);
        if (adapter.isEmpty()) {
            swipeLayout.setRefreshing(true);
        }
        getLoaderManager().initLoader(1, null, this);
        displayDataFromDB();
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
    }

    @Override
    public void onRefresh() {
        Log.e("Inside", "onRefresh");
        clearActivityFeed(daoSession);
        getLoaderManager().destroyLoader(1);
        getLoaderManager().initLoader(1, null, this);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<ActivityFeedResponse> onCreateLoader(int loaderID, Bundle args) {
        Log.e("Inside", "onCreateLoader");
            return new ThrowableLoader<ActivityFeedResponse>(getActivity(), null) {
                    @Override
                    public ActivityFeedResponse loadData() throws IOException {
                        if (pager == null) {
                            try {
                                pager = new ActivityFeedPager(serviceProvider.getService(getActivity()));
                            } catch (AccountsException e) {
                                e.printStackTrace();
                            }
                        }
//                        do {
//                            pager.next();
//                        } while (pager.hasNext());
                        pager.next();
                        return pager.getResources();
                    }
                };
    }

    @Override
    public void onLoadFinished(Loader<ActivityFeedResponse> loader, ActivityFeedResponse data) {
        Log.e("Inside", "onLoadFinished");
        if (getActivity() == null) {
            return;
        }
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        final Exception exception = getLoaderException(loader);
        Log.e("Inside", "load finished");
        if (exception == null) {
            //Remove the swipe refresh icon and the sticky notification if any
//            if (pager != null && !pager.hasMore()) {
//                if (adapter.getFootersCount() != 0) {  //if pager reached last page remove footer
//                    // if footer added already
//                    adapter.removeFooter(loadingLayout);
//                }
//            }
//            adapter.addData(data);
            saveDataInDB(data);
            displayDataFromDB();
        } else {
            Log.e("Inside", "Error occured");
            Log.e("Exception", exception+"");
            showError(getErrorMessage(exception));
        }
    }

    private void saveDataInDB(ActivityFeedResponse activityFeedResponse) {
        Log.e("Inside", "saveDataInDB");
        for (User user : activityFeedResponse.getUsers()) {
            userDao.insertOrReplace(user);
        }
        for (AttachmentContent attachmentContent : activityFeedResponse.getAttachmentContents()) {
            attachmentContentDao.insertOrReplace(attachmentContent);
        }
        for (Category category : activityFeedResponse.getPostcategories()) {
            categoryDao.insertOrReplace(category);
        }
        for (FeedVideo video : activityFeedResponse.getVideoContents()) {
            videoDao.insertOrReplace(video);
        }
        for (FeedPost post : activityFeedResponse.getPosts()) {
            postDao.insertOrReplace(post);
        }
        for (FeedChapter chapter : activityFeedResponse.getChapters()) {
            chapterDao.insertOrReplace(chapter);
        }
        for (ContentType contentType : activityFeedResponse.getContentTypes()) {
            contentTypeDao.insertOrReplace(contentType);
        }
        for (FeedHtmlContent htmlContent : activityFeedResponse.getHtmlContents()) {
            htmlContentDao.insertOrReplace(htmlContent);
        }
        for (FeedExam exam : activityFeedResponse.getExams()) {
            examDao.insertOrReplace(exam);
        }
        for (ChapterContentAttempt chapterContentAttempt : activityFeedResponse.getChaptercontentattempts()) {
            chapterContentAttempt.__setDaoSession(((TestpressApplication) getApplicationContext()).getDaoSession());
            if (chapterContentAttempt.video != null) {
                videoDao.insertOrReplace(chapterContentAttempt.video);
                chapterContentAttempt.setVideoId(chapterContentAttempt.video.getId());
            }
            if (chapterContentAttempt.assessment != null) {
                assessmentDao.insertOrReplace(chapterContentAttempt.assessment);
                chapterContentAttempt.setAssessmentId(chapterContentAttempt.assessment.getId());
            }
            if (chapterContentAttempt.content != null) {
                contentDao.insertOrReplace(chapterContentAttempt.content);
                chapterContentAttempt.setContentId(chapterContentAttempt.content.getId());
            }
            if (chapterContentAttempt.attachment != null) {
                attachmentDao.insertOrReplace(chapterContentAttempt.attachment);
                chapterContentAttempt.setAttachmentId(chapterContentAttempt.attachment.getId());
            }
            chapterContentAttemptDao.insertOrReplace(chapterContentAttempt);
        }
        for (ChapterContent chapterContent : activityFeedResponse.getChaptercontents()) {
            chapterContentDao.insertOrReplace(chapterContent);
        }
        for (Activity activity : activityFeedResponse.getActivities()) {
            if (checkIfTargetAndActionExists(activity)) {
                activityDao.insertOrReplace(activity);
            } else {
                Log.e("Not available", "11111111111111");
            }
        }
    }

    private boolean checkIfTargetAndActionExists(Activity feedActivity) {
        int id = Integer.parseInt(feedActivity.getActionObjectObjectId());
        String model = getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel();
        switch (model) {
            case "post" :
                return postDao.queryBuilder().where(PostDao.Properties.Id.eq(id)).count() != 0;
            case "chapter" :
                return chapterDao.queryBuilder()
                        .where(FeedChapterDao.Properties.Id.eq(id)).count() != 0;
            case "chaptercontent" :
                return chapterContentDao.queryBuilder()
                        .where(ChapterContentDao.Properties.Id.eq(id)).count() != 0;
            case "chaptercontentattempt" :
                return chapterContentAttemptDao.queryBuilder()
                        .where(ChapterContentAttemptDao.Properties.Id.eq(id)).count() != 0;
            case "user" :
                return userDao.queryBuilder()
                        .where(UserDao.Properties.Id.eq(id)).count() != 0;
            case "exam" :
                return examDao.queryBuilder()
                        .where(FeedExamDao.Properties.Id.eq(id)).count() != 0;
        }
        id = feedActivity.getTargetObjectId();
        model = getContentTypeWithId(feedActivity.getTargetContentType()).getModel();
        switch (model) {
            case "post" :
                return postDao.queryBuilder().where(PostDao.Properties.Id.eq(id)).count() != 0;
            case "chapter" :
                return chapterDao.queryBuilder()
                        .where(FeedChapterDao.Properties.Id.eq(id)).count() != 0;
            case "chaptercontent" :
                return chapterContentDao.queryBuilder()
                        .where(ChapterContentDao.Properties.Id.eq(id)).count() != 0;
            case "chaptercontentattempt" :
                return chapterContentAttemptDao.queryBuilder()
                        .where(ChapterContentAttemptDao.Properties.Id.eq(id)).count() != 0;
            case "user" :
                return userDao.queryBuilder()
                        .where(UserDao.Properties.Id.eq(id)).count() != 0;
            case "exam" :
                return examDao.queryBuilder()
                        .where(FeedExamDao.Properties.Id.eq(id)).count() != 0;
        }
        return false;
    }

    private ContentType getContentTypeWithId(int id) {
        if (contentTypeDao.queryBuilder().where(ContentTypeDao.Properties.Id.eq(id)).count() != 0) {
            return contentTypeDao.queryBuilder()
                    .where(ContentTypeDao.Properties.Id.eq(id)).list().get(0);
        }
        return new ContentType();
    }

    @Override
    public void onLoaderReset(Loader<ActivityFeedResponse> loader) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.e("firstVisibleItem", firstVisibleItem+"");
        Log.e("visibleItemCount", visibleItemCount+"");
        Log.e("totalItemCount", totalItemCount+"");
    }

    //This just notifies the adapter that new data is now available in db
    void displayDataFromDB() {
        Log.e("Inside", "displayDataFromDB");
        Ln.d("Adapter notifyDataSetChanged displayDataFromDB");
        adapter.notifyDataSetChanged();
        if (!adapter.isEmpty()) {
            showList();
        }
        if ( (pager != null && !pager.hasMore() && adapter.getCount() == 0) || activityDao.count() == 0) {
            setEmptyText(R.string.no_activities, R.string.no_activities_description,
                    R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.GONE);
        }
    }

    private void addFilterItemsInSpinner() {
        spinnerAdapter.clear();
        spinnerAdapter.addItem("all", "All activities", false, 0);
        spinnerAdapter.addItem("added", "Admin activities", false, 0);
        spinnerAdapter.addItem("attempted", "My activities", false, 0);
        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            spinnerAdapter.notifyDataSetChanged();
        } else {
            spinnerAdapter.notifyDataSetChanged();
            spinner.setSelection(selectedItemPosition);
        }
    }

    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    protected int getErrorMessage(Exception exception) {
        if (exception.getCause() instanceof IOException) {
            if (adapter.getCount() == 0) {
                setEmptyText(R.string.network_error, R.string.no_internet,R.drawable.ic_error_outline_black_18dp);
            }
            return R.string.no_internet;
        } else {
            if (adapter.getCount() == 0) {
                setEmptyText(R.string.error_loading_activities, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
            }
            return R.string.error_loading_activities;
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        listView.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    protected void showList() {
        emptyView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
    }

    /**
     * Initialize {@link TestpressService} with auth-token for the first time & returns the same
     * instance afterwards.
     *
     * @return TestpressService
     */
    TestpressService getTestpressService() {
        if (!authorizationChecked) {
            if (CommonUtils.isUserAuthenticated(getActivity())) {
                try {
                    Log.e("sssss","serviceProvider");
                    testpressService = serviceProvider.getService(getActivity());
                } catch (AccountsException e) {
                } catch (IOException e) {
                }
            }
            authorizationChecked = true;
        }
        return testpressService;
    }

    public void clearActivityFeed(DaoSession daoSession) {
        Log.e("Inside", "clearActivityFeed");
        daoSession.getActivityFeedResponseDao().deleteAll();
        daoSession.getActivityDao().deleteAll();
        daoSession.getFeedAttachmentDao().deleteAll();
        daoSession.getFeedChapterDao().deleteAll();
        daoSession.getFeedContentDao().deleteAll();
        daoSession.getFeedExamDao().deleteAll();
        daoSession.getFeedHtmlContentDao().deleteAll();
        daoSession.getFeedPostDao().deleteAll();
        daoSession.getFeedVideoDao().deleteAll();
        daoSession.getAssessmentDao().deleteAll();
        daoSession.getAttachmentContentDao().deleteAll();
        daoSession.getChapterContentDao().deleteAll();
        daoSession.getChapterContentAttemptDao().deleteAll();
        daoSession.getContentTypeDao().deleteAll();
//        daoSession.getUserDao().deleteAll();
//        daoSession.getCategoryDao().deleteAll();
    }
}

package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.dao.query.LazyList;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ForumsPager;
import in.testpress.testpress.core.PostCategoryPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.ForumDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import info.hoang8f.widget.FButton;

public class ForumListFragment extends Fragment implements
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, LoaderManager
        .LoaderCallbacks<List<Forum>> {

    @Inject protected TestpressService testpressService;
    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;
    @InjectView(android.R.id.list)
    ListView listView;
    @InjectView(R.id.sticky)
    TextView mStickyView;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) FButton retryButton;
    FloatingActionButton floatingActionButton;
    HeaderFooterListAdapter<ForumListAdapter> adapter;
    ForumsPager refreshPager;
    ForumsPager pager;
    View loadingLayout;
    ForumDao forumDao;
    CategoryDao categoryDao;
    DaoSession daoSession;
    LazyList<Forum> forums;
    int lastFirstVisibleItem;
    boolean isScrollingUp;
    boolean isUserSwiped;
    private ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    private View mSpinnerContainer;
    private Boolean mFistTimeCallback = false;

    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom forums
    private static final int POSTS_LOADER_ID = 1;

    // Number of maximum forums which can be missed from the latest before the db will get reset
    private static final int MISSED_POSTS_THRESHOLD = 50;

    Long categoryFilter = null;
    boolean authorizationChecked = false;
    PostCategoryPager categoryPager;
    List<Category> categories = new ArrayList<>();
    private User user;
    private UserDao userDao;
    private Forum forum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getLong("category_filter") != 0) {
                categoryFilter = getArguments().getLong("category_filter");
            }
        }
        //Get the dao handles for forums and categories
        daoSession = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();
        forumDao = daoSession.getForumDao();
        userDao = daoSession.getUserDao();
        categoryDao = daoSession.getCategoryDao();
        //Enable options. This will trigger onCreateOptionsMenu
        setHasOptionsMenu(true);
        Injector.inject(this);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(),
                getActivity().getResources(), true);
        mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_discussions), false, 0);
        mTopLevelSpinnerAdapter.addHeader(getString(R.string.categories));
        Toolbar toolbar;
        if (getActivity() instanceof MainActivity) {
            toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
//            return;
        } else {
            toolbar = ((ForumListActivity) (getActivity())).getActionBarToolbar();
        }
        mSpinnerContainer = getActivity().getLayoutInflater().inflate(R.layout.actionbar_spinner,
                toolbar, false);

        Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mTopLevelSpinnerAdapter);
        Ln.e("Getting actiobar toolbar");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long
                    itemId) {
                if (!mFistTimeCallback) {
                    mFistTimeCallback = true;
                    return;
                }
                listView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                String filter = mTopLevelSpinnerAdapter.getTag(position);
                if (filter.isEmpty()) {
                    adapter.getWrappedAdapter().clearCategoryFilter();
                } else {
                    adapter.getWrappedAdapter().setCategoryFilter(Long.parseLong(filter));
                }
                displayDataFromDB();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (getActivity() instanceof MainActivity) {
            mSpinnerContainer.setVisibility(View.GONE);
        } else {
            mSpinnerContainer.setVisibility(View.VISIBLE);
        }
//        mSpinnerContainer.setVisibility(View.VISIBLE);
//        spinner.setVisibility(View.GONE);//
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh_list, null);
        ButterKnife.inject(this, view);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_button);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (testpressService == null) {
            testpressService = getTestpressService();
        }
        adapter = new HeaderFooterListAdapter<ForumListAdapter>(listView, new ForumListAdapter
                (serviceProvider, getActivity(), R.layout.forum_list_item));
        listView.setAdapter(adapter);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(CreateForumActivity.createIntent(getActivity(), categories), 2);
            }
        });
        floatingActionButton.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.testpress_color_primary)));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.measure(View.MEASURED_SIZE_MASK,View.MEASURED_HEIGHT_STATE_SHIFT);
        swipeLayout.setRefreshing(true);
        getLoaderManager().initLoader(REFRESH_LOADER_ID, null, this);
        fetchCategories();
        if (categoryFilter != null) {
            adapter.getWrappedAdapter().setCategoryFilter(categoryFilter);
        }
        displayDataFromDB();
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            refreshWithProgress();
        }
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
                    testpressService = serviceProvider.getService(getActivity());
                } catch (AccountsException e) {
                } catch (IOException e) {
                }
            }
            authorizationChecked = true;
        }
        return testpressService;
    }

    PostCategoryPager getCategoryPager() {
        if (categoryPager == null) {
            categoryPager = new PostCategoryPager(getTestpressService());
            return categoryPager;
        }
        return categoryPager;
    }

    public void fetchCategories() {
//        if (getActivity() instanceof MainActivity)
//            return;
        new SafeAsyncTask<List<Category>>() {
            @Override
            public List<Category> call() throws Exception {
                do {
                    getCategoryPager().next();
                    categories = getCategoryPager().getResources();
                } while (getCategoryPager().hasNext());
                return categories;
            }

            protected void onSuccess(final List<Category> categories) throws Exception {
                Ln.e("On success");
                categoryDao.insertOrReplaceInTx(categories);
                mTopLevelSpinnerAdapter.clear();
                mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_discussions), false, 0);
                mTopLevelSpinnerAdapter.addHeader(getString(R.string.categories));
                for (final Category category : categories) {
                    mTopLevelSpinnerAdapter.addItem("" + category.getId(), category.getName(), true,
                            Color.parseColor("#" + category.getColor()));
                }

                mTopLevelSpinnerAdapter.notifyDataSetChanged();

                if (categoryFilter != null) {
                    Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
                    spinner.setSelection(mTopLevelSpinnerAdapter.getItemPositionFromTag(categoryFilter.toString()));
                }

                if (!categories.isEmpty()) {
                    Ln.e("Setting visible");
//                    mSpinnerContainer.setVisibility(View.VISIBLE);
                    Toolbar toolbar;
                    if (getActivity() instanceof MainActivity) {
                        toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
                        mSpinnerContainer.setVisibility(View.GONE);
                    } else {
                        toolbar = ((ForumListActivity) (getActivity())).getActionBarToolbar();
                        mSpinnerContainer.setVisibility(View.VISIBLE);
                    }
                    View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
                    toolbar.removeView(view);
                    toolbar.invalidate();
                    ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    toolbar.addView(mSpinnerContainer, lp);
                }
            }

        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Forum>> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case REFRESH_LOADER_ID:
                return new ThrowableLoader<List<Forum>>(getActivity(), null) {
                    @Override
                    public List<Forum> loadData() throws IOException {
                        if (refreshPager == null) {
                            initRefreshPager();
                        }
                        refreshPager.next();
                        return refreshPager.getResources();
                    }
                };
            case POSTS_LOADER_ID:
                return new ThrowableLoader<List<Forum>>(getActivity(), null) {
                    @Override
                    public List<Forum> loadData() throws IOException {
                        if (pager == null) {
                            initOldPostLoadingPager();
                        }
                        pager.next();
                        return pager.getResources();
                    }
                };
            default:
                //An invalid id was passed
                return null;
        }
    }

    void initRefreshPager() {
        Log.e("Inside", "init Refresh");
        if (refreshPager == null) {
            refreshPager = new ForumsPager(getTestpressService(), forumDao);
//            refreshPager.setQueryParams("order", "-published_date");
            if (forumDao.count() > 0) {
                Forum latest = forumDao.queryBuilder().orderDesc(ForumDao.Properties.ModifiedDate)
                        .list().get(0);
                refreshPager.setLatestModifiedDate(latest.getModified());
                LogLatestForumModifiedDate(latest);
                LogAllForums();
            }
        }
    }

    void initOldPostLoadingPager() {
        Log.e("Inside", "init OldPostLoadingPager");
        pager = new ForumsPager(getTestpressService(), null);
//        pager.setQueryParams("order", "-published_date");
        Forum lastForum = forumDao.queryBuilder().orderDesc(ForumDao.Properties
                .Published).list().get((int) forumDao.count() - 1);
        pager.setQueryParams("until", lastForum.getLastCommentedTime());
        Log.e("latestModified", lastForum.getPublishedDate());
        pager.setLatestModifiedDate(null);
    }

    @Override
    public void onLoadFinished(Loader<List<Forum>> loader, List<Forum> data) {
        Log.e("Inside", "onLoadFinished");
        if (getActivity() == null) {
            return;
        }
        final Exception exception = getException(loader);
        if (exception != null) {
            //Remove the swipe refresh icon and the sticky notification if any
            swipeLayout.setRefreshing(false);
            if (pager != null && !pager.hasMore()) {
                if (adapter.getFootersCount() != 0) {  //if pager reached last page remove footer
                    // if footer added already
                    adapter.removeFooter(loadingLayout);
                }
            }
            displayDataFromDB();
            showError(getErrorMessage(exception));
            return;
        }

        switch (loader.getId()) {
            case REFRESH_LOADER_ID:
                onRefreshLoadFinished(data);
                break;
            case POSTS_LOADER_ID:
                onNetworkLoadFinished(data);
                break;
            default:
                return;
        }
    }

    void onRefreshLoadFinished(List<Forum> items) {
        Log.e("Inside", "refresh load finished");
//        for (Forum forum : items) {
//            Log.e("refresh finished",forum.getTitle());
//        }
        //If no data is available in the local database, directly insert
        //display from database
        Ln.e(swipeLayout.isRefreshing());
        if ((forumDao.count() == 0) || items == null || items.isEmpty()) {

            Log.e("Inside", "if");
            //Remove the swipe refresh icon and the sticky notification if any
            swipeLayout.setRefreshing(false);
            mStickyView.setVisibility(View.GONE);

            //Return if no new forums are available
            if (items == null || items.isEmpty()) {
                Log.e("Inside", "empty");
                displayDataFromDB();
                if (forumDao.count() == 0) {
                    setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.ic_error_outline_black_18dp);
                    retryButton.setVisibility(View.GONE);
                }
                return;
            }
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            //Insert the categories and forums to the database
            writeToDB(items);

            //Trigger displaying data
            displayDataFromDB();
        } else {
            Log.e("Inside", "else");
            //If data is already available in the local database, then
            //notify user about the new data to view latest data.
            Ln.d(MISSED_POSTS_THRESHOLD >= refreshPager.getTotalCount());
            if (MISSED_POSTS_THRESHOLD >= refreshPager.getTotalCount()) {
                if (refreshPager.hasNext()) {
                    refreshPager.clearQueryParams();
                    getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, ForumListFragment
                            .this);
                    return;
                }
            }
            if (isUserSwiped || (lastFirstVisibleItem == 0)) {
                displayNewPosts();
            } else {
                mStickyView.setVisibility(View.VISIBLE);
            }
        }
    }

    void onNetworkLoadFinished(List<Forum> items) {

        if (pager != null && !pager.hasMore()) {
            if (adapter.getFootersCount() != 0) {  //if pager reached last page remove footer if
                // footer added already
                adapter.removeFooter(loadingLayout);
            }
        }
        //Return if no new forums are available
        if (items.isEmpty()) {
            Log.e("Inside", "No new item found");
            return;
        }

        Log.e("Pager has more", pager.hasMore()+"");

        //Insert forums to the database
        writeToDB(items);

        //Trigger displaying data
        displayDataFromDB();
    }

    //This just notifies the adapter that new data is now available in db
    void displayDataFromDB() {
        Ln.d("Adapter notifyDataSetChanged displayDataFromDB");
        adapter.notifyDataSetChanged();

        if (forumDao.count() == 0 || (pager != null && !pager.hasMore() && adapter.getCount() == 0)) {
            setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getId() == listView.getId()) {
            final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            if (currentFirstVisibleItem > lastFirstVisibleItem) {
                isScrollingUp = false;
            } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
                isScrollingUp = true;
            }
            lastFirstVisibleItem = currentFirstVisibleItem;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int
            totalItemCount) {
        // If the ListView is the only one child of the SwipeRefreshLayout we wouldn’t have any
        // kind of problems, because everything works smoothly. In some cases we have not only the
        // ListView but we have other elements. This case is a little bit more complex, because if
        // we scroll up the items in the ListView everything works as expected, but if the scroll
        // down the refresh process starts and the list items doesn’t scroll as we want. In this
        // case we can use a trick, we can disable the refresh notification using setEnabled(false)
        // and enable it again as soon as the first item in the ListView is visible.
        // Here we override the onScrollListener of the ListView to handle the enable/disable
        // mechanism
        // See more at:
        // http://www.survivingwithandroid.com/2014/05/android-swiperefreshlayout-tutorial.html
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }
        if (getActivity() == null)
            return;

        if (getLoaderManager().hasRunningLoaders())
            return;

        if (listView != null && (forumDao.count() != 0) && !isScrollingUp &&
                (listView.getLastVisiblePosition() + 3) >= adapter.getWrappedAdapter().getCount()) {

            Ln.d("Onscroll showing more");

            if (pager == null) {
                if (listView.getVisibility() != View.VISIBLE) {
                    listView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                if (adapter.getFootersCount() == 0) { //display loading footer if not present
                    // when loading next page
                    adapter.addFooter(loadingLayout);
                }
            } else {
                if (!pager.hasMore()) {
                    if (adapter.getFootersCount() != 0) {  //if pager reached last page remove
                        // footer if footer added already
                        adapter.removeFooter(loadingLayout);
                    }
                    if (adapter.getCount() == 0) {
                        setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.ic_error_outline_black_18dp);
                        retryButton.setVisibility(View.GONE);
                    }
                    return;
                }
                pager.clearQueryParams();
            }
            getLoaderManager().restartLoader(POSTS_LOADER_ID, null, ForumListFragment.this);
        }
    }

    @Override
    public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (swipeLayout == null) {
                    return;
                }
                swipeLayout.setRefreshing(true);
                mStickyView.setVisibility(View.GONE);
                isUserSwiped = true;
                refreshPager.clear();
                refreshPager.setQueryParams("order", "-published_date");
                if (forumDao.count() > 0) {
                    Forum latest = forumDao.queryBuilder().orderDesc(ForumDao.Properties
                            .ModifiedDate).list().get(0);
                    refreshPager.setLatestModifiedDate(latest.getModified());
                    LogLatestForumModifiedDate(latest);
                }
                getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, ForumListFragment.this);
                categories.clear();
                categoryPager.clear();
                fetchCategories();
            }
        });
    }

    @OnClick(R.id.sticky)
    public void displayNewPosts() {
        Log.e("Inside", "displayNewPosts");
        Ln.d("Sticky Clicked");
        mStickyView.setVisibility(View.GONE);
        //Remove the swipe refresh icon if present
        swipeLayout.setRefreshing(false);
        Ln.d(MISSED_POSTS_THRESHOLD < refreshPager.getTotalCount());
        if (MISSED_POSTS_THRESHOLD < refreshPager.getTotalCount()) {
            clearDB();
            onRefresh();
        } else {
            //Insert forums to the database
            writeToDB(refreshPager.getResources());
        }
        //Trigger displaying data
        displayDataFromDB();
    }

    protected void writeToDB(List<Forum> forums) {
        Log.e("Inside", "writeToDB");
//      Insert the categories and forums to the database
//        this.forums.addAll(forums);
        List<Category> categories = new ArrayList<Category>();
        for (Forum forum : forums) {
            if (forum.category != null) {
                Ln.e("Post category for " + forum.getTitle() + " is " + forum.category.name);
                categories.add(forum.category);
            } else {
                Ln.e("Post category for " + forum.getTitle() + " is null");
            }
        }
        categoryDao.insertOrReplaceInTx(categories);
        for (Forum forumTemp : forums) {
            user = forumTemp.createdBy;
            userDao.insertOrReplace(user);
            forumTemp.setCreatorId(user.getId());
            user = forumTemp.lastCommentedBy;
            if (user != null) {
                userDao.insertOrReplace(user);
                forumTemp.setCommentorId(user.getId());
            }
            forumDao.insertOrReplace(forumTemp);
        }
//        forumDao.insertOrReplaceInTx(forums);
        LogAllForums();
    }

    protected int getErrorMessage(Exception exception) {
        if (exception.getCause() instanceof IOException) {
            if (adapter.getCount() == 0) {
                setEmptyText(R.string.network_error, R.string.no_internet,R.drawable.ic_error_outline_black_18dp);
            }
            return R.string.no_internet;
        } else {
            if (adapter.getCount() == 0) {
                setEmptyText(R.string.error_loading_posts, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
            }
            return R.string.error_loading_posts;
        }
    }

    @Override
    public void onStop() {
        if (forums != null) {
            forums.close();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.retry_button)
    protected void refreshWithProgress() {
        Log.e("Inside", "refreshWithProgress");
        emptyView.setVisibility(View.GONE);
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
        onRefresh();
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        listView.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    protected Exception getException(final Loader<List<Forum>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Forum>>) loader).clearException();
        } else {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    void clearDB() {
        Ln.d("ClearDB");
        forumDao.deleteAll();
        refreshPager.removeQueryParams("since");
        pager = null;
        displayDataFromDB();
    }

    void LogAllForums() {
        Log.e("Inside", "LogAllForums");
        if (forumDao.count() > 0) {
            List<Forum> dbForums = forumDao.queryBuilder().orderDesc(ForumDao.Properties.Published) //TODO : Here Published was there
                    .listLazy();
            for (Forum f : dbForums)
                Ln.d(f.getTitle() + " " + f.getPublishedDate() + "\n");
        }
    }

    void LogLatestForumModifiedDate(Forum latest) {
        Date date = new Date(latest.getModifiedDate());
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Ln.d("Latest post available is " + latest.getTitle()
                + " modified on " + format.format(date) + " - " + latest.getModifiedDate());
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        Ln.e("setUserVisibleHunt");
        if (visible && getActivity() != null) {
            Toolbar toolbar;
            if (getActivity() instanceof MainActivity) {
                toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
                mSpinnerContainer.setVisibility(View.GONE);
            } else {
                toolbar = ((ForumListActivity) (getActivity())).getActionBarToolbar();
            }
            View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
            toolbar.removeView(view);
            toolbar.invalidate();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);
        }
    }
}
package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;
import java.net.UnknownHostException;
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
import in.testpress.testpress.core.PostCategoryPager;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import info.hoang8f.widget.FButton;

public class PostsListFragment extends Fragment implements
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, LoaderManager
        .LoaderCallbacks<List<Post>> {

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
    HeaderFooterListAdapter<PostsListAdapter> adapter;
    PostsPager refreshPager;
    PostsPager pager;
    View loadingLayout;
    PostDao postDao;
    CategoryDao categoryDao;
    DaoSession daoSession;
    LazyList<Post> posts;
    int lastFirstVisibleItem;
    boolean isScrollingUp;
    boolean isUserSwiped;
    private ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    private View mSpinnerContainer;
    private Boolean mFistTimeCallback = false;

    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom posts
    private static final int POSTS_LOADER_ID = 1;

    // Number of maximum posts which can be missed from the latest before the db will get reset
    private static final int MISSED_POSTS_THRESHOLD = 50;

    Long categoryFilter = null;
    boolean authorizationChecked;
    PostCategoryPager categoryPager;
    List<Category> categories = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getLong("category_filter") != 0) {
                categoryFilter = getArguments().getLong("category_filter");
            }
        }
        //Get the dao handles for posts and categories
        daoSession = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();
        postDao = daoSession.getPostDao();
        categoryDao = daoSession.getCategoryDao();
        //Enable options. This will trigger onCreateOptionsMenu
        setHasOptionsMenu(true);
        Injector.inject(this);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(),
                getActivity().getResources(), true);
        mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_posts), false, 0);
        mTopLevelSpinnerAdapter.addHeader(getString(R.string.categories));Toolbar toolbar;
        if (getActivity() instanceof MainActivity) {
            toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
//            mSpinnerContainer.setVisibility(View.GONE);
        } else {
            toolbar = ((PostsListActivity) (getActivity())).getActionBarToolbar();
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
        mSpinnerContainer.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh_list, null);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new HeaderFooterListAdapter<PostsListAdapter>(listView, new PostsListAdapter
                (getActivity(), R.layout.post_list_item));
        listView.setAdapter(adapter);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
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
                    mSpinnerContainer.setVisibility(View.VISIBLE);
                    Toolbar toolbar;
                    if (getActivity() instanceof MainActivity) {
                        toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
                        mSpinnerContainer.setVisibility(View.GONE);
                    } else {
                        toolbar = ((PostsListActivity) (getActivity())).getActionBarToolbar();
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
    public Loader<List<Post>> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case REFRESH_LOADER_ID:
                return new ThrowableLoader<List<Post>>(getActivity(), null) {
                    @Override
                    public List<Post> loadData() throws IOException {
                        if (refreshPager == null) {
                            initRefreshPager();
                        }
                        refreshPager.next();
                        return refreshPager.getResources();
                    }
                };
            case POSTS_LOADER_ID:
                return new ThrowableLoader<List<Post>>(getActivity(), null) {
                    @Override
                    public List<Post> loadData() throws IOException {
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
        if (refreshPager == null) {
            refreshPager = new PostsPager(getTestpressService(), postDao);
            refreshPager.setQueryParams("order", "-published_date");
            if (postDao.count() > 0) {
                Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.ModifiedDate)
                        .list().get(0);
                refreshPager.setLatestModifiedDate(latest.getModified());
                LogLatestPostModifiedDate(latest);
                LogAllPosts();
            }
        }
    }

    void initOldPostLoadingPager() {
        pager = new PostsPager(getTestpressService(), null);
        pager.setQueryParams("order", "-published_date");
        Post lastPost = postDao.queryBuilder().orderDesc(PostDao.Properties
                .Published).list().get((int) postDao.count() - 1);
        pager.setQueryParams("until", lastPost.getPublishedDate());
        pager.setLatestModifiedDate(null);
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
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

    void onRefreshLoadFinished(List<Post> items) {
        //If no data is available in the local database, directly insert
        //display from database
        Ln.e(swipeLayout.isRefreshing());
        if ((postDao.count() == 0) || items == null || items.isEmpty()) {

            //Remove the swipe refresh icon and the sticky notification if any
            swipeLayout.setRefreshing(false);
            mStickyView.setVisibility(View.GONE);

            //Return if no new posts are available
            if (items == null || items.isEmpty()) {
                displayDataFromDB();
                if (postDao.count() == 0) {
                    setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.ic_error_outline_black_18dp);
                    retryButton.setVisibility(View.GONE);
                }
                return;
            }
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            //Insert the categories and posts to the database
            writeToDB(items);

            //Trigger displaying data
            displayDataFromDB();
        } else {
            //If data is already available in the local database, then
            //notify user about the new data to view latest data.
            Ln.d(MISSED_POSTS_THRESHOLD >= refreshPager.getTotalCount());
            if (MISSED_POSTS_THRESHOLD >= refreshPager.getTotalCount()) {
                if (refreshPager.hasNext()) {
                    refreshPager.clearQueryParams();
                    getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment
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

    void onNetworkLoadFinished(List<Post> items) {

        if (pager != null && !pager.hasMore()) {
            if (adapter.getFootersCount() != 0) {  //if pager reached last page remove footer if
                // footer added already
                adapter.removeFooter(loadingLayout);
            }
        }
        //Return if no new posts are available
        if (items.isEmpty())
            return;

        //Insert posts to the database
        writeToDB(items);

        //Trigger displaying data
        displayDataFromDB();
    }

    //This just notifies the adapter that new data is now available in db
    void displayDataFromDB() {
        Ln.d("Adapter notifyDataSetChanged displayDataFromDB");
        adapter.notifyDataSetChanged();

        if (postDao.count() == 0 || (pager != null && !pager.hasMore() && adapter.getCount() == 0)) {
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

        if (listView != null && (postDao.count() != 0) && !isScrollingUp &&
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
            getLoaderManager().restartLoader(POSTS_LOADER_ID, null, PostsListFragment.this);
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
                if (postDao.count() > 0) {
                    Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties
                            .ModifiedDate).list().get(0);
                    refreshPager.setLatestModifiedDate(latest.getModified());
                    LogLatestPostModifiedDate(latest);
                }
                getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
                categories.clear();
                categoryPager.clear();
                fetchCategories();
            }
        });
    }

    @OnClick(R.id.sticky)
    public void displayNewPosts() {
        Ln.d("Sticky Clicked");
        mStickyView.setVisibility(View.GONE);
        //Remove the swipe refresh icon if present
        swipeLayout.setRefreshing(false);
        Ln.d(MISSED_POSTS_THRESHOLD < refreshPager.getTotalCount());
        if (MISSED_POSTS_THRESHOLD < refreshPager.getTotalCount()) {
            clearDB();
            onRefresh();
        } else {
            //Insert posts to the database
            writeToDB(refreshPager.getResources());
        }
        //Trigger displaying data
        displayDataFromDB();
    }

    protected void writeToDB(List<Post> posts) {
        //Insert the categories and posts to the database
        List<Category> categories = new ArrayList<Category>();
        for (Post post : posts) {
            if (post.category != null) {
                Ln.e("Post category for " + post.getTitle() + " is " + post.category.getName());
                categories.add(post.category);
            } else {
                Ln.e("Post category for " + post.getTitle() + " is null");
            }
        }
        categoryDao.insertOrReplaceInTx(categories);
        postDao.insertOrReplaceInTx(posts);
        LogAllPosts();
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
        if (posts != null) {
            posts.close();
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

    protected Exception getException(final Loader<List<Post>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Post>>) loader).clearException();
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
        postDao.deleteAll();
        refreshPager.removeQueryParams("since");
        pager = null;
        displayDataFromDB();
    }

    void LogAllPosts() {
        if (postDao.count() > 0) {
            List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.Published)
                    .listLazy();
            for (Post p : dbPosts)
                Ln.d(p.getTitle() + " " + p.getPublishedDate() + "\n");
        }
    }

    void LogLatestPostModifiedDate(Post latest) {
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
                toolbar = ((PostsListActivity) (getActivity())).getActionBarToolbar();
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
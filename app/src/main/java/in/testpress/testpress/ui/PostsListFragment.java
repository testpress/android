package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.dao.query.LazyList;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

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
    @InjectView(R.id.empty)
    TextView emptyView;
    @InjectView(R.id.sticky)
    TextView mStickyView;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().getLong("category_filter") != 0) {
            categoryFilter = getArguments().getLong("category_filter");
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
        mTopLevelSpinnerAdapter.addHeader(getString(R.string.categories));
        Toolbar toolbar = ((PostsListActivity) (getActivity())).getActionBarToolbar();
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
        mSpinnerContainer.setVisibility(View.VISIBLE);
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
        listView.setDividerHeight(0);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
        getLoaderManager().initLoader(REFRESH_LOADER_ID, null, this);
        fetchCategories();
        if (categoryFilter != null) {
            adapter.getWrappedAdapter().setCategoryFilter(categoryFilter);
        }
        displayDataFromDB();
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
    }

    public void fetchCategories() {
        new SafeAsyncTask<List<Category>>() {
            @Override
            public List<Category> call() throws Exception {
                AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
                final Account[] account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                if (account.length > 0) {
                    try {
                        testpressService = serviceProvider.getService(getActivity());
                    } catch (AccountsException e) {
                    } catch (IOException e) {
                    }
                }
                return testpressService
                        .getCategories(Constants.Http.URL_CATEGORIES_FRAG, null).getResults();
            }

            protected void onSuccess(final List<Category> categories) throws Exception {
                Ln.e("On success");

                for (final Category category : categories) {
                    mTopLevelSpinnerAdapter.addItem("" + category.getId(), category.getName(), true,
                            Color.parseColor("#" + category.getColor()));
                }

                if ((mSpinnerContainer.getVisibility() == View.GONE)) {
                    Ln.e("Setting visible");
                    mSpinnerContainer.setVisibility(View.VISIBLE);
                }
                mTopLevelSpinnerAdapter.notifyDataSetChanged();

                if (categoryFilter != null) {
                    Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
                    spinner.setSelection(mTopLevelSpinnerAdapter.getItemPositionFromTag(categoryFilter.toString()));
                }

                Toolbar toolbar = ((PostsListActivity)(getActivity())).getActionBarToolbar();
                View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
                toolbar.removeView(view);
                toolbar.invalidate();
                ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                toolbar.addView(mSpinnerContainer, lp);
            }

        }.execute();
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case REFRESH_LOADER_ID:
                return new ThrowableLoader<List<Post>>(getActivity(), null) {
                    @Override
                    public List<Post> loadData() throws IOException {
                        if (refreshPager == null) {
                            initPager();
                        }
                        refreshPager.next();
                        return refreshPager.getResources();
                    }
                };
            case POSTS_LOADER_ID:
                return new ThrowableLoader<List<Post>>(getActivity(), null) {
                    @Override
                    public List<Post> loadData() throws IOException {
                        pager.next();
                        return pager.getResources();
                    }
                };
            default:
                //An invalid id was passed
                return null;
        }
    }

    void initPager() {
        if (refreshPager == null) {
            AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
            final Account[] account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
            if (account.length > 0) {
                try {
                    testpressService = serviceProvider.getService(getActivity());
                } catch (AccountsException e) {
                } catch (IOException e) {
                }
            }
            refreshPager = new PostsPager(testpressService, getContext());
            refreshPager.setQueryParams("order", "-created");
            if (postDao.count() > 0) {
                Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate)
                        .list().get(0);
                refreshPager.setQueryParams("gt", latest.getCreated());
                LogLatestPostCreatedDate(latest);
                LogAllPosts();
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
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
            showError(getErrorMessage(exception));
            if (adapter.getCount() == 0) {
                emptyView.setText(getErrorMessage(exception));
            }
            displayDataFromDB();
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
        if ((postDao.count() == 0) || items.isEmpty()) {

            //Remove the swipe refresh icon and the sticky notification if any
            swipeLayout.setRefreshing(false);
            mStickyView.setVisibility(View.GONE);

            //Return if no new posts are available
            if (items.isEmpty()) {
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

        if (!pager.hasMore()) {
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
        if (adapter.getCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No Articles");
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
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

        if (listView != null && (postDao.count() != 0) &&
                !isScrollingUp && adapter.getWrappedAdapter().getCount() > 3
                && (listView.getLastVisiblePosition() + 3) >= adapter.getWrappedAdapter().getCount()) {

            Ln.d("Onscroll showing more");

            if (pager == null) {
                pager = new PostsPager(testpressService, getContext());
                pager.setQueryParams("order", "-created");
                Post lastPost = postDao.queryBuilder().orderDesc(PostDao.Properties
                        .CreatedDate).list().get((int) postDao.count() - 1);
                pager.setQueryParams("lt", lastPost.getCreated());
                Ln.d("Latest post available is " + lastPost.getTitle() + lastPost.getCreated());
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
                isUserSwiped = true;
                refreshPager.clear();
                if (postDao.count() > 0) {
                    Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties
                            .CreatedDate).list().get(0);
                    refreshPager.setQueryParams("order", "-created");
                    refreshPager.setQueryParams("gt", latest.getCreated());
                    LogLatestPostCreatedDate(latest);
                }
                getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
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
        }

        //Insert posts to the database
        writeToDB(refreshPager.getResources());

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

    @OnItemClick(android.R.id.list)
    public void onListItemClick(int position) {
        Ln.d("Clicked " + position);
        Post post = adapter.getWrappedAdapter().getItem(position);
        Ln.d("Post at position is " + post.getTitle());
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("urlWithBase", post.getUrl());
        startActivity(intent);
    }

    protected int getErrorMessage(Exception exception) {
        if (exception.getCause() instanceof UnknownHostException) {
            emptyView.setText(R.string.no_internet);
            return R.string.no_internet;
        }
        return R.string.error_loading_posts;
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

    protected Exception getException(final Loader<List<Post>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Post>>) loader).clearException();
        } else {
            return null;
        }
    }

    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    void clearDB() {
        Ln.d("ClearDB");
        postDao.deleteAll();
        refreshPager.removeQueryParams("gt");
        pager = null;
        displayDataFromDB();
    }

    void LogAllPosts() {
        if (postDao.count() > 0) {
            List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate)
                    .listLazy();
            for (Post p : dbPosts)
                Ln.d(p.getTitle() + " " + p.getCreated() + "\n");
        }
    }

    void LogLatestPostCreatedDate(Post latest) {
        Date date = new Date(latest.getCreatedDate());
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        Ln.d("Latest post available is " + latest.getTitle()
                + " created on " + format.format(date) + " - " + latest.getCreated());
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        Ln.e("setUserVisibleHunt");
        if (visible && getActivity() != null) {
            Toolbar toolbar = ((PostsListActivity)(getActivity())).getActionBarToolbar();
            View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
            toolbar.removeView(view);
            toolbar.invalidate();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);
        }
    }
}

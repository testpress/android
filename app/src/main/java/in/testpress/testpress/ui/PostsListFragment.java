package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
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
import butterknife.OnItemClick;
import de.greenrobot.dao.query.LazyList;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.Ln;

public class PostsListFragment extends Fragment implements
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<Post>> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    @InjectView(android.R.id.list) ListView listView;
    @InjectView(R.id.empty) TextView emptyView;
    @InjectView(R.id.sticky) TextView mStickyView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeLayout;
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


    // Loader for refresh
    private static final int REFRESH_LOADER_ID = 0;

    // Loader to load bottom posts
    private static final int POSTS_LOADER_ID = 1;

    // Number of maximum posts which can be missed from the latest before the db will get reset
    private static final int MISSED_POSTS_THRESHOLD = 50;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the dao handles for posts and categories
        daoSession = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();
        postDao = daoSession.getPostDao();
        categoryDao = daoSession.getCategoryDao();
        //Enable options. This will trigger onCreateOptionsMenu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_refresh_list, null);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        try {
            refreshPager = new PostsPager(serviceProvider.getService(getActivity()), getContext());
            refreshPager.setQueryParams("order", "-created");
            if (postDao.count() > 0) {
                Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list().get(0);
                refreshPager.setQueryParams("gt", latest.getCreated());
                LogLatestPostCreatedDate(latest);
                LogAllPosts();
            } else {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("No Articles");
                listView.setVisibility(View.GONE);
            }
        } catch (AccountsException e) {
            //TODO handle this
            e.printStackTrace();
        } catch (IOException e) {
            //TODO handle this
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new HeaderFooterListAdapter<PostsListAdapter>(listView, new PostsListAdapter(getActivity(), R.layout.post_list_item));
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
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
        displayDataFromDB();
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case REFRESH_LOADER_ID:
                return new ThrowableLoader<List<Post>>(getActivity(), null) {
                    @Override
                    public List<Post> loadData() throws IOException {
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

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
        final Exception exception = getException(loader);
        if (exception != null) {
            //Remove the swipe refresh icon and the sticky notification if any
            swipeLayout.setRefreshing(false);
            if (pager != null && !pager.hasMore()) {
                if(adapter.getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
                    adapter.removeFooter(loadingLayout);
                }
            }
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
                if(refreshPager.hasNext()) {
                    refreshPager.clearQueryParams();
                    getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
                    return;
                }
            }
            if(isUserSwiped || (lastFirstVisibleItem == 0)) {
                displayNewPosts();
            } else {
                mStickyView.setVisibility(View.VISIBLE);
            }
        }
    }

    void onNetworkLoadFinished(List<Post> items) {

        if (!pager.hasMore()) {
            if(adapter.getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
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
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // If the ListView is the only one child of the SwipeRefreshLayout we wouldn’t have any
        // kind of problems, because everything works smoothly. In some cases we have not only the
        // ListView but we have other elements. This case is a little bit more complex, because if
        // we scroll up the items in the ListView everything works as expected, but if the scroll
        // down the refresh process starts and the list items doesn’t scroll as we want. In this
        // case we can use a trick, we can disable the refresh notification using setEnabled(false)
        // and enable it again as soon as the first item in the ListView is visible.
        // Here we override the onScrollListener of the ListView to handle the enable/disable mechanism
        // See more at: http://www.survivingwithandroid.com/2014/05/android-swiperefreshlayout-tutorial.html
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }
        if (getActivity() == null)
            return;

        if (getLoaderManager().hasRunningLoaders())
            return;

        if (listView != null && (postDao.count() != 0) && !isScrollingUp
                && (listView.getLastVisiblePosition() + 3) >= postDao.count()) {

            Ln.d("Onscroll showing more");

            if (pager == null) {
                try {
                    pager = new PostsPager(serviceProvider.getService(getActivity()), getContext());
                    pager.setQueryParams("order", "-created");
                    Post lastPost = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list().get((int) postDao.count() - 1);
                    pager.setQueryParams("lt", lastPost.getCreated());
                    Ln.d("Latest post available is " + lastPost.getTitle() + lastPost.getCreated());
                    if(adapter.getFootersCount() == 0) { //display loading footer if not present when loading next page
                        adapter.addFooter(loadingLayout);
                    }
                } catch (AccountsException e) {
                    //TODO handle this
                    e.printStackTrace();
                } catch (IOException e) {
                    //TODO handle this
                    e.printStackTrace();
                }
            } else {
                if (!pager.hasMore()) {
                    if(adapter.getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
                        adapter.removeFooter(loadingLayout);
                    }
                    return;
                }
                pager.clearQueryParams();
            }
            getLoaderManager().restartLoader(POSTS_LOADER_ID, null, PostsListFragment.this);
        }
    }

    @Override public void onRefresh() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                isUserSwiped = true;
                refreshPager.clear();
                if (postDao.count() > 0) {
                    Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list().get(0);
                    refreshPager.setQueryParams("order", "-created");
                    refreshPager.setQueryParams("gt", latest.getCreated());
                    LogLatestPostCreatedDate(latest);
                }
                getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
            }
        });
    }

    @OnClick(R.id.sticky) public void displayNewPosts() {
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
            categories.add(post.category);
        }
        categoryDao.insertOrReplaceInTx(categories);
        postDao.insertOrReplaceInTx(posts);
        LogAllPosts();
    }
    
    @OnItemClick(android.R.id.list) public void onListItemClick(int position) {
        Ln.d("Clicked " + position);
        Post post = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position);
        Ln.d("Post at position is " + post.getTitle());
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("urlWithBase", post.getUrl());
        startActivity(intent);
    }

    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.handleForbidden(getActivity(), serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else if (exception.getCause() instanceof UnknownHostException) {
            emptyView.setText(R.string.no_internet);
            return R.string.no_internet;
        }
        return R.string.error_loading_posts;
    }

    @Override
    public void onStop () {
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
            List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy();
            for (Post p :  dbPosts)
                Ln.d(p.getTitle() + " " + p.getCreated() + "\n");
        }
    }

    void LogLatestPostCreatedDate(Post latest) {
        Date date = new Date(latest.getCreatedDate());
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        Ln.d("Latest post available is " + latest.getTitle()
                + " created on " + format.format(date) + " - " + latest.getCreated());
    }
}

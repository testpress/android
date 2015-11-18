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
    PostDao postDao;
    CategoryDao categoryDao;
    DaoSession daoSession;
    LazyList<Post> posts;
    int lastFirstVisibleItem;
    boolean isScrollingUp;

    @InjectView(android.R.id.list) ListView listView;
    @InjectView(R.id.empty) TextView emptyView;
    @InjectView(R.id.sticky) TextView mStickyView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeLayout;
    HeaderFooterListAdapter<PostsListAdapter> adapter;
    PostsPager refreshPager;
    PostsPager pager;
    View loadingLayout;

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
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        Ln.e("onCreateOptionsMenu");
        inflater.inflate(R.menu.posts_list, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                Ln.e("Clearing DB");
                clearDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                //just for log, have to remove****
                Date date = new Date(latest.getCreatedDate());
                Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
                Ln.e("Latest post available is " + latest.getTitle()
                        + " created on " + format.format(date) + " - " + latest.getCreated());

                List<Post> items = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy();
                for (Post p :  items)
                    Ln.e(p.getTitle() + " " + p.getCreated() + "\n");
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
        ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.list_divider));
        listView.setDivider(sage);
        listView.setDividerHeight(1);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If the ListView is the only one child of the SwipeRefreshLayout we wouldn’t have any
        // kind of problems, because everything works smoothly. In some cases we have not only the
        // ListView but we have other elements. This case is a little bit more complex, because if
        // we scroll up the items in the ListView everything works as expected, but if the scroll
        // down the refresh process starts and the list items doesn’t scroll as we want. In this
        // case we can use a trick, we can disable the refresh notification using setEnabled(false)
        // and enable it again as soon as the first item in the ListView is visible.
        // See more at: http://www.survivingwithandroid.com/2014/05/android-swiperefreshlayout-tutorial.html
//        swipeLayout.setEnabled(false);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.primary,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_green_dark);
        swipeLayout.setEnabled(true);
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
            //TODO Handle error properly
            return;
        }

        switch (loader.getId()) {
            case REFRESH_LOADER_ID:
                onRefreshItemsLoaded(data);
                break;
            case POSTS_LOADER_ID:
                onNetworkLoadFinished(data);
                break;
            default:
                return;
        }
    }

    void onRefreshItemsLoaded(List<Post> items) {
        //If no data is available in the local database, directly insert
        //display from database
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
            List<Category> categories = new ArrayList<Category>();
            for (Post item : items) {
                categories.add(item.category);
                item.setCategory(item.category);
            }
            categoryDao.insertOrReplaceInTx(categories);
            postDao.insertOrReplaceInTx(items);

            //Trigger displaying data
            displayDataFromDB();

            //just for log, have to remove****
            if (postDao.count() > 0) {
                List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy();
                for (Post p :  dbPosts)
                    Ln.e(p.getTitle() + " " + p.getCreated() + "\n");
            }
        } else {
            //If data is already available in the local database, then
            //notify user about the new data to view latest data.
            Ln.e(MISSED_POSTS_THRESHOLD >= refreshPager.getTotalItemsWantTOLoad());
            Ln.e(MISSED_POSTS_THRESHOLD);
            Ln.e(refreshPager.getTotalItemsWantTOLoad());
            if (MISSED_POSTS_THRESHOLD >= refreshPager.getTotalItemsWantTOLoad()) {
                Ln.e(refreshPager.hasNext());
                if(refreshPager.hasNext()) {
                    refreshPager.clearQueryParams();
                    getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
                    return;
                }
            }
            mStickyView.setVisibility(View.VISIBLE);
            swipeLayout.setRefreshing(false);
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

        //Insert the categories and posts to the database
        List<Category> categories = new ArrayList<Category>();
        for (Post item : items) {
            categories.add(item.category);
            item.setCategory(item.category);
        }
        categoryDao.insertOrReplaceInTx(categories);
        postDao.insertOrReplaceInTx(items);

        //Trigger displaying data
        displayDataFromDB();

        //just for log, have to remove****
        if (postDao.count() > 0) {
            List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy();
            for (Post p :  dbPosts)
                Ln.e(p.getTitle() + " " + p.getCreated() + "\n");

        }
    }

    //This just notifies the adapter that new data is now available in db
    void displayDataFromDB() {
        Ln.e("Adapter notifyDataSetChanged displayDataFromDB");
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
        //TODO Handle pagination
        if (getActivity() == null)
            return;

        if (getLoaderManager().hasRunningLoaders())
            return;

        if (listView != null && (postDao.count() != 0) && !isScrollingUp
                && (listView.getLastVisiblePosition() + 3) >= postDao.count()) {

            Ln.e("Onscroll showing more");

            if (pager == null) {
                try {
                    pager = new PostsPager(serviceProvider.getService(getActivity()), getContext());
                    pager.setQueryParams("order", "-created");
                    Post lastPost = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list().get((int) postDao.count() - 1);
                    pager.setQueryParams("lt", lastPost.getCreated());
                    Ln.e("Latest post available is " + lastPost.getTitle() + lastPost.getCreated());
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
        //if stickyView is visible instead check for new post call onStickyClick
        if (mStickyView.getVisibility() == View.VISIBLE) {
            onStickyClick();
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    refreshPager.clear();
                    if (postDao.count() > 0) {
                        Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list().get(0);
                        refreshPager.setQueryParams("order", "-created");
                        refreshPager.setQueryParams("gt", latest.getCreated());
                        //just for log, have to remove****
                        Date date = new Date(latest.getCreatedDate());
                        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
                        Ln.e("Latest post available is " + latest.getTitle()
                                + " created on " + format.format(date) + " - " + latest.getCreated());

                    }
                    getLoaderManager().restartLoader(REFRESH_LOADER_ID, null, PostsListFragment.this);
                }
            });
        }
    }

    @OnClick(R.id.sticky) public void onStickyClick() {
        //TODO If total count of posts available is > than MISSED_POSTS_THRESHOLD then we need
        //TODO clear the old posts in db
        Ln.e("Sticky Clicked");
        mStickyView.setVisibility(View.GONE);
        //Remove the swipe refresh icon if present
        swipeLayout.setRefreshing(false);
        Ln.e(MISSED_POSTS_THRESHOLD < refreshPager.getTotalItemsWantTOLoad());
        Ln.e(MISSED_POSTS_THRESHOLD);
        Ln.e(refreshPager.getTotalItemsWantTOLoad());
        if (MISSED_POSTS_THRESHOLD < refreshPager.getTotalItemsWantTOLoad()) {
            clearDB();
        }
        List<Category> categories = new ArrayList<Category>();
        List<Post> items = refreshPager.getResources();
        for (Post item : items) {
            categories.add(item.category);
            item.setCategory(item.category);
        }
        categoryDao.insertOrReplaceInTx(categories);
        postDao.insertOrReplaceInTx(items);
        displayDataFromDB();

        //just for log, have to remove****
        if (postDao.count() > 0) {
            List<Post> dbPosts = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy();
            for (Post p :  dbPosts)
                Ln.e(p.getTitle() + " " + p.getCreated() + "\n");
        }
    }

    @OnItemClick(android.R.id.list) public void onListItemClick(int position) {
        Ln.e("Clicked " + position);
        Post post = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position);
        Ln.e("Post at position is " + post.getTitle());
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
        Ln.e("ClearDB");
        postDao.deleteAll();
//        DBSessionDao dbSessionDao = daoSession.getDBSessionDao();
//        dbSessionDao.deleteAll();
        daoSession.clear();
        Ln.e("Query params " + refreshPager.queryParams);
        refreshPager.removeQueryParams("gt");
        Ln.e("Query params " + refreshPager.queryParams);
        pager = null;
        displayDataFromDB();
    }
}

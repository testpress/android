package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.QueryBuilder;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.PostsPager;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.DBSession;
import in.testpress.testpress.models.DBSessionDao;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.DBSessionManager;

public class PostsListFragment extends ItemListFragment<Post>
        implements AbsListView.OnScrollListener {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    PostsPager pager, newPager;
    PostDao postDao;
    DBSessionDao sessionDao;
    DBSessionManager dbSessionManager;
    DBSession newSession, currentSession;
    LazyList<Post> posts;
    View headerLayout, footerLayout;
    QueryBuilder<Post> queryBuilder;
    String latestPost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        ButterKnife.inject(this.getActivity());
        try {
            pager = new PostsPager(serviceProvider.getService(getActivity()), getContext());
            pager.setQueryParams("order","-created");
        } catch (AccountsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);

        postDao = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession().getPostDao();
        sessionDao = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession().getDBSessionDao();
        footerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
        dbSessionManager = new DBSessionManager(getContext());
    }

    void init() {
        if(sessionDao.count() == 0) {
            //create first session when no DB exist
            currentSession = dbSessionManager.getNewSession();
            getLoaderManager().initLoader(0, null, this);

        } else {

            //show posts from db
              //get the latest session
            currentSession = dbSessionManager.getLatestSession();

            //latestPost(latest session's latest post) - used while pagination if internet unavailable moving from one session to another session while scrolling
            // to display the posts of both session , if internet available there is no need of another session
            //because current session will load completely & merge to existing session & so on thereby only one session will display
            latestPost = currentSession.getLatestPostReceived();

            //if current session is completed & has previous session then merge to it
            dbSessionManager.merge(currentSession);

            displayDataFromDB();

            //get the argument from activity if activity called by notification create new session & do network request
            boolean parentIsNotification = getArguments().getBoolean("parentIsNotification");

            //check network request is needed or not
            Calendar rightNow = Calendar.getInstance();
            if(parentIsNotification && (((rightNow.getTimeInMillis() - currentSession.getCreated()) / (60 * 1000)) > 1)) {  //check for new posts

                //show progress in header
                headerLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
                ((TextView) headerLayout.findViewById(R.id.loadingText)).setText("Checking for new Articles...");
                getListAdapter().addHeader(headerLayout);

                //create new session
                newSession = dbSessionManager.getNewSession();

                //get another pager
                try {
                    newPager = new PostsPager(serviceProvider.getService(getActivity()), getContext());
                    newPager.setQueryParams("order","-created");
                } catch (AccountsException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //request post greater than latest post we have
                newPager.setQueryParams("gt", currentSession.getLatestPostReceived());

                //init loader with id 1 - this loader is used to load new posts while entering the activity
                getLoaderManager().initLoader(1, null, this);

            }
        }
    }

    @Override
    public Loader<List<Post>> onCreateLoader(final int id, Bundle bundle) {
        return new ThrowableLoader<List<Post>>(getActivity(), items) {

            @Override
            public List<Post> loadData() throws IOException {
                if(id == 0) {
                    pager.next();
                    return pager.getResources();
                } else {
                    newPager.next();
                    return newPager.getResources();
                }
            }
        };
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Don't take any action on changed
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_posts);
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(this);
        getListView().setFastScrollEnabled(true);
        init();
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {

        super.configureList(activity, listView);
        ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.list_divider));
        listView.setDivider(sage);
        listView.setDividerHeight(1);
        getListAdapter().addFooter(footerLayout);
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> items) {
        Ln.e("onLoadFinished " + items.size());

        getActivity().setProgressBarIndeterminateVisibility(false);

        final Exception exception = getException(loader);
        if (exception != null) {
            showError(getErrorMessage(exception));
            showList();
            return;
        }

        //check the caller of loader using id
        if(loader.getId() == 1) { //loader called by new post checking

            if(items.isEmpty()) {

                //if no posts remove header
                if(getListAdapter().getHeadersCount() != 0) {
                    getListAdapter().removeHeader(headerLayout);
                }

                //update session created time for next time delay
                currentSession.setCreated(newSession.getCreated());
                return;
            }

            //if items present update current session
            dbSessionManager.updateSession(newSession, newPager, items);

            //modify header
            if(getListAdapter().getHeadersCount() != 0) {
                ((TextView) headerLayout.findViewById(R.id.loadingText)).setText("New Articles available click to get them");
                ((ProgressBar) headerLayout.findViewById(R.id.progressbar)).setVisibility(View.GONE);
                ((TextView) headerLayout.findViewById(R.id.loadingText)).setBackgroundColor(Color.RED);
                ((TextView) headerLayout.findViewById(R.id.loadingText)).setTextColor(Color.WHITE);
                ((TextView) headerLayout.findViewById(R.id.loadingText)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        refreshWithProgress(); //if user click header refresh
                    }
                });
            }

        } else {  //loader called by pagination or when no db exist

            if (items.isEmpty()) {

                if(sessionDao.count() == 0) { //if no db exist

                    //if no items are returned call super it will handle
                    setEmptyText(R.string.no_posts);
                    super.onLoadFinished(loader, items);
                    return;
                }

                if (dbSessionManager.getPreviousSession(currentSession) != null) {  //check whether previous session is available

                    //get the previous session
                    currentSession = dbSessionManager.getPreviousSession(currentSession);
                    displayDataFromDB();

                } else {  //reached end of whole data in db
                    getListAdapter().removeFooter(footerLayout);
                }

            }

            //if items present update current session
            dbSessionManager.updateSession(currentSession, pager, items);

            //if current session is completed & has previous session then merge to it
            dbSessionManager.merge(currentSession);

            if(latestPost == null) { //when no db exist state have to assign latestPost
                latestPost = currentSession.getLatestPostReceived();
            }

            //display items
            displayDataFromDB();
        }
    }



    protected void displayDataFromDB() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Ln.e(currentSession.getOldestPostReceived());
        try {

            //query posts between latest to oldest post
            //here the latest post is the latest session's latest post
            //in session we stored date in above format but to order the posts from DB, post contain another field called CreatedDate(string converted to Long)
            //so convert the string to long to query
            queryBuilder = postDao.queryBuilder().where(
                    PostDao.Properties.CreatedDate.between(
                            simpleDateFormat.parse(currentSession.getOldestPostReceived()).getTime(),
                            simpleDateFormat.parse(currentSession.getLatestPostReceived()).getTime()));

        } catch (ParseException e) {
            Ln.e("QueryBuilder parseException " + e);
        }

        int offset;
        if(posts == null) {
            offset = 0;
        } else {
            offset = posts.size();
        }
        posts = queryBuilder.orderDesc(PostDao.Properties.CreatedDate).limit(20 + offset).listLazy();
        Ln.e("Posts is " + posts);
        Ln.e("Length of posts" + posts.size());

        //assign posts to items because showList() of super will check items.isEmpty()
        items = posts;

        //assign items to adapter
        getListAdapter().getWrappedAdapter().setItems(posts.toArray());

        //while refresh show only the top most post otherwise in some scenario(refresh from page > 1) until we touch the screen
        // onScroll will not call automatically to get the next page & loading footer will ideally display
        Ln.e(posts.toArray().length);
        if(posts.size() == 20) {
            getListView().setSelection(0);
        }

        //hide progress & show list
        showList();
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {

        if (getLoaderManager().hasRunningLoaders())
            return;

        if ((!items.isEmpty()) && listView != null && (listView.getLastVisiblePosition() + 3) >= posts.size()) {

            if(!pager.hasMore()) {
            //internet available means it loaded all data from server otherwise data loading from db

                nextPage();

            } else {

                refresh(); //restart loader to load next page from server
            }
        }
    }

    void nextPage() {

        if(queryBuilder.count() == posts.size()) {
        //reached end of current session -:if internet avail topmost end is reached else while loaded using db partial session reached end

            if(currentSession.getState().equals("partial")) {

                pager.reset();
                pager.setQueryParams("lt", currentSession.getOldestPostReceived()); //where we left the loading previously
                pager.setQueryParams("gt", currentSession.getLastSyncedDate());   //upTo where we have
                refresh(); //restart loader

            } else {  //reached end of whole data in server itself

                getListAdapter().removeFooter(footerLayout);
            }

        } else {  //load next page from DB

            displayDataFromDB();
        }
    }

    @Override
    protected void refreshWithProgress() {

        if(posts != null) {
            posts.close();
        }
        pager.reset();
        setListShown(false);
        //remove the header if present
        if(getListAdapter().getHeadersCount() != 0) {
            getListAdapter().removeHeader(headerLayout);
        }
        init();
    }

    @Override
    protected SingleTypeAdapter<Post> createAdapter(List<Post> items) {
        return new PostsListAdapter(getActivity().getLayoutInflater(), items, R.layout.post_list_inner_content);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Post post = ((Post) l.getItemAtPosition(position));
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("urlWithBase", post.getUrl());
        startActivity(intent);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.handleForbidden(getActivity(), serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.no_internet);
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
        setListAdapter(null);

        super.onDestroyView();
    }

}

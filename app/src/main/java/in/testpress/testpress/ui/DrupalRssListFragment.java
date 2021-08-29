package in.testpress.testpress.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import in.testpress.core.TestpressException;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.RssItem;
import in.testpress.testpress.models.RssItemDao;
import in.testpress.ui.BaseListViewFragment;
import in.testpress.util.SingleTypeAdapter;
import in.testpress.util.ThrowableLoader;

public class DrupalRssListFragment extends BaseListViewFragment<RssItem> {

    public static final String RSS_FEED_URL = "rssFeedUrl";

    @Inject
    protected TestpressService testpressService;
    private RssItemDao rssFeedDao;

    public static void show(FragmentActivity activity, int containerViewId) {
        activity.getSupportFragmentManager().beginTransaction()
                .replace(containerViewId, new DrupalRssListFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        rssFeedDao = TestpressApplication.getDaoSession().getRssItemDao();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListAdapter().notifyDataSetChanged();
    }

    protected void displayDataFromDB() {
        getListAdapter().notifyDataSetChanged();

        if (isItemsEmpty()) {
            setEmptyText();
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isItemsEmpty() {
        return rssFeedDao.count() == 0;
    }

    @NonNull
    @Override
    public Loader<List<RssItem>> onCreateLoader(int id, Bundle args) {
        return new RssItemsLoader(this, items);
    }

    private static class RssItemsLoader extends ThrowableLoader<List<RssItem>> {

        private DrupalRssListFragment fragment;

        RssItemsLoader(DrupalRssListFragment fragment, List<RssItem> data) {
            //noinspection ConstantConditions
            super(fragment.getContext(), data);
            this.fragment = fragment;
        }

        @Override
        public List<RssItem> loadData() throws TestpressException {
            try {
                assert fragment.getArguments() != null;
                return fragment.testpressService
                        .getRssFeed(fragment.getArguments().getString(RSS_FEED_URL)).getItems();

            } catch (Exception e) {
                e.printStackTrace();
                if (e.getCause() instanceof IOException) {
                    throw TestpressException.networkError((IOException) e.getCause());
                } else {
                    throw TestpressException.unexpectedError(e);
                }
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<List<RssItem>> loader, List<RssItem> items) {
        final TestpressException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = items;
        if (!items.isEmpty()) {
            rssFeedDao.insertOrReplaceInTx(items);
        }
        displayDataFromDB();
        showList();
    }
    
    @Override
    protected SingleTypeAdapter<RssItem> createAdapter(List<RssItem> items) {
        //noinspection ConstantConditions
        return new DrupalRssListAdapter(getActivity(), rssFeedDao);
    }

    @Override
    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            Log.e("sssss", "isNetworkError");
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_courses,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    protected void setEmptyText() {
        setEmptyText(R.string.no_posts, R.string.no_posts_description,
                    R.drawable.ic_error_outline_black_18dp);
    }

}

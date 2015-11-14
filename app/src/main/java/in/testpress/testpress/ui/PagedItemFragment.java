package in.testpress.testpress.ui;


import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;


import java.io.IOException;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.util.Ln;

public abstract class PagedItemFragment<E> extends ItemListFragment<E>
        implements AbsListView.OnScrollListener {

    /**
     * Resource pager
     */
    protected ResourcePager<E> pager;

    /**
     * Create pager that provides resources
     *
     * @return pager
     */
    protected abstract ResourcePager<E> getPager();
    View loadingLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pager = getPager();
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(this);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount, int totalItemCount)
    {
        Ln.e("onScroll Called firstVisibleItem = " + firstVisibleItem);
        Ln.e("onScroll Called visibleItemCount = " + visibleItemCount);
        Ln.e("onScroll Called totalItemCount = " + totalItemCount);
        // Triggered only when new data needs to be appended to the list
        // Add whatever code ixs needed to append new items to your AdapterView
        //customLoadMoreDataFromApi(page);
        // or customLoadMoreDataFromApi(totalItemsCount);
        if (!isUsable())
            return;
        if (!pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
            return;
        }
        if (getLoaderManager().hasRunningLoaders())
            return;
        Ln.e("Pager size is " + pager.size());
        if (listView != null
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            if(getListAdapter().getFootersCount() == 0) { //display loading footer if not present when loading next page
                getListAdapter().addFooter(loadingLayout);
            }
            Ln.e("Onscroll showing more");
            showMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Don't take any action on changed
    }

    @Override
    public Loader<List<E>> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<List<E>>(getActivity(), items) {

            @Override
            public List<E> loadData() throws IOException {
                pager.next();
                return pager.getResources();
            }
        };
    }

    @Override
    protected void forceRefresh() {
        pager.clear();

        super.forceRefresh();
    }

    /**
     * Show more events while retaining the current pager state
     */
    public void showMore() {
        refresh();

    }

    @Override
    protected void refreshWithProgress() {
        pager.reset();
        pager = getPager();

        super.refreshWithProgress();
    }
}

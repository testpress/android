
package in.testpress.testpress.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Exam;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


/**
 * Base fragment for displaying a list of items that loads with a progress bar
 * visible
 *
 * @param <E>
 */
public abstract class ItemListFragmenthistory<E> extends Fragment
        implements AbsListView.OnScrollListener, LoaderManager.LoaderCallbacks<List<E>> {

    private static final String FORCE_REFRESH = "forceRefresh";


    /**
     * @param args bundle passed to the loader by the LoaderManager
     * @return true if the bundle indicates a requested forced refresh of the
     * items
     */
    protected static boolean isForceRefresh(final Bundle args) {
        return args != null && args.getBoolean(FORCE_REFRESH, false);
    }

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<E> items = Collections.emptyList();

    /**
     * List view
     */
    CardArrayAdapter mCardArrayAdapter;

    CardListView listView;
    protected ResourcePager<E> pager;
    protected abstract ResourcePager<E> getPager();

    Exam e;

    /**
     * Empty view
     */
    protected TextView emptyView;

    /**
     * Progress bar
     */
    protected ProgressBar progressBar;

    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!items.isEmpty()) {
            setListShown(true, false);
        }

        getLoaderManager().initLoader(0, null, this);

        getListView().setOnScrollListener(this);
        getListView().setFastScrollEnabled(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment_native_list_base, null);
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        listShown = false;
        emptyView = null;
        progressBar = null;
        listView = null;

        super.onDestroyView();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    listView = (CardListView) view.findViewById(R.id.carddemo_list_base1);

    progressBar = (ProgressBar) view.findViewById(id.pb_loading);

    emptyView = (TextView) view.findViewById(id.empty);

    configureList(getActivity(), getListView());

    }
    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(final Activity activity, final ListView listView) {
        listView.setAdapter(mCardArrayAdapter);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        pager = getPager();

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
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.testpress, optionsMenu);
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount)
    {
        // Triggered only when new data needs to be appended to the list
        // Add whatever code ixs needed to append new items to your AdapterView
        //customLoadMoreDataFromApi(page);
        // or customLoadMoreDataFromApi(totalItemsCount);
        if (!isUsable())
            return;
        if (!pager.hasMore())
            return;
        if (getLoaderManager().hasRunningLoaders())
            return;
        if (listView != null
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            showMore();
        }
    }


    private void showMore() {
        refresh();
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Don't take any action on changed
    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isUsable()) {
            return false;
        }
        switch (item.getItemId()) {
            case id.refresh:
                refreshWithProgress();
                return true;
            case id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract LogoutService getLogoutService();

    private void logout() {
        getLogoutService().logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                forceRefresh();
            }
        });
    }

    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        pager.clear();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        refresh(null);
    }

    private void refresh(final Bundle args) {
        if (!isUsable()) {
            return;
        }

        getActivity().setProgressBarIndeterminateVisibility(true);

        getLoaderManager().restartLoader(0, args, this);
    }

    /**
     * Get error message to display for exception
     *
     * @param exception
     * @return string resource id
     */
    protected abstract int getErrorMessage(final Exception exception);


    protected abstract List<Card> initCards();

    public void onLoadFinished(Loader<List<E>> loader, List<E> items) {

        getActivity().setProgressBarIndeterminateVisibility(false);

        final Exception exception = getException(loader);
        if (exception != null) {
            showError(getErrorMessage(exception));
            showList();
            return;
        }

        if(items != null) {
            this.items = items;
            mCardArrayAdapter=new CardArrayAdapter(getActivity(),initCards());
        }
        else {
            setEmptyText(R.string.no_internet);
        }
        showList();


        }

    /**
     * Create adapter to display items
     *
     * @return adapter
     */



    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */


  //  protected abstract SingleTypeAdapter<E> createAdapter(final List<E> items);

    /**
     * Set the list to be shown
     */
    protected void showList() {
        setListShown(true, isResumed());
    }

    @Override
    public void onLoaderReset(final Loader<List<E>> loader) {
        // Intentionally left blank
    }

    /**
     * Show exception in a Toast
     *
     * @param message
     */
    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<List<E>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<E>>) loader).clearException();
        } else {
            return null;
        }
    }

    /**
     * Refresh the list with the progress bar showing
     */
    protected void refreshWithProgress() {

        pager.reset();
        pager = getPager();


        items.clear();
        setListShown(false);
        refresh();
    }

    /**
     * Get {@link ListView}
     *
     * @return listView
     */
    public CardListView getListView() {
        return listView;
    }

    /**
     * Get list adapter
     *
     * @return list adapter
     */


    /**
     * Set list adapter to use on list view
     *
     * @param adapter
     * @return this fragment
     */
    protected ItemListFragmenthistory<E> setListAdapter(final ListAdapter adapter) {
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        return this;
    }

    private ItemListFragmenthistory<E> fadeIn(final View view, final boolean animate) {
        if (view != null) {
            if (animate) {
                view.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.fade_in));
            } else {
                view.clearAnimation();
            }
        }
        return this;
    }

    private ItemListFragmenthistory<E> show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private ItemListFragmenthistory<E> hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @return this fragment
     */
    public ItemListFragmenthistory<E> setListShown(final boolean shown) {
        return setListShown(shown, true);
    }

    /**
     * Set list shown or progress bar show
     *
     * @param shown
     * @param animate
     * @return this fragment
     */
    public ItemListFragmenthistory<E> setListShown(final boolean shown, final boolean animate) {
        if (!isUsable()) {
            return this;
        }

        if (shown == listShown) {
            if (shown) {
                // List has already been shown so hide/show the empty view with
                // no fade effect
                if (items.isEmpty()) {
                    hide(listView).show(emptyView);
                } else {
                    hide(emptyView).show(listView);
                }
            }
            return this;
        }

        listShown = shown;

        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).fadeIn(listView, animate)
                        .show(listView);
            } else {
                hide(progressBar).hide(listView).fadeIn(emptyView, animate)
                        .show(emptyView);
            }
        } else {
            hide(listView).hide(emptyView).fadeIn(progressBar, animate)
                    .show(progressBar);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param message
     * @return this fragment
     */
    protected ItemListFragmenthistory<E> setEmptyText(final String message) {
        if (emptyView != null) {
            emptyView.setText(message);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param resId
     * @return this fragment
     */
    protected ItemListFragmenthistory<E> setEmptyText(final int resId) {
        if (emptyView != null) {
            emptyView.setText(resId);
        }
        return this;
    }

    /**
     * Callback when a list view item is clicked
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */


    /**
     * Is this fragment still part of an activity and usable from the UI-thread?
     *
     * @return true if usable on the UI-thread, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }
}

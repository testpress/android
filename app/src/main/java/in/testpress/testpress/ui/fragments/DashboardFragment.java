package in.testpress.testpress.ui.fragments;

import android.accounts.AccountsException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.DashboardPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.DashboardSectionDao;
import in.testpress.testpress.ui.adapters.DashboardAdapter;
import in.testpress.testpress.ui.loaders.DashboardLoader;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;

import static in.testpress.util.ThrowableLoader.getException;

public class DashboardFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<DashboardSection>> {

    private ArrayList<String> sections = new ArrayList<>();
    @InjectView(R.id.recycler_View)
    RecyclerView recyclerView;
    @InjectView(R.id.empty_container)
    LinearLayout emptyView;
    @InjectView(R.id.empty_title)
    TextView emptyTitleView;
    @InjectView(R.id.empty_description)
    TextView emptyDescView;
    @InjectView(R.id.retry_button)
    Button retryButton;

    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    protected TestpressServiceProvider serviceProvider;
    private DashboardAdapter adapter;
    private TestpressService testpressService;
    private DashboardPager pager;
    private DashboardSectionDao dashboardSectionDao;
    private boolean firstCallback = true;
    private DaoSession daoSession;
    protected TestpressException exception;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        updateTestpressSession();
        daoSession = ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();
        dashboardSectionDao = daoSession.getDashboardSectionDao();
    }

    private void updateTestpressSession() {
        new SafeAsyncTask<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final TestpressService service = serviceProvider.getService(getActivity());
                return service != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                pager = new DashboardPager(getTestpressService());
                initLoader();
            }
        }.execute();
    }

    TestpressService getTestpressService() {
        if (CommonUtils.isUserAuthenticated(getActivity())) {
            try {
                testpressService = serviceProvider.getService(getActivity());
            } catch (AccountsException e) {
            } catch (IOException e) {
            }
        }
        return testpressService;
    }

    private void initLoader() {
        if (firstCallback) {
            getLoaderManager().initLoader(0, null, this);
            firstCallback = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.dashboard_view, null);

    }

    private List<DashboardSection> getSections() {
        List<DashboardSection> dashboardSections = daoSession.getDashboardSectionDao().queryBuilder()
                .orderAsc(DashboardSectionDao.Properties.Order)
                .list();
        List<DashboardSection> sections = new ArrayList<>();

        for (DashboardSection section: dashboardSections) {
            if (!section.getItems().isEmpty() && !section.getContentType().equals("user_stats")) {
                sections.add(section);
            }
        }
        return sections;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setEnabled(true);
                refresh();
            }
        });

        adapter = new DashboardAdapter(getContext(), getSections());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getSections().isEmpty()) {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    public void refresh() {
        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<List<DashboardSection>> onCreateLoader(int id, @Nullable Bundle args) {
        return new DashboardLoader(getContext(), pager);
    }

    private void showError(final int message) {
        Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void setEmptyText(final int title, final int description, final int icon) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
    }

    private void setEmptyText() {
        setEmptyText(R.string.no_data_found, R.string.try_after_some_time,
                R.drawable.ic_error_outline_black_18dp);
    }

    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.authentication_failed, R.string.testpress_please_login,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_contents,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<DashboardSection>> loader, List<DashboardSection> items) {
        final TestpressException exception = getException(loader);
        swipeRefreshLayout.setRefreshing(false);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!items.isEmpty()) {
                showError(errorMessage);
            }
            getLoaderManager().destroyLoader(loader.getId());
        } else {
            this.exception = null;
            dashboardSectionDao.deleteAll();
            dashboardSectionDao.insertOrReplaceInTx(items);
            adapter.setSections(getSections());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<DashboardSection>> loader) {
    }
}

package in.testpress.testpress.ui.fragments;

import android.accounts.AccountsException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.github.kevinsawicki.wishlist.Toaster;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.ThrowableLoader;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;

import static in.testpress.testpress.util.PreferenceManager.getDashboardDataPreferences;
import static in.testpress.testpress.util.PreferenceManager.setDashboardData;


public class DashboardFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<DashboardResponse> {

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
    private TestpressService testpressService;
    private boolean firstCallback = true;
    private DaoSession daoSession;
    protected Exception exception;
    DashboardResponse dashboardResponse;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        updateTestpressSession();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.setEnabled(true);

        if (getSections().isEmpty()) {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true);
        }
        addOnClickListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.dashboard_view, null);

    }

    private void addOnClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setEnabled(true);
                refresh();
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);
                emptyView.setVisibility(View.GONE);
                refresh();
            }
        });
    }

    public void refresh() {
        if (getActivity() != null) {
            getLoaderManager().restartLoader(0, null, this);
        }
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

    private List<DashboardSection> getSections() {
        return getDashboardDataPreferences(getContext()).getDashboardSections();
    }

    @NonNull
    @Override
    public Loader<DashboardResponse> onCreateLoader(int id, @Nullable Bundle args) {
        return new ThrowableLoader<DashboardResponse>(getContext(), dashboardResponse) {

            @Override
            public DashboardResponse loadData() throws Exception {
                try {
                    return serviceProvider.getService(getActivity()).getDashboardData();
                } catch (Exception exception) {
                    throw exception;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<DashboardResponse> loader, DashboardResponse data) {
        final Exception exception = getException(loader);
        swipeRefreshLayout.setRefreshing(false);
        if (exception != null) {
            this.exception = exception;
            showError(getErrorMessage(exception));
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        setDashboardData(getContext(), json);
    }

    private void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    private void setEmptyText() {
        setEmptyText(R.string.no_data_available, R.string.try_after_some_time,
                R.drawable.ic_error_outline_black_18dp);
    }

    protected int getErrorMessage(Exception exception) {
        if (exception instanceof IOException) {
            if (getSections().isEmpty()) {
                setEmptyText(R.string.authentication_failed, R.string.testpress_please_login,
                        R.drawable.ic_error_outline_black_18dp);
            }
            return R.string.testpress_authentication_failed;
        } else {
            if (getSections().isEmpty()) {
                setEmptyText(R.string.testpress_error_loading_contents,
                        R.string.testpress_some_thing_went_wrong_try_again,
                        R.drawable.ic_error_outline_black_18dp);
            }
        }
        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    protected Exception getException(final Loader<DashboardResponse> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<DashboardResponse>) loader).clearException();
        } else {
            return null;
        }
    }

    private void setEmptyText(final int title, final int description, final int icon) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<DashboardResponse> loader) {

    }

    private void initLoader() {
        if (firstCallback) {
            getLoaderManager().initLoader(0, null, this);
            firstCallback = false;
        }
    }

}

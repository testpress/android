package in.testpress.testpress.ui.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.ThrowableLoader;
import in.testpress.testpress.ui.adapters.DashboardAdapter;
import io.sentry.Sentry;
import io.sentry.protocol.User;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.util.PreferenceManager.getDashboardDataPreferences;
import static in.testpress.testpress.util.PreferenceManager.setDashboardData;


public class DashboardFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<DashboardResponse> {

    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout loadingPlaceholder;

    @Inject
    protected TestpressServiceProvider serviceProvider;
    private DashboardAdapter adapter;
    private boolean firstCallback = true;
    protected Exception exception;
    DashboardResponse dashboardResponse;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TestpressApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        initLoader();
        setUsernameInSentry();
    }

    private void setUsernameInSentry() {
        AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        Account[] account = manager.getAccountsByType(APPLICATION_ID);
        if (account.length > 0) {
            User user = new User();
            user.setUsername(account[0].name);
            Sentry.setUser(user);
        }
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        adapter = new DashboardAdapter(getContext(), new DashboardResponse(), serviceProvider);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.setEnabled(true);
        showDataFromCacheIfAvailable();
        addOnClickListeners();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_View);
        emptyView = view.findViewById(R.id.empty_container);
        retryButton = view.findViewById(R.id.retry_button);
        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        loadingPlaceholder = view.findViewById(R.id.shimmer_view_container);
    }

    private void showDataFromCacheIfAvailable() {
        if (!getSections().isEmpty()) {
            adapter.setResponse(getDashboardDataPreferences(requireContext()));
        } else {
            showLoading();
        }
    }

    private void showLoading() {
        loadingPlaceholder.setVisibility(View.VISIBLE);
        loadingPlaceholder.startShimmer();
    }

    private void hideShimmer() {
        loadingPlaceholder.stopShimmer();
        loadingPlaceholder.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    private List<DashboardSection> getSections() {
        return getDashboardDataPreferences(getContext()).getAvailableSections();
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
        hideShimmer();
        if (exception != null) {
            this.exception = exception;
            getLoaderManager().destroyLoader(loader.getId());
            adapter.setResponse(getDashboardDataPreferences(getContext()));
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        setDashboardData(getContext(), json);
        adapter.setResponse(data);
        adapter.notifyDataSetChanged();
    }

    protected Exception getException(final Loader<DashboardResponse> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<DashboardResponse>) loader).clearException();
        } else {
            return null;
        }
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

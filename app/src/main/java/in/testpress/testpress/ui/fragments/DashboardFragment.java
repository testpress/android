package in.testpress.testpress.ui.fragments;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.util.PreferenceManager.getDashboardDataPreferences;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import in.testpress.testpress.repository.DashBoardRepository;
import in.testpress.testpress.ui.ThrowableLoader;
import in.testpress.testpress.ui.adapters.DashboardAdapter;
import in.testpress.testpress.viewmodel.DashBoardViewModel;
import io.sentry.Sentry;
import io.sentry.protocol.User;


public class DashboardFragment extends Fragment {

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
    @InjectView(R.id.shimmer_view_container)
    ShimmerFrameLayout loadingPlaceholder;

    @Inject
    protected TestpressServiceProvider serviceProvider;
    private DashboardAdapter adapter;
    private TestpressService testpressService;
    private boolean firstCallback = true;
    private DaoSession daoSession;
    protected Exception exception;
    DashboardResponse dashboardResponse;
    private DashBoardViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        initViewModel();
        setUsernameInSentry();
    }

    private void initViewModel() {
        viewModel = DashBoardViewModel.Companion.initializeViewModel(
                this,
                new DashBoardRepository(requireContext())
        );
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.dashboard_view, null);

    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        adapter = new DashboardAdapter(getContext(), new DashboardResponse(), serviceProvider);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.setEnabled(true);

        showDataFromCacheIfAvailable();
        addOnClickListeners();
    }

    private void showDataFromCacheIfAvailable() {
        if (!getSections().isEmpty()) {
            adapter.setResponse(getDashboardDataPreferences(requireContext()));
        } else {
            loadData();
        }
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
            loadData();
        }
    }

    private void loadData() {
        viewModel.loadData().observe(getViewLifecycleOwner(), dashboard -> {
            showLoadingImage();
            switch (dashboard.getStatus()) {
                case SUCCESS: {
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.setResponse(Objects.requireNonNull(dashboard.getData()));
                    hideShimmer();
                    break;
                }
                case ERROR: {
                    hideShimmer();
                    setEmptyText();
                    break;
                }
            }
        });
    }

    private void showLoadingImage() {
        loadingPlaceholder.setVisibility(View.VISIBLE);
        loadingPlaceholder.startShimmer();
    }

    private void hideShimmer() {
        loadingPlaceholder.stopShimmer();
        loadingPlaceholder.setVisibility(View.GONE);
    }

    private void setEmptyText() {
        setEmptyText(R.string.no_data_available, R.string.try_after_some_time,
                R.drawable.ic_error_outline_black_18dp);
    }

    private List<DashboardSection> getSections() {
        return getDashboardDataPreferences(getContext()).getAvailableSections();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
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

}

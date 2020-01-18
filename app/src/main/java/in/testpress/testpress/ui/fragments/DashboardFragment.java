package in.testpress.testpress.ui.fragments;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.network.TestpressApiClient;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.DashboardPager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.BannerDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.DashboardSectionDao;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.adapters.DashboardAdapter;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ThrowableLoader;

import static in.testpress.util.ThrowableLoader.getException;

public class DashboardFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<DashboardSection>> {

    private ArrayList<String> sections = new ArrayList<>();
    @InjectView(R.id.recycler_View)
    RecyclerView recyclerView;
    @InjectView(R.id.welcome_text)
    TextView welcomeText;

    @InjectView(R.id.shimmer_view_container)
    ShimmerFrameLayout loadingPlaceholder;

    @Inject protected TestpressServiceProvider serviceProvider;
    private TestpressService testpressService;
    private DashboardPager pager;
    private DashboardAdapter adapter;
    private DashboardSectionDao dashboardSectionDao;
    private boolean firstCallback = true;
    private TestpressApiClient apiClient;
    private DaoSession daoSession;

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
                // Calling getService will update the testpress session.
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
//                apiClient = new TestpressApiClient(getActivity(), TestpressSdk.getTestpressSession(getContext()));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.dashboard_view, null);

    }

    public void showLoadingPlaceholder() {
        loadingPlaceholder.setVisibility(View.VISIBLE);
        loadingPlaceholder.startShimmer();
    }

    public void hideLoadingPlaceholder() {
        loadingPlaceholder.stopShimmer();
        loadingPlaceholder.setVisibility(View.GONE);
    }

    private List<DashboardSection> getSections() {
        return daoSession.getDashboardSectionDao().queryBuilder()
                .orderAsc(DashboardSectionDao.Properties.Order)
                .listLazy();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        welcomeText.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));

        adapter = new DashboardAdapter(getContext(), getSections());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getSections().isEmpty()) {
            showLoadingPlaceholder();
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

    private static class DashboardLoader extends ThrowableLoader<List<DashboardSection>> {
        private DashboardPager pager;
        private Context context;

        DashboardLoader(Context context, DashboardPager pager) {
            super(context, null);
            this.pager = pager;
            this.context = context;
        }

        @Override
        public List<DashboardSection> loadData() throws TestpressException {
            pager.getItems(1, -1);
            DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();

            ContentDao contentDao = TestpressSDKDatabase.getContentDao(context);
            contentDao.insertOrReplaceInTx(pager.getResponse().getContents());

            CourseAttemptDao courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context);
            courseAttemptDao.insertOrReplaceInTx(pager.getResponse().getContentAttempts());

            PostDao postDao = daoSession.getPostDao();
            postDao.insertOrReplaceInTx(pager.getResponse().getPosts());

            BannerDao bannerDao = daoSession.getBannerDao();
            bannerDao.insertOrReplaceInTx(pager.getResponse().getBanners());
            return pager.getResponse().getDashboardSections();
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<DashboardSection>> loader, List<DashboardSection> items) {
        final TestpressException exception = getException(loader);
        getLoaderManager().destroyLoader(loader.getId());
        hideLoadingPlaceholder();
        if (exception != null) {
            return;
        } else {
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

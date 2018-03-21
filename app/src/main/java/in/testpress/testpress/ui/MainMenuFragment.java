package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.UIUtils;

import static in.testpress.exam.network.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;

public class MainMenuFragment extends Fragment {

    @InjectView(R.id.welcome_message) TextView welcomeMessage;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.grid_layout) RelativeLayout gridLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    GridView grid;
    @InjectView(R.id.recyclerview) RecyclerView recyclerView;
    @InjectView(R.id.quick_links_container)
    LinearLayout quickLinksContainer;
    Account[] account;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        if (account.length == 0) {
            MenuItem logout = menu.findItem(R.id.logout);
            logout.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        getActivity().invalidateOptionsMenu();
        return inflater.inflate(R.layout.main_menu_grid_view, null);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
//        fetchStarredCategories();
        ProfileDetails profileDetails = ProfileDetails.getProfileDetailsFromPreferences(getActivity());
        if (profileDetails == null) {
            gridLayout.setVisibility(View.GONE);
            UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            welcomeMessage.setText(String.format(getString(R.string.welcome_message),
                    profileDetails.getDisplayName()));
        }
        fetchProfileDetails();
        welcomeMessage.setTypeface(TestpressSdk.getRubikRegularFont(getContext()));
        AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        DaoSession daoSession =
                ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();

        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        InstituteSettings instituteSettings = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(Constants.Http.URL_BASE))
                .list().get(0);

        LinkedHashMap<Integer, Integer> mMenuItemResIds = new LinkedHashMap<>();
        final boolean isUserAuthenticated = account.length > 0;
        if (isUserAuthenticated) {
            if (!instituteSettings.getShowGameFrontend()) {
                mMenuItemResIds.put(R.string.my_exams, R.drawable.exams);
            }
            if (instituteSettings.getDocumentsEnabled()) {
                mMenuItemResIds.put(R.string.documents, R.drawable.documents);
            }
            mMenuItemResIds.put(R.string.analytics, R.drawable.analytics);
            mMenuItemResIds.put(R.string.profile, R.drawable.ic_profile_details);
        }
        if (instituteSettings.getStoreEnabled()) {
            mMenuItemResIds.put(R.string.store, R.drawable.store);
        }
        if (instituteSettings.getPostsEnabled()) {
            mMenuItemResIds.put(R.string.posts, R.drawable.posts);
        }
//        mMenuItemResIds.put(R.string.share, R.drawable.share);
//        mMenuItemResIds.put(R.string.rate_us, R.drawable.heart);
//        if (isUserAuthenticated) {
//            mMenuItemResIds.put(R.string.logout, R.drawable.logout);
//        } else {
//            mMenuItemResIds.put(R.string.login, R.drawable.login);
//        }

        MainMenuGridAdapter adapter = new MainMenuGridAdapter(getActivity(), mMenuItemResIds);
        grid=(GridView)view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                switch ((int) id) {
                    case R.string.my_exams:
                        checkAuthenticatedUser(R.string.my_exams);
                        break;
                    case R.string.store:
                        intent = new Intent(getActivity(), ProductsListActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.documents:
                        intent = new Intent(getActivity(), DocumentsListActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.orders:
                        intent = new Intent(getActivity(), OrdersListActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.posts:
                        intent = new Intent(getActivity(), PostsListActivity.class);
                        intent.putExtra("userAuthenticated", isUserAuthenticated);
                        startActivity(intent);
                        break;
                    case R.string.analytics:
                        checkAuthenticatedUser(R.string.analytics);
                        break;
                    case R.string.profile:
                        intent = new Intent(getActivity(), ProfileDetailsActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.share:
                        shareApp();
                        break;
                    case R.string.rate_us:
                        rateApp();
                        break;
                    case R.string.logout:
                        ((MainActivity) getActivity()).logout();
                        break;
                    case R.string.login:
                        intent = new Intent(getActivity(), LoginActivity.class);
                        intent.putExtra(Constants.DEEP_LINK_TO, "home");
                        startActivity(intent);
                        break;
                }
            }
        });
        setHasOptionsMenu(true);
    }

    void checkAuthenticatedUser(final int clickedMenuTitleResId) {
        if (!CommonUtils.isUserAuthenticated(getActivity())) {
            serviceProvider.logout(getActivity(), testpressService,
                    serviceProvider, logoutService);
            return;
        }
        if (TestpressSdk.hasActiveSession(getActivity())) {
            showSDK(clickedMenuTitleResId);
        } else {
            new SafeAsyncTask<Void>() {
                @Override
                public Void call() throws Exception {
                    serviceProvider.getService(getActivity());
                    return null;
                }

                @Override
                protected void onSuccess(Void aVoid) throws Exception {
                    showSDK(clickedMenuTitleResId);
                }
            }.execute();
        }
    }

    void showSDK(int clickedMenuTitleResId) {
        switch (clickedMenuTitleResId) {
            case R.string.my_exams:
                //noinspection ConstantConditions
                TestpressExam.showCategories(getActivity(), true,
                        TestpressSdk.getTestpressSession(getActivity()));
                break;
            case R.string.analytics:
                //noinspection ConstantConditions
                TestpressExam.showAnalytics(getActivity(), SUBJECT_ANALYTICS_PATH,
                        TestpressSdk.getTestpressSession(getActivity()));
                break;
        }
    }

    void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message) + getActivity().getPackageName());
        startActivity(Intent.createChooser(share, "Share with"));
    }

    void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }

    private void fetchProfileDetails() {
        new SafeAsyncTask<ProfileDetails>() {
            @Override
            public ProfileDetails call() throws Exception {
                return serviceProvider.getService(getActivity()).getProfileDetails();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                if (ProfileDetails.getProfileDetailsFromPreferences(getActivity()) != null) {
                    return;
                }
                int errorDescription;
                if (exception.getCause() instanceof IOException) {
                    errorDescription = R.string.no_internet_try_again;
                } else {
                    errorDescription = R.string.try_after_sometime;
                }
                setEmptyText(R.string.network_error, errorDescription);
                progressBar.setVisibility(View.GONE);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        fetchProfileDetails();
                    }
                });
            }

            @Override
            protected void onSuccess(ProfileDetails profileDetails) throws Exception {
                ProfileDetails.saveProfileDetailsInPreferences(getActivity(), profileDetails);
                welcomeMessage.setText(String.format(getString(R.string.welcome_message),
                        profileDetails.getDisplayName()));

                emptyView.setVisibility(View.GONE);
                gridLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                shareApp();
                return true;
            case R.id.rate_us:
                rateApp();
                return true;
            case R.id.logout:
                ((MainActivity) getActivity()).logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchStarredCategories() {
        new SafeAsyncTask<List<Category>>() {
            @Override
            public List<Category> call() throws Exception {
                Map<String, String> queryParams = new LinkedHashMap<String, String>();
                queryParams.put("starred", "true");
                if (account.length > 0) {
                    return serviceProvider.getService(getActivity())
                            .getCategories(Constants.Http.URL_CATEGORIES_FRAG, queryParams).getResults();
                } else {
                    return testpressService
                            .getCategories(Constants.Http.URL_CATEGORIES_FRAG, queryParams).getResults();
                }
            }

            protected void onSuccess(final List<Category> categories) throws Exception {
                if (getActivity() == null) {
                    return;
                }
                Ln.e("On success");
                if (categories.isEmpty()) {
                    quickLinksContainer.setVisibility(View.GONE);
                } else {
                    quickLinksContainer.setVisibility(View.VISIBLE);
                    CategoryDao categoryDao = ((TestpressApplication) getActivity()
                            .getApplicationContext()).getDaoSession().getCategoryDao();
                    categoryDao.insertOrReplaceInTx(categories);
                    recyclerView.setAdapter(new StarredCategoryAdapter(getActivity(),
                            categories));
                }
            }
        }.execute();
    }

    public static class StarredCategoryAdapter
            extends RecyclerView.Adapter<StarredCategoryAdapter.ViewHolder> {


        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<Category> mValues;
        public class ViewHolder extends RecyclerView.ViewHolder {
            public Long mCategoryId;

            public final View mView;
            public final TextView categoryView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                categoryView = (TextView) view.findViewById(R.id.category);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + categoryView.getText();
            }
        }

        public Category getValueAt(int position) {
            return mValues.get(position);
        }

        public StarredCategoryAdapter(Context context, List<Category> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_menu_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mCategoryId = mValues.get(position).getId();
            holder.categoryView.setText(mValues.get(position).getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PostsListActivity.class);
                    intent.putExtra("category_filter", holder.mCategoryId);

                    context.startActivity(intent);
                }
            });
            ShapeDrawable colorDrawable = (ShapeDrawable) holder.categoryView.getCompoundDrawables()[2];
            if (colorDrawable == null) {
                colorDrawable = new ShapeDrawable(new OvalShape());
                int mDotSize = holder.mView.getResources().getDimensionPixelSize(
                        R.dimen.tag_color_dot_size);
                colorDrawable.setIntrinsicWidth(mDotSize);
                colorDrawable.setIntrinsicHeight(mDotSize);
                colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                holder.categoryView.setCompoundDrawablesWithIntrinsicBounds(colorDrawable, null,
                        null, null);
            }
            colorDrawable.getPaint().setColor(Color.parseColor("#" + mValues.get(position).getColor()));
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
    }
}

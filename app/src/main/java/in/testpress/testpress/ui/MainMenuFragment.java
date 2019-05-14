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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.store.TestpressStore;
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
import in.testpress.testpress.models.TestpressApiErrorResponse;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.Strings;
import in.testpress.testpress.util.UIUtils;
import retrofit.RetrofitError;

import static in.testpress.exam.network.TestpressExamApiClient.SUBJECT_ANALYTICS_PATH;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.ui.DrupalRssListFragment.RSS_FEED_URL;

public class MainMenuFragment extends Fragment {

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    GridView grid;
    @InjectView(R.id.recyclerview) RecyclerView recyclerView;
    @InjectView(R.id.quick_links_container)
    LinearLayout quickLinksContainer;
    Account[] account;

    private InstituteSettings mInstituteSettings;

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
        fetchStarredCategories();
        AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        account = manager.getAccountsByType(APPLICATION_ID);
        DaoSession daoSession =
                ((TestpressApplication) getActivity().getApplicationContext()).getDaoSession();

        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        InstituteSettings instituteSettings = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list().get(0);
        mInstituteSettings = instituteSettings;

        LinkedHashMap<Integer, Integer> mMenuItemResIds = new LinkedHashMap<>();
        final boolean isUserAuthenticated = account.length > 0;
        // ToDo get from institute settings
        boolean drupalRssFeedEnabled = false;

        if (!Strings.toString(instituteSettings.getAboutUs()).isEmpty()) {
            mMenuItemResIds.put(R.string.about_us, R.drawable.about_us);
        }

        if (isUserAuthenticated) {

            if (!instituteSettings.getShowGameFrontend()) {
                mMenuItemResIds.put(R.string.my_exams, R.drawable.exams);
            }
            if (instituteSettings.getBookmarksEnabled()) {
                mMenuItemResIds.put(R.string.bookmarks, R.drawable.bookmark);
            }
            if (instituteSettings.getDocumentsEnabled()) {
                mMenuItemResIds.put(R.string.documents, R.drawable.documents);
            }

            if (!instituteSettings.getDisableStudentAnalytics()) {
                mMenuItemResIds.put(R.string.analytics, R.drawable.analytics);
            }

            mMenuItemResIds.put(R.string.profile, R.drawable.ic_profile_details);
            if (instituteSettings.getStoreEnabled()) {
                mMenuItemResIds.put(R.string.store, R.drawable.store);
            }
        }
        if (drupalRssFeedEnabled) {
            mMenuItemResIds.put(R.string.rss_posts, R.drawable.rss_feed);
        }
        if (instituteSettings.getPostsEnabled()) {
            mMenuItemResIds.put(R.string.posts, R.drawable.posts);
        }
        mMenuItemResIds.put(R.string.share, R.drawable.share);
        mMenuItemResIds.put(R.string.rate_us, R.drawable.heart);
        if (isUserAuthenticated) {
            mMenuItemResIds.put(R.string.logout, R.drawable.logout);
        } else {
            mMenuItemResIds.put(R.string.login, R.drawable.login);
        }

        MainMenuGridAdapter adapter = new MainMenuGridAdapter(getActivity(), mMenuItemResIds, instituteSettings);
        grid=(GridView)view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                String custom_title;
                switch ((int) id) {
                    case R.string.about_us:
                        intent = new Intent(getActivity(), AboutUsActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.my_exams:
                        checkAuthenticatedUser(R.string.my_exams);
                        break;
                    case R.string.bookmarks:
                        checkAuthenticatedUser(R.string.bookmarks);
                        break;
                    case R.string.store:
                        checkAuthenticatedUser(R.string.store);
                        break;
                    case R.string.documents:
                        custom_title = UIUtils.getMenuItemName(R.string.documents, mInstituteSettings);
                        intent = new Intent(getActivity(), DocumentsListActivity.class);
                        intent.putExtra("title", custom_title);
                        startActivity(intent);
                        break;
                    case R.string.orders:
                        intent = new Intent(getActivity(), OrdersListActivity.class);
                        startActivity(intent);
                        break;
                    case R.string.rss_posts:
                        intent = new Intent(getActivity(), DrupalRssListActivity.class);
                        intent.putExtra(RSS_FEED_URL, "https://www.wired.com/feed/");
                        startActivity(intent);
                        break;
                    case R.string.posts:
                        custom_title = UIUtils.getMenuItemName(R.string.posts, mInstituteSettings);
                        intent = new Intent(getActivity(), PostsListActivity.class);
                        intent.putExtra("userAuthenticated", isUserAuthenticated);
                        intent.putExtra("title", custom_title);
                        startActivity(intent);
                        break;
                    case R.string.forum:
                        intent = new Intent(getActivity(), ForumListActivity.class);
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
        //noinspection ConstantConditions
        TestpressSession session = TestpressSdk.getTestpressSession(getActivity());
        assert session != null;
        switch (clickedMenuTitleResId) {
            case R.string.my_exams:
                TestpressExam.showCategories(getActivity(), true, session);
                break;
            case R.string.bookmarks:
                TestpressExam.showBookmarks(getActivity(), session);
                break;
            case R.string.analytics:
                TestpressExam.showAnalytics(getActivity(), SUBJECT_ANALYTICS_PATH, session);
                break;
            case R.string.store:
                String title = UIUtils.getMenuItemName(R.string.store, mInstituteSettings);
                Intent intent = new Intent();
                intent.putExtra("title", title);
                getActivity().setIntent(intent);
                TestpressStore.show(getActivity(), session);
                break;
        }
    }

    void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) +
                getString(R.string.get_it_at) + getActivity().getPackageName());
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

            protected void onException(Exception e) {
                super.onException(e);

                if (e.getMessage().equals("403 FORBIDDEN")){
                    logoutIfExceptionContainInvalidSignature(e);
                }
            }
        }.execute();
    }

    public void logoutIfExceptionContainInvalidSignature(Exception e) {

        TestpressApiErrorResponse testpressApiErrorResponse = (TestpressApiErrorResponse) (((RetrofitError) e).getBodyAs(TestpressApiErrorResponse.class));

        if (testpressApiErrorResponse.getDetail().equals("Invalid signature")) {
            serviceProvider.logout(getActivity(), testpressService, serviceProvider, logoutService);
        }
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
}

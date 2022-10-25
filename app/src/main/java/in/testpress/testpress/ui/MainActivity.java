package in.testpress.testpress.ui;
import in.testpress.course.ui.CourseListFragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.course.fragments.DownloadsFragment;
import in.testpress.course.repository.VideoWatchDataRepository;
import in.testpress.database.OfflineVideoDao;
import in.testpress.database.TestpressDatabase;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.CheckPermission;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.SsoUrl;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.ui.fragments.DashboardFragment;
import in.testpress.testpress.ui.fragments.DiscussionFragmentv2;
import in.testpress.testpress.ui.utils.HandleMainMenu;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.Strings;
import in.testpress.testpress.util.UIUtils;
import in.testpress.testpress.util.UpdateAppDialogManager;
import in.testpress.ui.fragments.DiscussionFragment;
import io.sentry.android.core.SentryAndroid;

import static in.testpress.testpress.BuildConfig.ALLOW_ANONYMOUS_USER;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.ui.TermsAndConditionActivityKt.TERMS_AND_CONDITIONS;
import static in.testpress.testpress.ui.utils.EasterEggUtils.enableOrDisableEasterEgg;
import static in.testpress.testpress.ui.utils.EasterEggUtils.enableScreenShot;
import static in.testpress.testpress.ui.utils.EasterEggUtils.isEasterEggEnabled;
import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;

public class MainActivity extends TestpressFragmentActivity {

    private static final String SELECTED_ITEM = "selectedItem";

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;

    @InjectView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @InjectView(R.id.progressbar) RelativeLayout progressBarLayout;
    @InjectView(R.id.viewpager)
    NonSwipeableViewPager viewPager;
    @InjectView(R.id.grid) GridView grid;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawer;
    @InjectView(R.id.navigation_view)
    NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;
    private int mSelectedItem;
    private BottomNavBarAdapter mBottomBarAdapter;
    private ArrayList<Integer> mMenuItemImageIds = new ArrayList<>();
    private ArrayList<Integer> mMenuItemTitleIds = new ArrayList<>();
    private ArrayList<Fragment> mMenuItemFragments = new ArrayList<>();
    private InstituteSettings mInstituteSettings;
    private InstituteSettingsDao instituteSettingsDao;
    private boolean isUserAuthenticated;
    public String ssoUrl;
    private boolean isInitScreenCalledOnce;
    private CourseListFragment courseListFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM);
        }
        viewPager.setVisibility(View.GONE);
        viewPager.setSwipeEnabled(false);
        DaoSession daoSession = TestpressApplication.getDaoSession();
        instituteSettingsDao = daoSession.getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list();

        if (instituteSettingsList.size() > 0) {
            onFinishFetchingInstituteSettings(instituteSettingsList.get(0));
            checkUpdate();
        } else {
            checkUpdate();
        }
        setupEasterEgg();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isProductPurchaseSuccessful(requestCode, resultCode)) {
            courseListFragment.onActivityResult(requestCode, resultCode, data);
         }
    }

    private boolean isProductPurchaseSuccessful(int requestCode, int resultCode){
        return requestCode == STORE_REQUEST_CODE && resultCode == RESULT_OK;
    }

    private void setupEasterEgg() {
        Menu navigationMenu = navigationView.getMenu();
        final MenuItem rateUsButton = navigationMenu.findItem(R.id.rate_us);
        Button button = new Button(this);
        button.setAlpha(0);
        rateUsButton.setActionView(button);
        rateUsButton.getActionView().setVisibility(View.GONE);


        findViewById(R.id.version_info).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "App version is " + getString(R.string.version), Toast.LENGTH_SHORT).show();
                enableOrDisableEasterEgg(getApplicationContext(), true);
                rateUsButton.getActionView().setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rateUsButton.getActionView().setVisibility(View.GONE);
                        enableOrDisableEasterEgg(getApplicationContext(), false);
                    }
                }, 7000);
                return false;
            }
        });

        rateUsButton.getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (isEasterEggEnabled(getApplicationContext())) {
                    enableScreenShot(getApplicationContext());
                }
                return false;
            }
        });
    }

    private void setUpNavigationDrawer() {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
        drawerToggle.syncState();

        drawer.addDrawerListener(drawerToggle);
        setupDrawerContent(navigationView);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(
                this, drawer, getActionBarToolbar(),
                R.string.open_drawer,  R.string.close_drawer
        );
    }

    private void setupDrawerContent(NavigationView navigationView) {
        hideMenuItemsForUnauthenticatedUser(navigationView.getMenu());
        showShareButtonBasedOnInstituteSettings(navigationView.getMenu());
        showRateUsButtonBasedOnInstituteSettings(navigationView.getMenu());
        updateMenuItemNames(navigationView.getMenu());
        final HandleMainMenu handleMainMenu = new HandleMainMenu(MainActivity.this, serviceProvider);
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    handleMainMenu.handleMenuOptionClick(menuItem.getItemId());
                    return true;
                }
        });
    }

    private void hideMenuItemsForUnauthenticatedUser(Menu menu) {
        AccountManager manager = (AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] account = manager.getAccountsByType(APPLICATION_ID);
        final boolean isUserAuthenticated = account.length > 0;
        if (!isUserAuthenticated) {
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.login_activity).setVisible(false);
            menu.findItem(R.id.analytics).setVisible(false);
            menu.findItem(R.id.profile).setVisible(false);
            menu.findItem(R.id.bookmarks).setVisible(false);
        } else {
            menu.findItem(R.id.logout).setVisible(true);
            if (mInstituteSettings != null) {
                menu.findItem(R.id.doubts).setVisible(Boolean.TRUE.equals(mInstituteSettings.getIsHelpdeskEnabled()));
            }
            menu.findItem(R.id.login_activity).setVisible(true);
            menu.findItem(R.id.analytics).setVisible(true);
            menu.findItem(R.id.profile).setVisible(true);
            menu.findItem(R.id.bookmarks).setVisible(true);
            menu.findItem(R.id.login).setVisible(false);
        }
    }

    private void showShareButtonBasedOnInstituteSettings(Menu menu){
        if (mInstituteSettings != null) {
            menu.findItem(R.id.share).setVisible(Boolean.TRUE.equals(mInstituteSettings.getShowShareButton()));
        }
    }

    private void showRateUsButtonBasedOnInstituteSettings(Menu menu){
        if (mInstituteSettings != null) {
            menu.findItem(R.id.rate_us).setVisible(Boolean.TRUE.equals(mInstituteSettings.getShowShareButton()));
        }
    }

    private void updateMenuItemNames(Menu menu) {
        if (mInstituteSettings != null) {
            menu.findItem(R.id.posts).setTitle(Strings.toString(mInstituteSettings.getPostsLabel()));
            menu.findItem(R.id.bookmarks).setTitle(Strings.toString(mInstituteSettings.getBookmarksLabel()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTestpressSession() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                // Calling getService will update the testpress session.
                final TestpressService service = serviceProvider.getService(MainActivity.this);
                return service != null;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                if (e instanceof OperationCanceledException) {
                    // User cancelled the authentication process (back button, etc).
                    // Since auth could not take place, lets finish this activity.
                    finish();
                }
            }

            @Override
            protected void onSuccess(final Boolean hasAuthenticated) throws Exception {
                super.onSuccess(hasAuthenticated);
                isUserAuthenticated = true;
                if (viewPager.getVisibility() != View.VISIBLE) {
                    initScreen();
                }
            }
        }.execute();
    }

    private void initScreen() {
        isInitScreenCalledOnce = true;
        SharedPreferences preferences =
                getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)) {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.makeGooglePlayServicesAvailable(this);
            CommonUtils.registerDevice(MainActivity.this, testpressService, serviceProvider);
        }

        if (isUserAuthenticated && mInstituteSettings.getShowGameFrontend()) {
            addMenuItem(R.string.dashboard, R.drawable.ic_dashboard, new DashboardFragment());
        } else {
            addMenuItem(R.string.dashboard, R.drawable.profile_default, new MainMenuFragment());
        }
        // Show courses list if game front end is enabled, otherwise hide bottom bar
        if (isUserAuthenticated && mInstituteSettings.getShowGameFrontend()) {
            //noinspection ConstantConditions
            addMenuItem(R.string.learn, R.drawable.learn,
                    courseListFragment = TestpressCourse.getCoursesListFragment(this, TestpressSdk.getTestpressSession(this)));

            if (mInstituteSettings.getCoursesEnableGamification()) {
                //noinspection ConstantConditions
                addMenuItem(R.string.testpress_leaderboard, R.drawable.leaderboard,
                        TestpressCourse.getLeaderboardFragment(this, TestpressSdk.getTestpressSession(this)));
            }
            if (mInstituteSettings.getForumEnabled()) {
                addMenuItem(R.string.discussions, R.drawable.chat_icon, new DiscussionFragmentv2());
            }
            if (mInstituteSettings.getIsVideoDownloadEnabled()) {
                DownloadsFragment downloadsFragment = new DownloadsFragment();
                addMenuItem(R.string.downloads, R.drawable.ic_downloads, downloadsFragment);
            }
        } else {
            grid.setVisibility(View.GONE);
        }
        mBottomBarAdapter = new BottomNavBarAdapter(this, mMenuItemImageIds, mMenuItemTitleIds, mInstituteSettings);
        grid.setAdapter(mBottomBarAdapter);
        grid.setNumColumns(mBottomBarAdapter.getCount());
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
            }
        });
        BottomBarPagerAdapter mPagerAdapter = new BottomBarPagerAdapter(this, mMenuItemFragments);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                onItemSelected(position);
                if (!CommonUtils.isUserAuthenticated(MainActivity.this)) {
                    serviceProvider.logout(MainActivity.this, testpressService, serviceProvider,
                            logoutService);
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        viewPager.setCurrentItem(mSelectedItem);
        viewPager.setVisibility(View.VISIBLE);
        onItemSelected(mSelectedItem);
        progressBarLayout.setVisibility(View.GONE);
    }

    private void onItemSelected(int position) {
        mSelectedItem = position;
        mBottomBarAdapter.setSelectedPosition(position);
        mBottomBarAdapter.notifyDataSetChanged();

        if (!UIUtils.getMenuItemName(mMenuItemTitleIds.get(position), mInstituteSettings).isEmpty()) {
            updateToolbarText(UIUtils.getMenuItemName(mMenuItemTitleIds.get(position), mInstituteSettings));
        } else {
            updateToolbarText(getString(mMenuItemTitleIds.get(position)));
        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    private void fetchInstituteSettings() {
        if (mInstituteSettings == null) {
            progressBarLayout.setVisibility(View.VISIBLE);
        }
        new SafeAsyncTask<InstituteSettings>() {
            @Override
            public InstituteSettings call() {
                return testpressService.getInstituteSettings();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                if (mInstituteSettings != null) {
                    return;
                }
                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
                progressBarLayout.setVisibility(View.GONE);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        fetchInstituteSettings();
                    }
                });
            }

            @Override
            protected void onSuccess(InstituteSettings instituteSettings) {
                instituteSettings.setBaseUrl(BASE_URL);
                instituteSettingsDao.insertOrReplace(instituteSettings);

                if (mInstituteSettings == null) {
                    onFinishFetchingInstituteSettings(instituteSettings);
                } else if (mInstituteSettings.getForceStudentData()) {
                    checkForForceUserData();
                } else {
                    showMainActivityContents();
                }
            }
        }.execute();
    }

    public void onFinishFetchingInstituteSettings(InstituteSettings instituteSettings) {
        this.mInstituteSettings = instituteSettings;
        isUserAuthenticated = CommonUtils.isUserAuthenticated(this);
        setUpNavigationDrawer();
        SentryAndroid.init(
                this,
                options -> {
                    options.setDsn(instituteSettings.getAndroidSentryDns());
                    options.setEnableSessionTracking(true);
                });
        //noinspection ConstantConditions
        if (!isUserAuthenticated && !ALLOW_ANONYMOUS_USER) {
            // Show login screen if user not logged in else update institute settings in TestpressSDK
            updateTestpressSession();
        } else {
            initScreen();
            showMainActivityContents();
            syncVideoWatchedData();

            if (isUserAuthenticated) {
                updateTestpressSession();

                if (mInstituteSettings.getForceStudentData()) {
                    checkForForceUserData();
                }
            }
        }
    }

    private void syncVideoWatchedData() {
        OfflineVideoDao offlineVideoDao = TestpressDatabase.Companion.invoke(this).offlineVideoDao();
        VideoWatchDataRepository videoWatchDataRepository = new VideoWatchDataRepository(this, offlineVideoDao);
        AsyncTask.execute((Runnable) videoWatchDataRepository::sync);
    }

    private void checkUpdate() {
        if (mInstituteSettings == null) {
            progressBarLayout.setVisibility(View.VISIBLE);
        }
        new SafeAsyncTask<Update>() {
            @Override
            public Update call() {
                return testpressService.checkUpdate("" + BuildConfig.VERSION_CODE, getApplicationContext().getPackageName());
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                fetchInstituteSettings();
            }

            @Override
            protected void onSuccess(final Update update) {
                progressBarLayout.setVisibility(View.GONE);
                if(update.getUpdateRequired()) {
                    if (update.getForce()) {
                        UpdateAppDialogManager
                                .showDialog(MainActivity.this, true, update.getMessage());
                    } else {
                        fetchInstituteSettings();
                        if (UpdateAppDialogManager.canShowDialog(MainActivity.this, update.getDays())) {
                            UpdateAppDialogManager
                                    .showDialog(MainActivity.this, false, update.getMessage());
                        }
                    }
                } else {
                    fetchInstituteSettings();
                }
            }
        }.execute();
    }

    public void addMenuItem(int titleResId, int imageResId, Fragment fragment) {
        mMenuItemTitleIds.add(titleResId);
        mMenuItemImageIds.add(imageResId);
        mMenuItemFragments.add(fragment);
    }

    public void logout() {
        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                serviceProvider.logout(MainActivity.this, testpressService,
                                        serviceProvider, logoutService);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isVerandaLearningApp()){
            showTermsAndConditionsIfNotAgreed();
        }

        if (navigationView != null) {
            hideMenuItemsForUnauthenticatedUser(navigationView.getMenu());
        }
        if (mInstituteSettings != null && mInstituteSettings.getForceStudentData()) {
            checkForForceUserData();
        } else {
            showMainActivityContents();
        }
    }

    public void callWebViewActivity(String url) {

        if (!Strings.toString(url).isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.ACTIVITY_TITLE, "Mandatory Update");
            intent.putExtra(WebViewActivity.SHOW_LOGOUT, "true");
            intent.putExtra(WebViewActivity.URL_TO_OPEN, BASE_URL + url + "&next=/settings/force/mobile/");
            startActivity(intent);
        }
    }

    public void checkForForceUserData() {
        new SafeAsyncTask<CheckPermission>() {
            @Override
            public CheckPermission call() throws Exception {
                return serviceProvider.getService(MainActivity.this).checkPermission();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                hideMainActivityContents();

                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        fetchInstituteSettings();
                    }
                });
            }

            @Override
            protected void onSuccess(final CheckPermission checkPermission) {
                progressBarLayout.setVisibility(View.GONE);

                if (!checkPermission.getIsDataCollected()) {
                    hideMainActivityContents();

                    if (!Strings.toString(ssoUrl).isEmpty()) {
                        callWebViewActivity(ssoUrl);
                    } else {
                        fetchSsoLink();
                    }
                } else {
                    showMainActivityContents();
                }
            }
        }.execute();
    }

    public void fetchSsoLink() {
        new SafeAsyncTask<SsoUrl>() {
            @Override
            public SsoUrl call() throws Exception {
                return serviceProvider.getService(MainActivity.this).getSsoUrl();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                hideMainActivityContents();

                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        checkForForceUserData();
                    }
                });
            }

            @Override
            protected void onSuccess(final SsoUrl ssoLink) throws Exception {
                showMainActivityContents();
                ssoUrl = ssoLink.getSsoUrl();
                callWebViewActivity(ssoLink.getSsoUrl());
            }
        }.execute();
    }

    public void hideMainActivityContents(){
        grid.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
    }

    public void showMainActivityContents(){

        if (isInitScreenCalledOnce) {
            viewPager.setVisibility(View.VISIBLE);
            grid.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isVerandaLearningApp(){
        return getApplicationContext().getPackageName().equals("com.verandalearning");
    }

    public void showTermsAndConditionsIfNotAgreed(){
        boolean data = getSharedPreferences(TERMS_AND_CONDITIONS, Context.MODE_PRIVATE).getBoolean(TERMS_AND_CONDITIONS, false);
        if (data == false){
            startActivity(TermsAndConditionActivity.Companion.createIntent(MainActivity.this));
        }
    }

}

package in.testpress.testpress.ui;
import in.testpress.RequestCode;
import in.testpress.course.fragments.OfflineDownloadsTabsFragment;
import in.testpress.course.repository.OfflineAttachmentsRepository;
import in.testpress.course.services.OfflineAttachmentDownloadManager;
import in.testpress.course.ui.AvailableCourseListFragment;
import in.testpress.course.ui.CourseListFragment;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.util.Log;
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

import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.course.fragments.DownloadsFragment;
import in.testpress.course.repository.VideoWatchDataRepository;
import in.testpress.course.ui.MyCoursesFragment;
import in.testpress.database.OfflineVideoDao;
import in.testpress.database.TestpressDatabase;
import in.testpress.database.dao.OfflineAttachmentsDao;
import in.testpress.exam.ui.view.NonSwipeableViewPager;
import in.testpress.testpress.BuildConfig;
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
import in.testpress.testpress.models.Update;
import in.testpress.testpress.ui.fragments.DashboardFragment;
import in.testpress.testpress.ui.utils.HandleMainMenu;
import in.testpress.testpress.util.AppChecker;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.SalesforceSdkInitializer;
import in.testpress.testpress.util.Strings;
import in.testpress.testpress.util.UIUtils;
import in.testpress.testpress.util.UpdateAppDialogManager;
import io.sentry.android.core.SentryAndroid;

import static in.testpress.testpress.BuildConfig.ALLOW_ANONYMOUS_USER;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.BuildConfig.WHITE_LABELED_HOST_URL;
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
    private LinearLayout emptyView;
    private TextView emptyTitleView;
    private TextView emptyDescView;
    private Button retryButton;

    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout progressBarLayout;
    private NonSwipeableViewPager viewPager;
    private GridView grid;
    private DrawerLayout drawer;
    private NavigationView navigationView;

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
    int touchCountToEnableScreenShot = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        TestpressApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        bindViews();

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
        initOfflineAttachmentDownloadManager();
    }

    private void bindViews() {
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        retryButton = findViewById(R.id.retry_button);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        progressBarLayout = findViewById(R.id.progressbar);
        viewPager = findViewById(R.id.viewpager);
        grid = findViewById(R.id.grid);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }


    @Override
    public void onBackPressed() {
        if (courseListFragment != null && viewPager.getCurrentItem() == 1) {
            if (courseListFragment.onBackPress()) {
                viewPager.setCurrentItem(0);
            }
        } else if (viewPager.getCurrentItem() != 0) {
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
        final MenuItem versionInfo = navigationMenu.findItem(R.id.version_info);
        Button button = new Button(this);
        button.setAlpha(0);
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "App version is " + getString(R.string.version), Toast.LENGTH_SHORT).show();
                enableOrDisableEasterEgg(getApplicationContext(), true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableOrDisableEasterEgg(getApplicationContext(), false);
                    }
                }, 7000);
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEasterEggEnabled(getApplicationContext())) {
                    if (touchCountToEnableScreenShot == 4) {
                        enableScreenShot(getApplicationContext());
                        touchCountToEnableScreenShot = 0;
                    } else {
                        touchCountToEnableScreenShot++;
                    }
                } else {
                    touchCountToEnableScreenShot = 0;
                }
            }
        });
        versionInfo.setActionView(button);
    }

    private void initOfflineAttachmentDownloadManager() {
        OfflineAttachmentsDao offlineAttachmentDao = TestpressDatabase.Companion.invoke(this).offlineAttachmentDao();
        OfflineAttachmentsRepository offlineAttachmentsRepository =new OfflineAttachmentsRepository(offlineAttachmentDao);
        OfflineAttachmentDownloadManager.Companion.init(offlineAttachmentsRepository);
        OfflineAttachmentDownloadManager.Companion.getInstance().restartDownloadProgressTracking(this);
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
        showOfflineExamBasedOnInstituteSettings(navigationView.getMenu());
        showDiscussionsButtonBasedOnInstituteSettings(navigationView.getMenu());
        showBookmarkButtonBasedOnInstituteSettings(navigationView.getMenu());
        showCustomOptions(navigationView.getMenu());
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
        } else {
            menu.findItem(R.id.logout).setVisible(true);
            if (mInstituteSettings != null) {
                menu.findItem(R.id.doubts).setVisible(Boolean.TRUE.equals(mInstituteSettings.getIsHelpdeskEnabled()));
            }
            menu.findItem(R.id.login_activity).setVisible(true);
            menu.findItem(R.id.analytics).setVisible(true);
            menu.findItem(R.id.profile).setVisible(true);
            menu.findItem(R.id.login).setVisible(false);
            if (mInstituteSettings != null){
                menu.findItem(R.id.student_report).setVisible(mInstituteSettings.isStudentReportEnabled());
            }
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

    private void showDiscussionsButtonBasedOnInstituteSettings(Menu menu) {
        if (mInstituteSettings != null) {
            String discussionsLabel = (mInstituteSettings.getForumLabel() != null)
                    ? mInstituteSettings.getForumLabel()
                    : getString(R.string.discussions);
            menu.findItem(R.id.discussions).setTitle(discussionsLabel);
            menu.findItem(R.id.discussions).setVisible(Boolean.TRUE.equals(mInstituteSettings.getForumEnabled()));
        }
    }

    private void showOfflineExamBasedOnInstituteSettings(Menu menu) {
        if (mInstituteSettings != null) {
            menu.findItem(R.id.offline_exam_list).setVisible(Boolean.TRUE.equals(mInstituteSettings.getEnableOfflineExam(this)));
        }
    }

    private void showBookmarkButtonBasedOnInstituteSettings(Menu menu) {
        if (mInstituteSettings != null) {
            String BookmarksLabel = (mInstituteSettings.getBookmarksLabel() != null)
                    ? mInstituteSettings.getBookmarksLabel()
                    : getString(R.string.bookmarks);
            menu.findItem(R.id.bookmarks).setTitle(BookmarksLabel);
            menu.findItem(R.id.bookmarks).setVisible(Boolean.TRUE.equals(mInstituteSettings.getBookmarksEnabled()));
        }
    }

    private void showCustomOptions(Menu menu) {
        if (mInstituteSettings != null && AppChecker.INSTANCE.isCatkingApp(this)) {
            menu.findItem(R.id.recorded_lessons).setVisible(true);
            menu.findItem(R.id.mocks).setVisible(true);
            menu.findItem(R.id.e_books).setVisible(true);
            menu.findItem(R.id.live_lectures_cat).setVisible(true);
            menu.findItem(R.id.live_lectures_non_cat).setVisible(true);
            menu.findItem(R.id.live_lectures_gd_watpi).setVisible(true);
        }
    }

    private void updateMenuItemNames(Menu menu) {
        if (mInstituteSettings != null) {
            menu.findItem(R.id.posts).setVisible(Boolean.TRUE.equals(mInstituteSettings.getPostsEnabled()));
            menu.findItem(R.id.posts).setTitle(Strings.toString(mInstituteSettings.getPostsLabel()));
            menu.findItem(R.id.bookmarks).setTitle(Strings.toString(mInstituteSettings.getBookmarksLabel()));
        }
        menu.findItem(R.id.version_info).setTitle("Version - "+getString(R.string.version));
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
                initSalesForceSDK();
                askNotificationAndStoragePermission();
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
            if (mInstituteSettings.getDashboardEnabled()){
                addMenuItem(R.string.dashboard, R.drawable.ic_dashboard, new DashboardFragment());
            }
        } else {
            addMenuItem(R.string.dashboard, R.drawable.profile_default, new MainMenuFragment());
        }
        // Show courses list if game front end is enabled, otherwise hide bottom bar
        if (isUserAuthenticated && mInstituteSettings.getShowGameFrontend()) {
            //noinspection ConstantConditions
            addMenuItem(R.string.learn, R.drawable.learn, new MyCoursesFragment());

            if (Boolean.TRUE.equals(!mInstituteSettings.getDisableStoreInApp())){
                addMenuItem(R.string.store, R.drawable.home_store_image, new AvailableCourseListFragment());
            }

            if (mInstituteSettings.getCoursesEnableGamification()) {
                //noinspection ConstantConditions
                addMenuItem(R.string.leaderboard, R.drawable.leaderboard,
                        TestpressCourse.getLeaderboardFragment(this, TestpressSdk.getTestpressSession(this)));
            }
            if (mInstituteSettings.getIsVideoDownloadEnabled()) {
                OfflineDownloadsTabsFragment offlineDownloadsTabsFragment = new OfflineDownloadsTabsFragment();
                addMenuItem(R.string.downloads, R.drawable.ic_downloads, offlineDownloadsTabsFragment);
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
            public InstituteSettings call() throws Exception {
                if (isUserAuthenticated) {
                    return serviceProvider.getService(MainActivity.this).getInstituteSettings();
                } else {
                    return testpressService.getInstituteSettings();
                }
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
                } else if (isUserAuthenticated && mInstituteSettings.getForceStudentData()) {
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
                    options.setEnableAutoSessionTracking(true);
                });
        //noinspection ConstantConditions
        if (!isUserAuthenticated && !ALLOW_ANONYMOUS_USER) {
            // Show login screen if user not logged in else update institute settings in TestpressSDK
            updateTestpressSession();
        } else {
            if(isVerandaLearningApp() && !hasAgreedTermsAndConditions()){
                startActivity(TermsAndConditionActivity.Companion.createIntent(MainActivity.this));
            }
            initScreen();
            showMainActivityContents();

            if (isUserAuthenticated) {
                updateTestpressSession();
                syncVideoWatchedData();

                if (isUserAuthenticated && mInstituteSettings.getForceStudentData()) {
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
                                setTermsAndConditionNotAgreed();
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

        if (navigationView != null) {
            hideMenuItemsForUnauthenticatedUser(navigationView.getMenu());
        }
        if (isUserAuthenticated && mInstituteSettings != null && mInstituteSettings.getForceStudentData()) {
            checkForForceUserData();
        } else {
            showMainActivityContents();
        }
    }

    public void openEnforceDataActivity(){
        this.startActivity(
                EnforceDataActivity.Companion.createIntent(
                        this,
                        "Mandatory Update",
                        WHITE_LABELED_HOST_URL + "/settings/force/mobile/",
                        true,
                        false,
                        EnforceDataActivity.class
                )
        );
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

                    openEnforceDataActivity();
                } else {
                    showMainActivityContents();
                }
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

    private void askNotificationAndStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions(new String[]{
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            }, RequestCode.PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            }, RequestCode.PERMISSION);
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCode.PERMISSION);

        }
    }

    private Boolean isVerandaLearningApp(){
        return getApplicationContext().getPackageName().equals("com.verandalearning");
    }

    private Boolean hasAgreedTermsAndConditions(){
        return getSharedPreferences(TERMS_AND_CONDITIONS, Context.MODE_PRIVATE).getBoolean(TERMS_AND_CONDITIONS, false);
    }

    private void setTermsAndConditionNotAgreed() {
        SharedPreferences.Editor editor = getSharedPreferences(TERMS_AND_CONDITIONS, MODE_PRIVATE).edit();
        editor.putBoolean(TERMS_AND_CONDITIONS, false);
        editor.apply();
    }

    private void initSalesForceSDK() {
        if (Boolean.TRUE.equals(mInstituteSettings.getSalesforceSdkEnabled())) {
            SalesforceSdkInitializer salesforceSdkInitializer = new SalesforceSdkInitializer(this);
            salesforceSdkInitializer.initialize(mInstituteSettings);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && requestCode == RequestCode.PERMISSION) {
            if (isNotificationPermissionGranted(permissions, grantResults)) {
                onNotificationPermissionGranted();
            }
        }
    }

    private boolean isNotificationPermissionGranted(@NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (Manifest.permission.POST_NOTIFICATIONS.equals(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void onNotificationPermissionGranted() {
        if (Boolean.TRUE.equals(mInstituteSettings.getSalesforceSdkEnabled())) {
            SalesforceSdkInitializer.notificationPermissionGranted();
        }
    }
}

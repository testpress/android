package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UIUtils;
import in.testpress.testpress.util.UpdateAppDialogManager;

import static in.testpress.testpress.BuildConfig.ALLOW_ANONYMOUS_USER;
import static in.testpress.testpress.BuildConfig.BASE_URL;

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
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.grid) GridView grid;

    private int mSelectedItem;
    private BottomNavBarAdapter mBottomBarAdapter;
    private ArrayList<Integer> mMenuItemImageIds = new ArrayList<>();
    private ArrayList<Integer> mMenuItemTitleIds = new ArrayList<>();
    private ArrayList<Fragment> mMenuItemFragments = new ArrayList<>();
    private InstituteSettings mInstituteSettings;
    private InstituteSettingsDao instituteSettingsDao;
    private boolean isUserAuthenticated;

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
        SharedPreferences preferences =
                getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)) {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.makeGooglePlayServicesAvailable(this);
            CommonUtils.registerDevice(MainActivity.this, testpressService, serviceProvider);
        }
        addMenuItem(R.string.dashboard, R.drawable.profile_default, new MainMenuFragment());
        // Show courses list if game front end is enabled, otherwise hide bottom bar
        if (isUserAuthenticated && mInstituteSettings.getShowGameFrontend()) {
            //noinspection ConstantConditions
            addMenuItem(R.string.learn, R.drawable.learn,
                    TestpressCourse.getCoursesListFragment(this, TestpressSdk.getTestpressSession(this)));

            if (mInstituteSettings.getCoursesEnableGamification()) {
                //noinspection ConstantConditions
                addMenuItem(R.string.testpress_leaderboard, R.drawable.leaderboard,
                        TestpressCourse.getLeaderboardFragment(this, TestpressSdk.getTestpressSession(this)));
            }
            if (mInstituteSettings.getForumEnabled()) {
                addMenuItem(R.string.discussions, R.drawable.chat_icon, new ForumListFragment());
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

        if (UIUtils.getMenuItemName(mMenuItemTitleIds.get(position), mInstituteSettings) != "") {
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
                }
            }
        }.execute();
    }

    public void onFinishFetchingInstituteSettings(InstituteSettings instituteSettings) {
        this.mInstituteSettings = instituteSettings;
        isUserAuthenticated = CommonUtils.isUserAuthenticated(this);
        //noinspection ConstantConditions
        if (!isUserAuthenticated && !ALLOW_ANONYMOUS_USER) {
            // Show login screen if user not logged in else update institute settings in TestpressSDK
            updateTestpressSession();
        } else {
            initScreen();
            if (isUserAuthenticated) {
                updateTestpressSession();
            }
        }
    }

    private void checkUpdate() {
        if (mInstituteSettings == null) {
            progressBarLayout.setVisibility(View.VISIBLE);
        }
        new SafeAsyncTask<Update>() {
            @Override
            public Update call() {
                return testpressService.checkUpdate("" + BuildConfig.VERSION_CODE);
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

}

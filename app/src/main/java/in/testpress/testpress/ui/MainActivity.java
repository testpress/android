package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

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
import in.testpress.testpress.authenticator.RegistrationIntentService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MainActivity extends TestpressFragmentActivity {

    private static final String SELECTED_ITEM = "selectedItem";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

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

    private BroadcastReceiver mRegistrationBroadcastReceiver;
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
//        mMenuItemFragments.clear();
//        mMenuItemImageIds.clear();
//        mMenuItemTitleIds.clear();
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM);
        }
        viewPager.setVisibility(View.VISIBLE);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CommonUtils.registerDevice(MainActivity.this, testpressService, serviceProvider);
            }
        };
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        instituteSettingsDao = daoSession.getInstituteSettingsDao();

//        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
//                .where(InstituteSettingsDao.Properties.BaseUrl.eq(Constants.Http.URL_BASE))
//                .list();
//
//        if (instituteSettingsList.size() > 0) {
//            Log.e("Institute settings", "found");
//            this.mInstituteSettings = instituteSettingsList.get(0);
//            initScreen();
//        }
        Log.e("Institute settings", "not found");

        fetchInstituteSettings();
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
                checkUpdate();
            }
        }.execute();
    }

    private void initScreen() {
        SharedPreferences preferences =
                getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)) {
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
                startService(intent);
            }
        }
        Log.e("Checking for gamified","");
        // Show courses list if game front end is enabled, otherwise hide bottom bar
        if (isUserAuthenticated && mInstituteSettings.getShowGameFrontend()) {
            Log.e("Checking for gamified","true found");
            //noinspection ConstantConditions
            addMenuItem(R.string.learn, R.drawable.learn,
                    TestpressCourse.getCoursesListFragment(this, TestpressSdk.getTestpressSession(this)));

            if (mInstituteSettings.getCoursesEnableGamification()) {
                //noinspection ConstantConditions
                addMenuItem(R.string.testpress_leaderboard, R.drawable.leaderboard,
                        TestpressCourse.getLeaderboardFragment(this, TestpressSdk.getTestpressSession(this)));
            }
        } else {
            grid.setVisibility(View.GONE);
        }
        addMenuItem(R.string.articles, R.drawable.news, new PostsListFragment());
        addMenuItem(R.string.discussions, R.drawable.chat_icon, new ForumListFragment());
        addMenuItem(R.string.app_name, R.drawable.profile_default, new MainMenuFragment());
        Log.e("No. of items", mMenuItemImageIds.size()+"");
        mBottomBarAdapter = new BottomNavBarAdapter(this, mMenuItemImageIds,
                mMenuItemTitleIds);
        Log.e("No. of adapt", mBottomBarAdapter.getCount()+"");
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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        viewPager.setCurrentItem(mSelectedItem);
        viewPager.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }

    private void onItemSelected(int position) {
        mSelectedItem = position;
        mBottomBarAdapter.setSelectedPosition(position);
        mBottomBarAdapter.notifyDataSetChanged();
        updateToolbarText(getString(mMenuItemTitleIds.get(position)));
        if (!CommonUtils.isUserAuthenticated(this)) {
            serviceProvider.logout(this, testpressService, serviceProvider, logoutService);
        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    private void fetchInstituteSettings() {
        new SafeAsyncTask<InstituteSettings>() {
            @Override
            public InstituteSettings call() throws Exception {
                return testpressService.getInstituteSettings();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                        .where(InstituteSettingsDao.Properties.BaseUrl.eq(Constants.Http.URL_BASE))
                        .list();

                if (instituteSettingsList.size() > 0) {
                    onFinishFetchingInstituteSettings(instituteSettingsList.get(0));
                    return;
                }
                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
//                progressBarLayout.setVisibility(View.GONE);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        fetchInstituteSettings();
                    }
                });
            }

            @Override
            protected void onSuccess(InstituteSettings instituteSettings) throws Exception {
                instituteSettings.setBaseUrl(Constants.Http.URL_BASE);
                instituteSettingsDao.insertOrReplace(instituteSettings);
                onFinishFetchingInstituteSettings(instituteSettings);
            }
        }.execute();
    }

    public void onFinishFetchingInstituteSettings(InstituteSettings instituteSettings) {
        this.mInstituteSettings = instituteSettings;
        // TODO: Get allowAnonymousUser flag from institute settings
        boolean allowAnonymousUser = false; // True if users can use the app(Access posts) without login
        //noinspection ConstantConditions
        if (CommonUtils.isUserAuthenticated(this) || !allowAnonymousUser) {
            updateTestpressSession(); // Show login screen if user not logged in
        } else {
            checkUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMPreference.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void checkUpdate() {
        new SafeAsyncTask<Update>() {
            @Override
            public Update call() throws Exception {
                return testpressService.checkUpdate("" + BuildConfig.VERSION_CODE);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                initScreen();
            }

            @Override
            protected void onSuccess(final Update update) throws Exception {
                if(update.getUpdateRequired()) {
                    if (update.getForce()) {
                        new MaterialDialog.Builder(MainActivity.this)
                                .cancelable(true)
                                .content(update.getMessage())
                                .cancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        finish();
                                    }
                                })
                                .neutralText("Update")
                                .buttonsGravity(GravityEnum.CENTER)
                                .neutralColorRes(R.color.primary)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onNeutral(MaterialDialog dialog) {
                                        dialog.cancel();
                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=" + getPackageName())));
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        initScreen();
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "New update is available",
                                        Snackbar.LENGTH_INDEFINITE)
                                .setAction("UPDATE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=" + getPackageName())));
                                        finish();
                                    }
                                });
                        snackbar.show();
                    }
                } else {
                    initScreen();
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

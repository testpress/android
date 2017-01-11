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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;

import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.exam.TestpressExam;
import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.authenticator.RegistrationIntentService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

public class MainActivity extends TestpressFragmentActivity {

    private static final String SELECTED_ITEM = "selectedItem";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    @InjectView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @InjectView(R.id.progressbar) RelativeLayout progressBarLayout;
    @InjectView(R.id.container) FrameLayout fragmentContainer;
    @InjectView(R.id.grid) GridView grid;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private MainMenuFragment mMainMenuFragment;
    private int mSelectedItem;
    private BottomNavBarAdapter mAdapter;
    private int[] mMenuItemImageId = {
            R.drawable.learn,
            R.drawable.news,
            R.drawable.profile_default,
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM);
        }
        fragmentContainer.setVisibility(View.INVISIBLE);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CommonUtils.registerDevice(MainActivity.this, testpressService, serviceProvider);
            }
        };
        checkAuth();
    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
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
        mAdapter = new BottomNavBarAdapter(this, mMenuItemImageId);
        grid.setAdapter(mAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFragment(position);
            }
        });
        mMainMenuFragment = new MainMenuFragment();
        selectFragment(mSelectedItem);
    }

    private void selectFragment(int position) {
        mSelectedItem = position;
        mAdapter.setSelectedPosition(position);
        mAdapter.notifyDataSetChanged();
        if (!CommonUtils.isUserAuthenticated(this)) {
            serviceProvider.logout(this, testpressService, serviceProvider, logoutService);
            return;
        }
        switch (position) {
            case 0:
                updateToolbarText(getString(R.string.learn));
                initSDK(position);
                break;
            case 1:
                updateToolbarText(getString(R.string.testpress_exams));
                initSDK(position);
                break;
            case 2:
                updateToolbarText(getString(R.string.app_name));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, mMainMenuFragment)
                        .commitAllowingStateLoss();
                fragmentContainer.setVisibility(View.VISIBLE);
                progressBarLayout.setVisibility(View.GONE);
                break;
        }
    }

    void initSDK(final int position) {
        if (TestpressSdk.hasActiveSession(this)) {
            showSDK(position);
        } else {
            new SafeAsyncTask<Void>() {
                @Override
                public Void call() throws Exception {
                    serviceProvider.getService(MainActivity.this);
                    return null;
                }

                @Override
                protected void onSuccess(Void aVoid) throws Exception {
                    showSDK(position);
                }
            }.execute();
        }
    }

    @SuppressWarnings("ConstantConditions")
    void showSDK(int position) {
        if (position == 0) {
            TestpressCourse.show(this, R.id.container, TestpressSdk.getTestpressSession(this));
        } else {
            TestpressExam.show(this, R.id.container, TestpressSdk.getTestpressSession(this));
        }
        fragmentContainer.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.GONE);
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
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
}

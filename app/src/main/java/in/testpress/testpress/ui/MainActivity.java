package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link in.testpress.testpress.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends TestpressFragmentActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    protected RelativeLayout progressBarLayout;
    private boolean userHasAuthenticated = false;
    private MainMenuFragment fragment;
    private SharedPreferences gcmPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    private void initScreen() {
        gcmPreferences = getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (!gcmPreferences.getBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false)) {
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
                startService(intent);
            }
        }
        fragment = new MainMenuFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
        progressBarLayout.setVisibility(View.GONE);
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

    private void registerDevice() {
        new SafeAsyncTask<Device>() {
            @Override
            public Device call() throws Exception {
                String token = GCMPreference.getRegistrationId(MainActivity.this.getApplicationContext());
                AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                Account[] account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                if (account.length > 0) {
                    testpressService = serviceProvider.getService(MainActivity.this);
                }
                return testpressService.register(token, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
            }

            @Override
            protected void onSuccess(final Device device) throws Exception {
                gcmPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }.execute();
    }

    private void updateDevice() {
        new SafeAsyncTask<Device>() {
            @Override
            public Device call() throws Exception {
                String token = GCMPreference.getRegistrationId(MainActivity.this.getApplicationContext());
                return testpressService.register(token, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
            }

            @Override
            protected void onSuccess(final Device device) throws Exception {
                gcmPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }.execute();
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
                        final CoordinatorLayout coordinatorLayout =
                                (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "New update is available", Snackbar.LENGTH_INDEFINITE)
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
        new MaterialDialog.Builder(this)
                .title("Log Out")
                .content("Are you sure you want to log out?")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        final MaterialDialog materialDialog = new MaterialDialog.Builder(MainActivity.this)
                                .title(R.string.label_logging_out)
                                .content(R.string.please_wait)
                                .widgetColorRes(R.color.primary)
                                .progress(true, 0)
                                .show();
                        testpressService.invalidateAuthToken();
                        serviceProvider.invalidateAuthToken();
                        gcmPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
                        updateDevice();
                        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
                        PostDao postDao = daoSession.getPostDao();
                        postDao.deleteAll();
                        daoSession.clear();
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        logoutService.logout(new Runnable() {
                            @Override
                            public void run() {
                                // Calling a checkAuth will force the service to look for a logged in user
                                // and when it finds none the user will be requested to log in again.
                                Intent intent = MainActivity.this.getIntent();
                                materialDialog.dismiss();
                                MainActivity.this.finish();
                                MainActivity.this.startActivity(intent);
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    public void onNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("url"))
            {
                Intent newintent = new Intent(this, PostActivity.class);
                newintent.putExtra("url", intent.getStringExtra("url"));
                startActivity(newintent);
                finish();
            }
        } else {
            setContentView(R.layout.main_activity);
            progressBarLayout = (RelativeLayout) findViewById(R.id.progressbar);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.pb_loading);
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    registerDevice();
                }
            };
            checkUpdate();
        }
    }
}

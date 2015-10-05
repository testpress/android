package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UIUtils;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link in.testpress.testpress.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends TestpressFragmentActivity {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    protected RelativeLayout progressBarLayout;
    private boolean userHasAuthenticated = false;
    private MainMenuFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);

        if(isTablet()) {
            setContentView(R.layout.main_activity_tablet);
        } else {
            setContentView(R.layout.main_activity);
        }
        progressBarLayout = (RelativeLayout) findViewById(R.id.progressbar);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        checkUpdate();
    }

    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }

    private void initScreen() {
        if (userHasAuthenticated) {
            fragment = new MainMenuFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss();
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final TestpressService svc = serviceProvider.getService(MainActivity.this);
                return svc != null;
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
                userHasAuthenticated = true;
                initScreen();
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
                checkAuth();
            }

            @Override
            protected void onSuccess(final Update update) throws Exception {
                if(update.getUpdateRequired()) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .cancelable(true)
                            .content(update.getMessage())
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    if (update.getForce()) {
                                        finish();
                                    } else {
                                        checkAuth();
                                    }
                                }
                            })
                            .neutralText("Update")
                            .buttonsGravity(GravityEnum.CENTER)
                            .neutralColorRes(R.color.primary)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                    dialog.cancel();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "in.testpress.testpress")));
                                    //Should change "in.testpress.testpress" to "in.testpress.<App name>" for different apps
                                    finish();
                                }
                            })
                            .show();
                } else checkAuth();
            }
        }.execute();
    }

    public void logout() {
        new MaterialDialog.Builder(this)
                .title("Do you really want to Logout?")
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
                        serviceProvider.invalidateAuthToken();
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
}

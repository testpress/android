

package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import br.liveo.Model.HelpLiveo;
import br.liveo.interfaces.OnItemClickListener;
import br.liveo.navigationliveo.NavigationLiveo;
import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Update;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link in.testpress.testpress.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends NavigationLiveo implements OnItemClickListener {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected TestpressService testpressService;
    @Inject protected LogoutService logoutService;

    private boolean userHasAuthenticated = false;
    private MaterialDialog materialDialog;
    private CarouselFragment fragment;

    private void initScreen() {
        if (userHasAuthenticated) {

            final Intent intent = getIntent();
            Bundle data = intent.getExtras();
            fragment = new CarouselFragment();
            fragment.setArguments(data);
            Ln.d("Foo");
            final FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss();
        }

    }

    private void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final TestpressService svc = serviceProvider.getService(MainActivity.this);
                if(materialDialog.isShowing())
                    materialDialog.dismiss();
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
                initUser();
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
                    materialDialog = new MaterialDialog.Builder(MainActivity.this)
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onInt(Bundle savedInstanceState) {
        HelpLiveo mHelpLiveo;;
        Injector.inject(this);
        checkUpdate();
        materialDialog = new MaterialDialog.Builder(this).build();
        this.userBackground. setImageResource(R.drawable.blue);

        // Creating navigation items
        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add(getString(R.string.home), R.drawable.house_icon);
        mHelpLiveo.add(getString(R.string.products), R.drawable.cart);
        mHelpLiveo.add(getString(R.string.logout), R.drawable.logout);
        with(this)
                .startingPosition(0) //Starting position in the list
                .addAllHelpItem(mHelpLiveo.getHelp())
                .build();
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                initScreen();
                break;
            case 1:
                ProductNativeGridBaseFragment productsListFragment = new ProductNativeGridBaseFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, productsListFragment).commitAllowingStateLoss();
                break;
            case 2:
                logout();
                break;
        }
    }

    void initUser(){
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        this.userName.setText(accounts[0].name);
        this.userPhoto.setImageResource(R.drawable.profile_icon);
    }

    void logout() {
        materialDialog = new MaterialDialog.Builder(this)
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
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                checkAuth();
            }
        });
    }

}

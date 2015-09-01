package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;

import br.liveo.Model.HelpLiveo;
import br.liveo.interfaces.OnItemClickListener;
import br.liveo.navigationliveo.NavigationLiveo;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.util.SafeAsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

public abstract class NavigationDrawer extends NavigationLiveo implements OnItemClickListener {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    protected boolean userHasAuthenticated = false;
    protected HelpLiveo mHelpLiveo;

    protected void checkAuth() {
        new SafeAsyncTask<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final TestpressService svc = serviceProvider.getService(NavigationDrawer.this);
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

    @Override
    public void onInt(Bundle savedInstanceState) {
        Injector.inject(this);
        this.userBackground.setImageResource(R.drawable.blue);

        // Creating navigation items
        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add(getString(R.string.home), R.drawable.house_icon);
        mHelpLiveo.add(getString(R.string.products), R.drawable.cart);
        mHelpLiveo.add(getString(R.string.orders), R.drawable.cart);
        mHelpLiveo.add(getString(R.string.logout), R.drawable.logout);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent;
        switch (position) {
            case 0:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(this, ProductsListActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(this, OrderListActivity.class);
                startActivity(intent);
                break;
            case 3:
                logout();
                break;
        }
    }

    protected void initUser(){
//        this.userName.setText("testpress");
        this.userPhoto.setImageResource(R.drawable.profile_icon);
    }

    protected void logout() {
        final MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.label_logging_out)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .show();
        serviceProvider.invalidateAuthToken();
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling MainActivity will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                materialDialog.dismiss();
                startActivity(intent);
                finish();
            }
        });
    }
    protected abstract void initScreen();
}

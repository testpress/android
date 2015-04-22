

package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import butterknife.OnClick;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.NavItemSelectedEvent;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.UIUtils;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;


/**
 * Initial activity for the application.
 *
 * If you need to remove the authentication from the application please see
 * {@link in.testpress.testpress.authenticator.ApiKeyProvider#getAuthKey(android.app.Activity)}
 */
public class MainActivity extends TestpressFragmentActivity {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;


    private boolean userHasAuthenticated = false;

    private CharSequence title;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        if(isTablet()) {
            setContentView(R.layout.main_activity_tablet);
        } else {
            setContentView(R.layout.main_activity);
        }

        // View injection with Butterknife
        ButterKnife.inject(this);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(isConnectingToInternet()) {
            checkAuth();
        }

    }

    private boolean isTablet() {
        return UIUtils.isTablet(this);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void initScreen() {
        if (userHasAuthenticated) {

            final Intent intent = getIntent();
            Bundle data = intent.getExtras();
            CarouselFragment fragment = new CarouselFragment();
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //menuDrawer.toggleMenu();
                return true;
            case R.id.logout:
                serviceProvider.invalidateAuthToken();
                logoutService.logout(new Runnable() {
                    @Override
                    public void run() {
                        // Calling a refresh will force the service to look for a logged in user
                        // and when it finds none the user will be requested to log in again.
                        checkAuth();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onNavigationItemSelected(NavItemSelectedEvent event) {

        Ln.d("Selected: %1$s", event.getItemPosition());

        switch(event.getItemPosition()) {
            case 0:
                // Home
                // do nothing as we're already on the home screen.
                break;
        }
    }

    @OnClick (R.id.retry) public void retry() {
        if(isConnectingToInternet()) {
            checkAuth();
        }
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}

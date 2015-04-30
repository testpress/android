package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.authenticator.TestpressAuthenticatorActivity;


public class ReviewActivity extends TestpressFragmentActivity {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_review);
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, reviewFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                serviceProvider.invalidateAuthToken();
                logoutService.logout(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

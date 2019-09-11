package in.testpress.testpress.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import in.testpress.testpress.Injector;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.events.CustomErrorEvent;
import in.testpress.ui.UserDevicesActivity;


/**
 * Base class for all Testpress Activities that need fragments.
 */
public class TestpressFragmentActivity extends AppCompatActivity {

    @Inject
    protected Bus eventBus;

    protected Toolbar mActionBarToolbar;
    protected Object busEventListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        // Directly subscribing in parent class won't work, only child class subscribers will work. https://github.com/square/otto/issues/26
        busEventListener = new Object() {
            @Subscribe
            public void onCustomErrorEvent(CustomErrorEvent customErrorEvent) {
                TestpressFragmentActivity.this.onReceiveCustomErrorEvent(customErrorEvent);
            }
        };
        eventBus.register(busEventListener);
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);

        ButterKnife.inject(this);
        Toolbar toolbar = getActionBarToolbar();
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                onBackPressed();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (IllegalStateException e) {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    protected void onReceiveCustomErrorEvent(final CustomErrorEvent customErrorEvent) {
        if (customErrorEvent.getErrorCode().equals("parallel_login_restriction")) {
            Intent intent = new Intent(this, UserDevicesActivity.class);
            startActivity(intent);
        }
    }
}

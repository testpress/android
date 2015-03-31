package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import in.testpress.testpress.Injector;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;


/**
 * Base class for all Testpress Activities that need fragments.
 */
public class TestpressFragmentActivity extends ActionBarActivity {

    @Inject
    protected Bus eventBus;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.inject(this);
    }

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);

        ButterKnife.inject(this);
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
}

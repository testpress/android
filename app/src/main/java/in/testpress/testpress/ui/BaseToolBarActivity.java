package in.testpress.testpress.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.testpress.core.Constants;

import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
/**
 * Base activity used to support the toolbar_material & handle backpress.
 * Activity that extends this activity must needs to include the #layout/toolbar_material
 * in its view.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity {

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);
        TestpressSession session = TestpressSdk.getTestpressSession(this);

        if (session != null && session.getInstituteSettings().isScreenshotDisabled()) {
            getWindow().setFlags(FLAG_SECURE, FLAG_SECURE);
        }
        Toolbar toolbar = (Toolbar) findViewById(in.testpress.R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}

package in.testpress.testpress.ui;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Base activity used to support the toolbar_material & handle backpress.
 * Activity that extends this activity must needs to include the #layout/toolbar_material
 * in its view.
 */
public abstract class BaseToolBarActivity extends AppCompatActivity {

    @Override
    public void setContentView(final int layoutResId) {
        super.setContentView(layoutResId);
        Toolbar toolbar = (Toolbar) findViewById(in.testpress.R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}

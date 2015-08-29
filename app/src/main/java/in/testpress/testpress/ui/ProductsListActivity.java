package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import in.testpress.testpress.R;

public class ProductsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ProductNativeGridBaseFragment productsListFragment = new ProductNativeGridBaseFragment();
        Toolbar toolbar = getActionBarToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Products");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, productsListFragment).commitAllowingStateLoss();
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
}

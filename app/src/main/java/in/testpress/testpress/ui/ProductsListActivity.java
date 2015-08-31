package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import in.testpress.testpress.R;

public class ProductsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ProductNativeGridBaseFragment productsListFragment = new ProductNativeGridBaseFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, productsListFragment).commitAllowingStateLoss();
    }
}

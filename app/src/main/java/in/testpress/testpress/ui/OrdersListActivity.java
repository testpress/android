package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class OrdersListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        OrdersListFragment ordersListFragment = new OrdersListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ordersListFragment).commitAllowingStateLoss();

        if (getIntent().getStringExtra("title") != "") {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }
    }
}

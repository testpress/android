package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class PostsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PostsListFragment ordersListFragment = new PostsListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ordersListFragment).commitAllowingStateLoss();
    }
}

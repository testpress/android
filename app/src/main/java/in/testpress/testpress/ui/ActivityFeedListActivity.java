package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class ActivityFeedListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActivityFeedListFragment activityFeedListFragment = new ActivityFeedListFragment();
        Bundle bundle = getIntent().getExtras();
        activityFeedListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, activityFeedListFragment).commitAllowingStateLoss();
    }
}
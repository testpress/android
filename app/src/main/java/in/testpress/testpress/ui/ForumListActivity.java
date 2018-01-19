package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class ForumListActivity extends TestpressFragmentActivity{

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ForumListFragment postsListFragment = new ForumListFragment();
        Bundle bundle = getIntent().getExtras();
        postsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, postsListFragment).commitAllowingStateLoss();
    }

}

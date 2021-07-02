package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;
import in.testpress.testpress.ui.fragments.DiscussionFragmentv2;
import in.testpress.ui.BaseToolBarActivity;


public class ForumListActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DiscussionFragmentv2 postsListFragment = new DiscussionFragmentv2();
        Bundle bundle = getIntent().getExtras();
        postsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, postsListFragment).commitAllowingStateLoss();
    }

}

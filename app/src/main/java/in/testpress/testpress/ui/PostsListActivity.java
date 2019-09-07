package in.testpress.testpress.ui;

import android.os.Bundle;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.events.CustomErrorEvent;
import in.testpress.testpress.util.CommonUtils;

public class PostsListActivity extends TestpressFragmentActivity {

    @Inject
    Bus bus;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PostsListFragment postsListFragment = new PostsListFragment();
        Bundle bundle = getIntent().getExtras();
        postsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, postsListFragment).commitAllowingStateLoss();

        if (getIntent().getStringExtra("title") != "") {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }


    @Subscribe
    public void onCustomErrorEvent(CustomErrorEvent customErrorEvent) {
        CommonUtils.showAlert(this, "Parallel Login Restriction", customErrorEvent.getDetail());
    }

}

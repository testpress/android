package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import in.testpress.testpress.R;

public class PostsListActivity extends TestpressFragmentActivity {
    boolean fromPostDetail;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fromPostDetail = getIntent().getBooleanExtra("parentIsNotification", false);
        PostsListFragment postsListFragment = new PostsListFragment();
        Bundle bundle = new Bundle();
        if(fromPostDetail) {
            bundle.putBoolean("parentIsNotification", true);
        } else {
            bundle.putBoolean("parentIsNotification", false);
        }
        postsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, postsListFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if(fromPostDetail) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }

    }
}

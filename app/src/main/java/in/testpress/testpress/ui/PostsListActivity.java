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
        PostsListFragment ordersListFragment = new PostsListFragment();
        Bundle bundle = new Bundle();
        if(fromPostDetail) {
            bundle.putBoolean("parentIsNotification", true);
        } else {
            bundle.putBoolean("parentIsNotification", false);
        }
        ordersListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ordersListFragment).commitAllowingStateLoss();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
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

package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;

import in.testpress.testpress.R;

public class ExamsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CarouselFragment fragment = new CarouselFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getBooleanExtra("isDeepLink", false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
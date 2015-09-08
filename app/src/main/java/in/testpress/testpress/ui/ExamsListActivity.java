package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class ExamsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CarouselFragment fragment = new CarouselFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }
}
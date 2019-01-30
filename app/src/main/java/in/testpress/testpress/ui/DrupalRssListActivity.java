package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class DrupalRssListActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout_material);
        DrupalRssListFragment fragment = new DrupalRssListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}

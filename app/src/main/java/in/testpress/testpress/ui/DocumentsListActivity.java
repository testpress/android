package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;
import in.testpress.testpress.util.CommonUtils;

public class DocumentsListActivity extends BaseAuthenticatedActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout_material);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("title") != "") {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }

        if (CommonUtils.isUserAuthenticated(this)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DocumentsListFragment())
                    .commitAllowingStateLoss();
        }
    }

}

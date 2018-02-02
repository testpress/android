package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;
import in.testpress.testpress.util.CommonUtils;

public class FormListActivity extends BaseAuthenticatedActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout_material);
        ApplicationFormFragment fragment = new ApplicationFormFragment();
        fragment.setArguments(getIntent().getExtras());
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (CommonUtils.isUserAuthenticated(this)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        }
    }

}

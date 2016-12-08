package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Subject;

import static in.testpress.testpress.ui.AnalyticsFragment.SUBJECT;

public class AnalyticsActivity extends BaseToolBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout_material);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AnalyticsFragment analyticsFragment = new AnalyticsFragment();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            analyticsFragment.setArguments(bundle);
            Subject subject = bundle.getParcelable(SUBJECT);
            if (subject != null) {
                getSupportActionBar().setTitle(subject.getName());
            }
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, analyticsFragment).commitAllowingStateLoss();
    }

}

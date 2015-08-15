package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Exam;


public class AttemptsListActivity extends TestpressFragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        AttemptsListFragment attemptsListFragment = new AttemptsListFragment();
        Bundle bundle = getIntent().getExtras();
        Toolbar toolbar = getActionBarToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Exam exam = bundle.getParcelable("exam");
        getSupportActionBar().setTitle(exam.getTitle());
        attemptsListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, attemptsListFragment).commitAllowingStateLoss();
    }

    public Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }
}

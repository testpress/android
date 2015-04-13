package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import in.testpress.testpress.R;


public class ReviewActivity extends ActionBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        final Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, reviewFragment).commitAllowingStateLoss();
    }
}

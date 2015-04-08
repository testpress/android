package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import in.testpress.testpress.R;


public class ReviewActivity extends ActionBarActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ReviewFragment reviewFragment = new ReviewFragment();
        Bundle bundle = new Bundle();
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, reviewFragment).commitAllowingStateLoss();
    }
}

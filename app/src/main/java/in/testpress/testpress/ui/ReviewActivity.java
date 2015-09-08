package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;

public class ReviewActivity extends TestpressFragmentActivity {

    @Inject protected TestpressServiceProvider serviceProvider;
    String previousActivity;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        Injector.inject(this);
        final Intent intent = getIntent();
        previousActivity = intent.getStringExtra("previousActivity");
        Bundle bundle = intent.getExtras();
        ReviewFragment reviewFragment = new ReviewFragment();
        reviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, reviewFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if((previousActivity != null) && previousActivity.equals("ExamActivity")) {
            //onBackPressed go to history
            Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("currentItem", "2");
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}

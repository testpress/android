package in.testpress.testpress.ui;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import in.testpress.testpress.R;
import in.testpress.testpress.R.id;


public class StartExamActivity extends TestpressFragmentActivity {
    Button startExam,previous,next;
    ProgressDialog progress;
    private TestpressViewPager pager;

    StartExamPagerAdapter pagerAdapter;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_exam);
        startExam=(Button)findViewById(id.start_exam);
        previous=(Button)findViewById(id.previous);
        next=(Button)findViewById(id.next);
        pager = (TestpressViewPager) findViewById(R.id.pager);

        progress= new ProgressDialog(this);
        startExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.show();
                FragmentManager startExamFragmentManager = getSupportFragmentManager();
                pagerAdapter = new StartExamPagerAdapter(startExamFragmentManager);
                pager.setAdapter(pagerAdapter);
                pager.setPagingEnabled(false);
                startExam.setVisibility(View.INVISIBLE);
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager.getCurrentItem() != 0) {
                    pager.setCurrentItem(pager.getCurrentItem()-1);
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pager.getCurrentItem() != pager.getChildCount()) {
                    pager.setCurrentItem(pager.getCurrentItem()+1);
                }
            }
        });

    }


}



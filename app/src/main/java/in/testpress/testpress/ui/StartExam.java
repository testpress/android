package in.testpress.testpress.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import in.testpress.testpress.R;
import in.testpress.testpress.R.id;


public class StartExam extends Activity {
    Button StartExam;
    ProgressDialog progress;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_exam);
        StartExam=(Button)findViewById(id.start_exam);
        progress= new ProgressDialog(this);
        StartExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.show();

            }
        });

    }
}



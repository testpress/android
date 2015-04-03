package in.testpress.testpress.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;

import javax.inject.Inject;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.UserExam;
import retrofit.RetrofitError;

public class ExamActivity extends TestpressFragmentActivity implements LoaderManager.LoaderCallbacks<UserExam>  {
    @Inject protected TestpressServiceProvider serviceProvider;

    String examId, questionsUrl;
    protected UserExam userExam = null;
    protected ProgressBar progressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);

        final Intent intent = getIntent();
        examId = intent.getStringExtra("examId");

        getSupportLoaderManager().initLoader(0, null, this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<UserExam> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<UserExam>(ExamActivity.this, userExam) {
            @Override
            public UserExam loadData() throws Exception {
                try {
                    return serviceProvider.getService(ExamActivity.this).getUserExam(examId);
                } catch (RetrofitError retrofitError) {
                    return null;

                }

            }

        };
    }

    public void onLoadFinished(final Loader<UserExam> loader, final UserExam userExam) {
        progressBar.setVisibility(View.INVISIBLE);
        if(userExam != null) {
            questionsUrl = userExam.getQuestionsUrl();
            UserExamFragment userExamFragment = new UserExamFragment();
            Bundle bundle = new Bundle();
            bundle.putString("questionsUrl", questionsUrl);
            userExamFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, userExamFragment).commitAllowingStateLoss();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExamActivity.this);
            builder.setMessage("Server Error");
            builder.setCancelable(true);
            builder.setNeutralButton("ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void onLoaderReset(final Loader<UserExam> loader) {

    }

}

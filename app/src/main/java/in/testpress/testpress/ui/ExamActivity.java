package in.testpress.testpress.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.IconTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.InternetConnectivityChecker;
import retrofit.RetrofitError;

public class ExamActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Attempt>  {
    @Inject protected TestpressServiceProvider serviceProvider;

    protected Exam exam = null;
    protected Attempt attempt = null;
    protected RelativeLayout progressBar;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
    private MaterialDialog materialDialog;
    @InjectView(R.id.exam_details) LinearLayout examDetailsContainer;
    @InjectView(R.id.start_exam) Button startExam;
    @InjectView(R.id.end_exam) Button endExam;
    @InjectView(R.id.resume_exam) Button resumeExam;
    @InjectView(R.id.exam_title) TextView examTitle;
    @InjectView(R.id.number_of_questions) TextView numberOfQuestions;
    @InjectView(R.id.exam_duration) TextView examDuration;
    @InjectView(R.id.mark_per_question) TextView markPerQuestion;
    @InjectView(R.id.negative_marks) TextView negativeMarks;
    @InjectView(R.id.attempt_actions) LinearLayout attemptActions;
    @InjectView(R.id.description) LinearLayout description;
    @InjectView(R.id.descriptionContent) TextView descriptionContent;
    @InjectView(R.id.fragment_container) LinearLayout fragmentContainer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        Injector.inject(this);
        ButterKnife.inject(this);
        progressBar = (RelativeLayout) findViewById(R.id.pb_loading);
        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        exam = data.getParcelable("exam");
        attempt = data.getParcelable("attempt");
        if (attempt != null) {
            attemptActions.setVisibility(View.VISIBLE);
        } else {
            startExam.setVisibility(View.VISIBLE);
        }
        examTitle.setText(exam.getTitle());
        numberOfQuestions.setText("" + exam.getNumberOfQuestions());
        examDuration.setText(exam.getDuration());
        markPerQuestion.setText(exam.getMarkPerQuestion());
        negativeMarks.setText(exam.getNegativeMarks());
        if ((exam.getDescription() != null) && !exam.getDescription().trim().isEmpty()) {
            description.setVisibility(View.VISIBLE);
            descriptionContent.setText(exam.getDescription());
        }
    }

    @OnClick(R.id.exam_back_button) void goBack() {
        super.onBackPressed();
    }

    @OnClick(R.id.start_exam) void startExam() {
        if(internetConnectivityChecker.isConnected()) {
            getSupportLoaderManager().initLoader(0, null, this);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @OnClick(R.id.end_exam) void endExam() {
        if(internetConnectivityChecker.isConnected()) {
            getSupportLoaderManager().initLoader(2, null, this);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @OnClick(R.id.resume_exam) void resumeExam() {
        if(internetConnectivityChecker.isConnected()) {
            getSupportLoaderManager().initLoader(1, null, this);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @Override
    public Loader<Attempt> onCreateLoader(final int id, final Bundle args) {
        return new ThrowableLoader<Attempt>(ExamActivity.this, attempt) {
            @Override
            public Attempt loadData() throws Exception {
                try {
                    switch (id) {
                        case 0:
                            return serviceProvider.getService(ExamActivity.this).createAttempt(exam.getAttemptsFrag());
                        case 1:
                            return serviceProvider.getService(ExamActivity.this).startAttempt(attempt.getStartUrlFrag());
                        case 2:
                            return serviceProvider.getService(ExamActivity.this).endAttempt(attempt.getEndUrlFrag());
                    }

                } catch (RetrofitError retrofitError) {
                    return null;
                }
                return null;
            }
        };
    }

    public void onLoadFinished(final Loader<Attempt> loader, final Attempt attempt) {
        progressBar.setVisibility(View.INVISIBLE);
        if(attempt != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            startExam.setVisibility(View.GONE);
            attemptActions.setVisibility(View.GONE);
            examDetailsContainer.setVisibility(View.GONE);
            ViewGroup layout = (ViewGroup) startExam.getParent();
            if(null != layout) //for safety only  as you are doing onClick
                layout.removeView(startExam);
            if (attempt.getState().equals("Running")) {
                AttemptFragment attemptFragment = new AttemptFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("attempt", attempt);
                bundle.putParcelable("exam", exam);
                attemptFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, attemptFragment).commitAllowingStateLoss();
            } else {
                //Show Review when end button pressed
                Intent intent = new Intent(this.getApplicationContext(), ReviewActivity.class);
                intent.putExtra("previousActivity", "ExamActivity");
                intent.putExtra("exam", exam);
                intent.putExtra("attempt", attempt);
                startActivity(intent);
                finish();
            }
        } else {
            materialDialog = new MaterialDialog.Builder(this)
                    .cancelable(true)
                    .title("Server Error")
                    .neutralText("ok")
                    .buttonsGravity(GravityEnum.CENTER)
                    .neutralColorRes(R.color.primary)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            dialog.cancel();
                            finish();
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onLoaderReset(final Loader<Attempt> loader) {

    }

    @Override
    public void onBackPressed() {
        AttemptFragment attemptFragment = null;
        try {
             attemptFragment = (AttemptFragment) getSupportFragmentManager().getFragments().get(0);
        }
        catch (Exception e) {
        }
        if(attemptFragment != null) {
            if (attemptFragment.mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                attemptFragment.collapsePanel();
            }
            else
                attemptFragment.pauseExam();
        }
        else
            super.onBackPressed();
    }
}

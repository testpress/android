package in.testpress.testpress.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ProgressBar;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import retrofit.RetrofitError;

public class ExamActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Attempt>  {
    @Inject protected TestpressServiceProvider serviceProvider;

    protected Exam exam = null;
    protected Attempt attempt = null;
    protected ProgressBar progressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        Injector.inject(this);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);

        final Intent intent = getIntent();
        Bundle data = intent.getExtras();
        exam = data.getParcelable("exam");

        getSupportLoaderManager().initLoader(0, null, this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Attempt> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<Attempt>(ExamActivity.this, attempt) {
            @Override
            public Attempt loadData() throws Exception {
                try {
                    return serviceProvider.getService(ExamActivity.this).createAttempt(exam.getAttemptsFrag());
                } catch (RetrofitError retrofitError) {
                    return null;

                }

            }

        };
    }

    public void onLoadFinished(final Loader<Attempt> loader, final Attempt attempt) {
        progressBar.setVisibility(View.INVISIBLE);
        if(attempt != null) {
            AttemptFragment attemptFragment = new AttemptFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("attempt", attempt);
            attemptFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, attemptFragment).commitAllowingStateLoss();
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
    public void onLoaderReset(final Loader<Attempt> loader) {

    }

}

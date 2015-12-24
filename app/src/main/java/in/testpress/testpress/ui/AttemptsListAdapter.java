package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

import java.util.List;

import javax.inject.Inject;

public class AttemptsListAdapter extends SingleTypeAdapter<Attempt> {
    Activity activity;
    Exam exam;
    @Inject protected TestpressServiceProvider serviceProvider;

    /**
     * @param activity
     * @param items
     */
    public AttemptsListAdapter(final Activity activity, final List<Attempt> items, final Exam
            exam, int layout) {
        super(activity.getLayoutInflater(), layout);
        Injector.inject(this);
        this.activity = activity;
        this.exam = exam;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.completed_attempt, R.id.paused_attempt,
                R.id.attempt_date, R.id.percentile, R.id.correct_count, R.id.incorrect_count,
                R.id.score, R.id.paused_attempt_date, R.id.remaining_time};
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        final Attempt item = getItem(position);
        convertView.findViewById(R.id.review_attempt).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ReviewActivity.class);
                intent.putExtra("exam", exam);
                intent.putExtra("attempt", item);
                activity.startActivity(intent);
            }
        });

        convertView.findViewById(R.id.email_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(activity)
                        .title(R.string.mail_pdf_confirm_title)
                        .content(R.string.mail_pdf_confirm)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .positiveColorRes(R.color.primary)
                        .negativeColorRes(R.color.primary)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull
                            DialogAction which) {
                                Ln.e("OnPositive onClick");
                                final MaterialDialog progressDialog = new MaterialDialog.Builder(activity)
                                        .title(R.string.mail_pdf)
                                        .content(R.string.please_wait)
                                        .progress(true, 0)
                                        .progressIndeterminateStyle(true)
                                        .show();

                                new SafeAsyncTask<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        Ln.e("Inside call");
                                        serviceProvider.getService(activity).mailPdf(item
                                                .getUrlFrag() +
                                                Constants.Http.URL_MAIL_PDF_FRAG);
                                        return null;
                                    }

                                    @Override
                                    protected void onException(Exception e) {
                                        Ln.e("Exception");
                                        Ln.e(e.getMessage());
                                        progressDialog.dismiss();
                                        new MaterialDialog.Builder(activity)
                                                .title(R.string.mail_pdf_error)
                                                .content(R.string.mail_pdf_error_description)
                                                .positiveText(R.string.ok)
                                                .positiveColorRes(R.color.primary)
                                                .show();
                                    }

                                    @Override
                                    protected void onSuccess(Void arg) {
                                        progressDialog.dismiss();
                                        new MaterialDialog.Builder(activity)
                                                .title(R.string.mail_pdf_complete)
                                                .content(R.string.mail_pdf_complete_description)
                                                .positiveText(R.string.ok)
                                                .positiveColorRes(R.color.primary)
                                                .show();
                                    }

                                    @Override
                                    protected void onFinally() {
                                    }
                                }.execute();
                                Ln.e("Called AsyncTask execute");
                            }
                        })
                        .show();
            }
        });

        convertView.findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(activity)
                        .title(R.string.end_message)
                        .positiveText(R.string.end)
                        .negativeText(R.string.cancel)
                        .positiveColorRes(R.color.primary)
                        .negativeColorRes(R.color.primary)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull
                            DialogAction which) {
                                Intent intent = new Intent(activity, ExamActivity.class);
                                intent.putExtra("exam", exam);
                                intent.putExtra("attempt", item);
                                intent.putExtra("action", "end");
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        })
                        .show();
            }
        });

        convertView.findViewById(R.id.resume_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ExamActivity.class);
                intent.putExtra("exam", exam);
                intent.putExtra("attempt", item);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        if (item.getState().equals("Running")) {
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.VISIBLE);
            setText(7, "Started on " + item.getDate());
            setText(8, item.getRemainingTime());
        } else {
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.VISIBLE);
            setText(2, "Attempted on " + item.getDate());
            setText(3, item.getPercentile());
            setText(4, "" + item.getCorrectCount());
            setText(5, "" + item.getIncorrectCount());
            setText(6, "" + item.getScore());
        }

        return convertView;
    }


    @Override
    protected void update(final int position, final Attempt item) {
    }
}

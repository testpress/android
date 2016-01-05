package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

import java.util.List;

import javax.inject.Inject;

public class AvailableExamsListAdapter extends SingleTypeAdapter<Exam> {
    Activity activity;
    @Inject
    protected TestpressServiceProvider serviceProvider;

    public AvailableExamsListAdapter(final Activity activity, final List<Exam> items, int layout) {
        super(activity.getLayoutInflater(), layout);
        Injector.inject(this);
        this.activity = activity;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration,
                R.id.number_of_questions, R.id.exam_date};
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        final Exam exam = getItem(position);
        setText(0, exam.getTitle());
        setText(1, exam.getDuration());
        setText(2, exam.getNumberOfQuestionsString());
        setText(3, exam.getFormattedStartDate() + " to " + exam.getFormattedEndDate());
        ((TextView) (convertView.findViewById(R.id.course_category))).setText(exam
                .getCourse_category());
        convertView.findViewById(R.id.start_exam).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ExamActivity.class);
                intent.putExtra("exam", exam);
                activity.startActivity(intent);
            }
        });
        Button emailMcqs = (Button)convertView.findViewById(R.id.email_mcqs);
        emailMcqs.setOnClickListener(new View.OnClickListener() {
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
                                final MaterialDialog progressDialog = new MaterialDialog.Builder(activity)
                                        .title(R.string.mail_pdf)
                                        .content(R.string.please_wait)
                                        .progress(true, 0)
                                        .progressIndeterminateStyle(true)
                                        .show();

                                new SafeAsyncTask<Void>() {
                                    @Override
                                    public Void call() throws Exception {
                                        serviceProvider.getService(activity).mailPdf(exam
                                                .getUrlFrag() +
                                                Constants.Http.URL_MAIL_PDF_QUESTIONS_FRAG);
                                        return null;
                                    }

                                    @Override
                                    protected void onException(Exception e) {
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
                            }
                        })
                        .show();
            }
        });

        if (exam.getAllowPdf() == true) {
            emailMcqs.setVisibility(View.VISIBLE);
        } else {
            emailMcqs.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    protected void update(final int position, final Exam item) {
    }
}

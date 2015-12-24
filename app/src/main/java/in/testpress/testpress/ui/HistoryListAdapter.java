package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Exam;

import java.util.List;

public class HistoryListAdapter extends SingleTypeAdapter<Exam> {
    Activity activity;
    /**
     * @param activity
     * @param items
     */
    public HistoryListAdapter(final Activity activity, final List<Exam> items, int layout) {
        super(activity.getLayoutInflater(), layout);
        this.activity = activity;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration, R.id.number_of_questions,
                R.id.exam_date, R.id.attempts_count, R.id.attempts_string, R.id.retake,
                R.id.review_attempt, R.id.resume_exam};
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        final Exam exam = getItem(position);
        convertView.findViewById(R.id.review_attempt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exam.getAttemptsCount() == 1 && exam.getPausedAttemptsCount() == 0) {
                    Intent intent = new Intent(activity, ReviewActivity.class);
                    intent.putExtra("exam", exam);
                    activity.startActivity(intent);
                } else {
                    Intent intent = new Intent(activity, AttemptsListActivity.class);
                    intent.putExtra("exam", exam);
                    activity.startActivity(intent);
                }
            }
        });

        convertView.findViewById(R.id.retake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ExamActivity.class);
                intent.putExtra("exam", exam);
                activity.startActivity(intent);
            }
        });

        convertView.findViewById(R.id.resume_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AttemptsListActivity.class);
                intent.putExtra("exam", exam);
                intent.putExtra("state", "paused");
                activity.startActivity(intent);
            }
        });

        setText(0, exam.getTitle());
        setText(1, exam.getDuration());
        setText(2, exam.getNumberOfQuestionsString());
        setText(3, exam.getFormattedStartDate() + " to " + exam.getFormattedEndDate());
        setText(4, "" + exam.getAttemptsCount());
        setText(5, (exam.getAttemptsCount() > 1) ? "attempts" : "attempt");

        if (!exam.getAllowRetake()) {
            setGone(6, true);
        } else if (exam.getMaxRetakes() > 0 && exam.getAttemptsCount() >= exam.getMaxRetakes() + 1) {
            setGone(6, true);
        } else if (exam.getPausedAttemptsCount() > 0) {
            setGone(6, true);
        } else {
            setGone(6, false);
            setText(6, R.string.retake_action);
        }

        View retakeButton = convertView.findViewById(R.id.retake);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)retakeButton.getLayoutParams();
//        if (exam.getAttemptsCount() == exam.getPausedAttemptsCount()) {
//            setGone(7, true);
//            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            retakeButton.setLayoutParams(params); //causes layout update
//        } else {
//            setGone(7, false);
//            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
//            retakeButton.setLayoutParams(params); //causes layout update
//        }

        setGone(8, (exam.getPausedAttemptsCount() <= 0));
        return convertView;
    }

    @Override
    protected void update(final int position, final Exam item) {
    }
}

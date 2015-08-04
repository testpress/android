package in.testpress.testpress.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;

import java.util.List;

public class AttemptsListAdapter extends AlternatingColorListAdapter<Attempt> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public AttemptsListAdapter(final LayoutInflater inflater, final List<Attempt> items,
                            final boolean selectable, int layout) {
        super(layout, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public AttemptsListAdapter(final LayoutInflater inflater, final List<Attempt> items, int layout) {
        super(layout, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.completed_attempt, R.id.paused_attempt,
                R.id.attempt_date, R.id.percentile, R.id.correct_count, R.id.incorrect_count,
                R.id.paused_attempt_date, R.id.remaining_time };
    }

    @Override
    protected void update(final int position, final Attempt item) {
        super.update(position, item);
        if (item.getState().equals("Running")) {
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.VISIBLE);
            setText(6, item.getDate());
            setText(7, item.getRemainingTime());
        } else {
            updater.view.findViewById(R.id.paused_attempt).setVisibility(View.GONE);
            updater.view.findViewById(R.id.completed_attempt).setVisibility(View.VISIBLE);
            setText(2, item.getDate());
            setText(3, item.getPercentile());
            setText(4, "" + item.getCorrectCount());
            setText(5, "" + item.getIncorrectCount());
        }
    }
}

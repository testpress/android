package in.testpress.testpress.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Exam;

import java.util.List;

public class ExamsListAdapter extends AlternatingColorListAdapter<Exam> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public ExamsListAdapter(final LayoutInflater inflater, final List<Exam> items,
                           final boolean selectable, int layout) {
        super(layout, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public ExamsListAdapter(final LayoutInflater inflater, final List<Exam> items, int layout) {
        super(layout, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_duration,
                R.id.number_of_questions, R.id.exam_date};
    }

    @Override
    protected void update(final int position, final Exam item) {
        super.update(position, item);
        setText(0, item.getTitle());
        setText(1, item.getDuration());
        setText(2, item.getNumberOfQuestionsString());
        setText(3, item.getFormattedEndDate());
    }
}

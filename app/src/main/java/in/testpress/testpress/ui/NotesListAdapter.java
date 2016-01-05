package in.testpress.testpress.ui;

import android.view.LayoutInflater;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.Notes;

public class NotesListAdapter extends AlternatingColorListAdapter<Notes> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public NotesListAdapter(final LayoutInflater inflater, final List<Notes> items,
                            final boolean selectable, int layout) {
        super(layout, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public NotesListAdapter(final LayoutInflater inflater, final List<Notes> items, int layout) {
        super(layout, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.exam_title, R.id.exam_date,R.id.questionsText};
    }

    @Override
    protected void update(final int position, final Notes item) {
        super.update(position, item);
        setText(0, item.getTitle());
        setText(1, item.getDescription());
        setText(2, "");
    }
}

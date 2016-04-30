package in.testpress.testpress.ui;

import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.Notes;

public class NotesListAdapter extends SingleTypeAdapter<Notes> {
    /**
     * @param inflater
     * @param items
     */
    public NotesListAdapter(final LayoutInflater inflater, final List<Notes> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.description};
    }

    @Override
    protected void update(final int position, final Notes item) {
        setText(0, item.getTitle());
        if (item.getDescription().isEmpty()) {
            setText(1, "No description available");
        } else {
            setText(1, item.getDescription());
        }
    }
}

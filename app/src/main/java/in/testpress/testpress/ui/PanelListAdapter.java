package in.testpress.testpress.ui;

import android.view.LayoutInflater;

import java.util.List;

import in.testpress.testpress.R;


public class PanelListAdapter extends AlternatingColorListAdapter<String> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public PanelListAdapter(final LayoutInflater inflater, final List<String> items,
                            final boolean selectable, int layout) {
        super(layout, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public PanelListAdapter(final LayoutInflater inflater, final List<String> items, int layout) {
        super(layout, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.question, R.id.question_index};
    }

    @Override
    protected void update(final int position, final String item) {
        setText(0, trim(item, 0, item.length()));
        setNumber(1, position + 1);
    }

    public static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.subSequence(start, end);
    }
}
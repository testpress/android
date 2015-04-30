package in.testpress.testpress.ui;

import android.text.Html;
import android.view.LayoutInflater;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.AttemptItem;


public class PanelListAdapter extends AlternatingColorListAdapter<AttemptItem> {
    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public PanelListAdapter(final LayoutInflater inflater, final List<AttemptItem> items,
                            final boolean selectable, int layout) {
        super(layout, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public PanelListAdapter(final LayoutInflater inflater, final List<AttemptItem> items, int layout) {
        super(layout, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.question, R.id.question_index};
    }

    @Override
    protected void update(final int position, final AttemptItem item) {
        String question = Html.fromHtml(item.getAttemptQuestion().getQuestionHtml()).toString();
        setText(0, trim(question, 0, question.length()));
        setNumber(1, item.getIndex());
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
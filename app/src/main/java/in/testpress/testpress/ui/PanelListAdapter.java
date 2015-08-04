package in.testpress.testpress.ui;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

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
        return new int[]{R.id.question_all, R.id.question_marked, R.id.question_index_all, R.id.question_index_marked,
        R.id.question_answered, R.id.question_index_answered};
    }

    @Override
    protected void update(final int position, final AttemptItem item) {
        String question = Html.fromHtml(item.getAttemptQuestion().getQuestionHtml()).toString();
        try {
            if(item.getReview() || item.getCurrentReview()) {
                updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
                updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
                updater.view.findViewById(R.id.marked_question).setVisibility(View.VISIBLE);
                setNumber(3, item.getIndex());
                setText(1, trim(question, 0, question.length()));
            }
            else {
                setAnswer(item, question);
            }
        }
        catch (Exception e) {
            setAnswer(item, question);
        }
    }

    void setAnswer(AttemptItem item, String question) {
        if(!item.getSelectedAnswers().isEmpty() || !item.getSavedAnswers().isEmpty()) {
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.VISIBLE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.GONE);
            setNumber(5, item.getIndex());
            setText(4, trim(question, 0, question.length()));
        }
        else {
            updater.view.findViewById(R.id.marked_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.answered_question).setVisibility(View.GONE);
            updater.view.findViewById(R.id.all_question).setVisibility(View.VISIBLE);
            setNumber(2, item.getIndex());
            setText(0, trim(question, 0, question.length()));
        }
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
package in.testpress.testpress.ui;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptAnswer;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.ReviewAnswer;
import in.testpress.testpress.models.ReviewItem;

import java.util.ArrayList;
import java.util.List;

public class ReviewListAdapter extends SingleTypeAdapter<ReviewItem> {

    private LayoutInflater inflater;

    /**
     * @param inflater
     * @param items
     */
    public ReviewListAdapter(final int layoutId, final LayoutInflater inflater,
                             final List<ReviewItem> items) {
        super(inflater, layoutId);
        this.inflater = inflater;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.question, R.id.answer, R.id.explanation };
    }

    // http://stackoverflow.com/a/16745540/400236
    public static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    @Override
    protected void update(final int position, final ReviewItem item) {
        Spanned html = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""));
        setText(0, trim(html, 0, html.length()));
        String explanation = item.getReviewQuestion().getExplanationHtml().replaceAll("\n", "");
        if (explanation.equals("")) {
            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.GONE);
            updater.view.findViewById(R.id.explanation).setVisibility(View.GONE);
        } else {
            html = Html.fromHtml(explanation);
            setText(2, trim(html, 0, html.length()));
        }
        ((TextView)updater.view.findViewById(R.id.question_index)).setText((position + 1) + ".");
        LinearLayout correctAnswersView = (LinearLayout)updater.view.findViewById(R.id.correct_answer);
        //Clear all children first else it keeps appending old items
        correctAnswersView.removeAllViews();
        LinearLayout answersView = (LinearLayout)updater.view.findViewById(R.id.answer);
        //Clear all children first else it keeps appending old items
        answersView.removeAllViews();
        List<ReviewAnswer> answers = item.getReviewQuestion().getAnswers();
        for(int i = 0 ; i < answers.size() ; i++) {
            ReviewAnswer answer = answers.get(i);
            View option = inflater.inflate(R.layout.review_answer, null);
            html = Html.fromHtml(answer.getTextHtml());
            TextView answerText = (TextView) option.findViewById(R.id.answer_text);
            TextView optionText = (TextView) option.findViewById(R.id.option);
            optionText.setText("" + (char) (i + 97));
            answerText.setText(trim(html, 0, html.length()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            answersView.addView(option);
            option.setLayoutParams(params);
            if (item.getSelectedAnswers().contains(answer.getId()) == true) {
                if (answer.getIsCorrect() == true) {
                    optionText.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_green_background));
                } else {
                    optionText.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_red_background));
                }
            }
            if (answer.getIsCorrect() == true) {
                TextView correctOption = new TextView(inflater.getContext());
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(60, 60);
                textParams.setMargins(0, 0, 5, 0);
                correctOption.setLayoutParams(textParams);
                correctOption.setGravity(Gravity.CENTER);
                correctOption.setTextSize(18);
                correctOption.setTextColor(Color.parseColor("#FFFFFF"));
                correctOption.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_background));
                correctOption.setTypeface(Typeface.DEFAULT_BOLD);
                correctOption.setText("" + (char) (i + 97));
                correctOption.setVisibility(View.VISIBLE);
                correctAnswersView.addView(correctOption);
                Log.e("ReviewListAdapter", "ANSWER CORRECT");
            } else {
                Log.e("ReviewListAdapter", "ANSWER NOT CORRECT");
            }
        }
    }
}

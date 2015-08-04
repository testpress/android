package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import in.testpress.testpress.R;
import in.testpress.testpress.models.ReviewAnswer;
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;

import java.util.List;

public class ReviewListAdapter extends SingleTypeAdapter<ReviewItem> {

    private LayoutInflater inflater;
    Activity activity;

    /**
     * @param inflater
     * @param items
     */
    public ReviewListAdapter(final int layoutId, final LayoutInflater inflater,
                             final List<ReviewItem> items, Activity activity) {
        super(inflater, layoutId);
        this.inflater = inflater;
        setItems(items);
        this.activity = activity;
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.question, R.id.answer, R.id.explanation };
    }

    @Override
    protected void update(final int position, final ReviewItem item) {
        TextView questionsView = textView(0);
        Log.e("ReviewList", "Using new UILImageGetter " + questionsView.getHeight());
        Log.e("ReviewList", "Measured Height " + questionsView.getMeasuredHeight());
        Context c = this.activity.getApplicationContext();
        Spanned html = Html.fromHtml(item.getReviewQuestion().getQuestionHtml().replaceAll("\n", ""), new UILImageGetter(questionsView, c), null);
        ZoomableImageString zoomableImageHtml = new ZoomableImageString(activity);
        questionsView.setText(zoomableImageHtml.convertString(html), TextView.BufferType.SPANNABLE);
        questionsView.setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView)updater.view.findViewById(R.id.question_index)).setText((position + 1) + ".");
        LinearLayout correctAnswersView = (LinearLayout)updater.view.findViewById(R.id.correct_answer);
        //Clear all children first else it keeps appending old items
        correctAnswersView.removeAllViews();
        LinearLayout answersView = (LinearLayout)updater.view.findViewById(R.id.answer);
        //Clear all children first else it keeps appending old items
        answersView.removeAllViews();
        List<ReviewAnswer> answers = item.getReviewQuestion().getAnswers();
        for(int i = 0 ; i < answers.size() ; i++) {
            final ReviewAnswer answer = answers.get(i);
            View option = inflater.inflate(R.layout.review_answer, null);
            final TextView answerTextView = (TextView) option.findViewById(R.id.answer_text);
            html = Html.fromHtml(answer.getTextHtml(), new UILImageGetter(answerTextView, c), null);
            ZoomableImageString zoomableImageAnswer = new ZoomableImageString(activity);
            answerTextView.setText(zoomableImageAnswer.convertString(html), TextView.BufferType.SPANNABLE);
            answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
            TextView optionTextView = (TextView) option.findViewById(R.id.option);
            optionTextView.setText("" + (char) (i + 97));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 20);
            answersView.addView(option);
            option.setLayoutParams(params);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (item.getSelectedAnswers().contains(answer.getId()) == true) {
                if (answer.getIsCorrect() == true) {
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionTextView.setBackgroundResource(R.drawable.round_green_background);
                    } else {
                        optionTextView.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_green_background));
                    }
                } else {
                    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        optionTextView.setBackgroundResource(R.drawable.round_red_background);
                    } else {
                        optionTextView.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_red_background));
                    }
                }
            }
            if (answer.getIsCorrect() == true) {
                TextView correctOption = new TextView(inflater.getContext());
                int hw = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, inflater.getContext().getResources().getDisplayMetrics());
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(hw, hw);
                textParams.setMargins(0, 0, 5, 0);
                correctOption.setLayoutParams(textParams);
                correctOption.setGravity(Gravity.CENTER);
                correctOption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                correctOption.setTextColor(Color.parseColor("#FFFFFF"));
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    correctOption.setBackgroundResource(R.drawable.round_background);
                } else {
                    correctOption.setBackground(inflater.getContext().getResources().getDrawable(R.drawable.round_background));
                }
                correctOption.setTypeface(Typeface.DEFAULT_BOLD);
                correctOption.setText("" + (char) (i + 97));
                correctOption.setVisibility(View.VISIBLE);
                correctAnswersView.addView(correctOption);
            }
        }

        final String explanation = item.getReviewQuestion().getExplanationHtml().replaceAll("\n", "");
        TextView explanationsView = textView(2);
        if (explanation.equals("")) {
            updater.view.findViewById(R.id.explanation_heading).setVisibility(View.GONE);
            updater.view.findViewById(R.id.explanation).setVisibility(View.GONE);
        } else {
            html = Html.fromHtml(explanation, new UILImageGetter(explanationsView, c), null);
            ZoomableImageString zoomableImageExplanation = new ZoomableImageString(activity);
            explanationsView.setText(zoomableImageExplanation.convertString(html), TextView.BufferType.SPANNABLE);
            explanationsView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}

package in.testpress.testpress.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import in.testpress.testpress.R;

import butterknife.InjectView;
import in.testpress.testpress.R.id;
import in.testpress.testpress.models.AttemptAnswer;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.AttemptQuestion;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;

public class AttemptQuestionsFragment extends Fragment {
    AttemptItem attemptItem;
    Integer index;

    @InjectView(id.question) TextView questionsView;
    @InjectView(id.question_index) TextView questionIndex;
    @InjectView(id.answers) RadioGroup answersView;
    @InjectView(id.review) CheckBox review;
    @InjectView(id.answers_checkbox) ViewGroup answersCheckboxView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.attemptItem = getArguments().getParcelable("attemptItem");
        this.index = getArguments().getInt("question_index");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
        final AttemptQuestion attemptQuestion = attemptItem.getAttemptQuestion();

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.attempt_question_fragment, container, false);
        ButterKnife.inject(this,view);

        questionIndex.setText(index + ".");
        Log.e("AttemptQuestions", "Using new UILImageGetter");
        Spanned htmlSpan = Html.fromHtml(attemptQuestion.getQuestionHtml(), new UILImageGetter(questionsView, getActivity()), null);
        Log.e("AttemptQuestions", "Clickable Text");
        ZoomableImageString zoomableImageQuestion = new ZoomableImageString(getActivity());
        questionsView.setText(zoomableImageQuestion.convertString(htmlSpan));
        questionsView.setMovementMethod(LinkMovementMethod.getInstance());

        String type = attemptItem.getAttemptQuestion().getType();
        switch (type) {
            case "R": createRadioButtonView(attemptAnswers, attemptQuestion);
                break;
            case "C": createCheckBoxView(attemptAnswers, attemptQuestion);
                break;
            default:break;
        }
        try {
            review.setChecked(attemptItem.getReview());
        }
        catch (Exception e) {
            attemptItem.setReview(false);
        }
        attemptItem.saveAnswers(attemptItem.getSelectedAnswers());
        attemptItem.setCurrentReview(attemptItem.getReview());
        return view;
    }

    @OnCheckedChanged(id.review) void onChecked(boolean checked) {
        attemptItem.setCurrentReview(checked);
    }

    public void createCheckBoxView(final List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        final List<Integer> savedAnswers = new ArrayList<Integer>();
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            final String answer  = attemptQuestion.getAttemptAnswers().get(i).getTextHtml();
            final CheckBox option = new CheckBox(getActivity());
            option.setId(i);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            option.setLayoutParams(layoutParams);

            Spanned htmlSpan = Html.fromHtml(attemptAnswers.get(i).getTextHtml(), new UILImageGetter(option, getActivity()), null);
            ZoomableImageString zoomableImageOption = new ZoomableImageString(getActivity());
            option.setText(zoomableImageOption.convertString(htmlSpan));
            option.setMovementMethod(LinkMovementMethod.getInstance());

            option.setButtonDrawable(android.R.color.transparent);
            option.setPadding(25, 10, 0, 0);
            List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersCheckboxView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
                    if(checked) {
                        compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        savedAnswers.add(attemptAnswers.get(compoundButton.getId()).getId());

                    }
                    else {compoundButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        savedAnswers.remove(attemptAnswers.get(compoundButton.getId()).getId());}
                    attemptItem.saveAnswers(savedAnswers);
                }
            });
        }
    }

    public void createRadioButtonView(List<AttemptAnswer> attemptAnswers, AttemptQuestion attemptQuestion) {
        for(int i = 0 ; i < attemptQuestion.getAttemptAnswers().size() ; i++) {
            LayoutInflater inflater;
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final RadioButton option = (RadioButton) inflater.inflate(R.layout.attempt_radio_button_fragment ,
                    null);
            option.setId(i);
            Spanned htmlSpan = Html.fromHtml(attemptAnswers.get(i).getTextHtml(), new UILImageGetter(option, getActivity()), null);

            ZoomableImageString zoomableImageOption = new ZoomableImageString(getActivity());
            option.setText(zoomableImageOption.convertString(htmlSpan));
            option.setMovementMethod(LinkMovementMethod.getInstance());
            List<Integer> selectedAnswers = attemptItem.getSelectedAnswers();

            if(!selectedAnswers.isEmpty()) {
                if (selectedAnswers.get(0).equals(attemptAnswers.get(i).getId())) {
                    option.setChecked(true);
                    //option.setBackgroundColor(Color.parseColor("#66FF99"));
                }
            }
            answersView.addView(option);
            option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    List<AttemptAnswer> attemptAnswers = attemptItem.getAttemptQuestion().getAttemptAnswers();
                    if(checked) {
                        //compoundButton.setBackgroundColor(Color.parseColor("#66FF99"));
                        List<Integer> savedAnswers = new ArrayList<Integer>();
                        savedAnswers.add(attemptAnswers.get(compoundButton.getId()).getId());
                        attemptItem.saveAnswers(savedAnswers);
                    }
                    //else compoundButton.setBackgroundColor(android.R.color.transparent);
                }
            });
        }
    }

}

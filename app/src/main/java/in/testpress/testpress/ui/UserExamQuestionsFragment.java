package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import in.testpress.testpress.R;

import butterknife.InjectView;
import in.testpress.testpress.R.id;
import in.testpress.testpress.models.Answer;
import in.testpress.testpress.models.Question;

public class UserExamQuestionsFragment extends Fragment {
    Question question;
    List<Answer> answers;

    UserExamQuestionsFragment(Question question)    {
        this.question = question;
        this.answers = question.getAnswers();
    }

      @InjectView(id.question) TextView questionsView;
      @InjectView(id.answers) ViewGroup answersView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_user_exam_questions, container, false);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Exam");
        ((ActionBarActivity)getActivity()).getSupportActionBar().setIcon(R.drawable.ic_home);
        ButterKnife.inject(this,view);
        questionsView.setText(Html.fromHtml(question.getQuestionHtml()));

        for(int i = 0; i < question.getAnswers().size() ; i++) {
            RadioButton option = new RadioButton(getActivity());
            option.setId(i);
            option.setText(Html.fromHtml(answers.get(i).getTextHtml()));
            option.setGravity(Gravity.CENTER);
            answersView.addView(option);
        }
        return view;
    }


}

package in.testpress.testpress.ui;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.List;

import in.testpress.testpress.models.Question;
import in.testpress.testpress.models.Questions;

public class ExamPagerAdapter extends FragmentStatePagerAdapter {
    int numberOfPages = 0;
    List<Questions> questions;
    Question question;

    public ExamPagerAdapter(FragmentManager fragmentManager, List<Questions> questions) {
        super(fragmentManager);
        this.questions = questions;
        numberOfPages = questions.size();
    }

    public void setcount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int arg0) {
        question = questions.get(arg0).getQuestion();
        UserExamQuestionsFragment userExamQuestionsFragment = new UserExamQuestionsFragment(question);
        return userExamQuestionsFragment;
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

}

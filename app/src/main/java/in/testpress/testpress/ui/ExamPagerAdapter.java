package in.testpress.testpress.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

import in.testpress.testpress.models.AttemptQuestion;

public class ExamPagerAdapter extends FragmentStatePagerAdapter {
    int numberOfPages = 0;
    List<AttemptQuestion> questions;

    public ExamPagerAdapter(FragmentManager fragmentManager, List<AttemptQuestion> questions) {
        super(fragmentManager);
        this.questions = questions;
        numberOfPages = questions.size();
    }

    public void setcount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int arg0) {
        AttemptQuestionsFragment attemptQuestionsFragment = new AttemptQuestionsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("question", questions.get(arg0));
        attemptQuestionsFragment.setArguments(bundle);
        return attemptQuestionsFragment;
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

}

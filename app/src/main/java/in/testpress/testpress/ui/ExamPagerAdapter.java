package in.testpress.testpress.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.AttemptItem;

public class ExamPagerAdapter extends FragmentPagerAdapter {
    int numberOfPages = 0;
    List<AttemptItem> attemptItemList = Collections.emptyList();


    public ExamPagerAdapter(FragmentManager fragmentManager, List<AttemptItem> attemptItemList) {
        super(fragmentManager);
        this.attemptItemList = attemptItemList;
        numberOfPages = attemptItemList.size();
    }

    public void setcount(int count) {
        this.numberOfPages = count;
    }

    @Override
    public Fragment getItem(int arg0) {
        AttemptQuestionsFragment attemptQuestionsFragment = new AttemptQuestionsFragment();
        Bundle bundle = new Bundle();
        AttemptItem item = attemptItemList.get(arg0);
        bundle.putParcelable("attemptItem", attemptItemList.get(arg0));
        bundle.putInt("question_index", arg0 + 1);
        attemptQuestionsFragment.setArguments(bundle);
        return attemptQuestionsFragment;
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

}

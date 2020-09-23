package in.testpress.testpress.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

class BottomBarPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragments;

    BottomBarPagerAdapter(FragmentActivity activity, ArrayList<Fragment> fragments) {
        super(activity.getSupportFragmentManager());
        mFragments = fragments;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

}

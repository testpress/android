package in.testpress.testpress.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

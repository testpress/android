package in.testpress.testpress.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class StartExamPagerAdapter extends FragmentStatePagerAdapter {
    int PAGE_COUNT=5;
    public StartExamPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int arg0) {
        StartExamFragment startExamFragment = new StartExamFragment();
        Bundle data = new Bundle();
        return startExamFragment;

    }
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

}
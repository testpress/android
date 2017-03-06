package in.testpress.testpress.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.testpress.testpress.R;

public class AnalyticsPagerAdapter extends FragmentPagerAdapter {
    private Resources resources;
    private Bundle bundle;

    public AnalyticsPagerAdapter(Resources resources, FragmentManager fragmentManager,
                                 Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new StrengthAnalyticsGraphFragment();
                break;
            case 1:
                fragment = new IndividualSubjectAnalyticsFragment();
                break;
            default:
                fragment = new IndividualSubjectAnalyticsFragment();
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.overall_reports);
            case 1:
                return resources.getString(R.string.individual_reports);
            default:
                return null;
        }
    }

}

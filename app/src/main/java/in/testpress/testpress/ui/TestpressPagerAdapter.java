

package in.testpress.testpress.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.testpress.testpress.R;

/**
 * Pager adapter
 */
public class TestpressPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public TestpressPagerAdapter(final Resources resources, final FragmentManager fragmentManager) {
        super(fragmentManager);
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return 3;
    }
    
    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        final Fragment result2;
        boolean t = false;
        String subclass;
        Bundle bundle;
        switch (position) {
            case 0:
                subclass = "available";
                break;
            case 1:
                subclass = "upcoming";
                break;
            case 2:
                subclass = "history";
                t = true;
                break;
            default:
                subclass = null;
                break;
        }
        if (t == true) {
            result2 = new NativeListBaseFragment();
            bundle = new Bundle();
            if (subclass != null) {
                bundle.putString("subclass", subclass);
                result2.setArguments(bundle);
            }
            return result2;
        } else {
            result = new ExamsListFragment();
            bundle = new Bundle();
            if (subclass != null) {
                bundle.putString("subclass", subclass);
                result.setArguments(bundle);
            }
            return result;
        }
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.page_available_exams);
            case 1:
                return resources.getString(R.string.page_upcoming_exams);
            case 2:
                return resources.getString(R.string.page_history_exams);
            default:
                return null;
        }
    }
}

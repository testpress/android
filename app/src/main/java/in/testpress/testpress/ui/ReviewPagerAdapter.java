

package in.testpress.testpress.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;

/**
 * Pager adapter
 */
public class ReviewPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;
    Exam exam;
    Attempt attempt;

    /**
     * Create pager adapter
     *
     * @param fragment
     * @param exam
     * @param attempt
     */
    public ReviewPagerAdapter(final Fragment fragment, Exam exam, Attempt attempt) {
        super(fragment.getChildFragmentManager());
        this.exam = exam;
        this.attempt = attempt;
        this.resources = fragment.getResources();
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        String filter = "";
        switch (position) {
            case 1:
                filter = "all";
                break;
            case 2:
                filter = "correct";
                break;
            case 3:
                filter = "incorrect";
                break;
            case 4:
                filter = "unanswered";
                break;
            case 5:
                filter = "review";
                break;
            case 0:
            default:
                filter = "stats";
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable("exam", exam);
        bundle.putParcelable("attempt", attempt);
        if (filter.equals("stats") == true) {
            result = new ReviewStatsFragment();
        } else {
            result = new ReviewQuestionsFragment();
            bundle.putString("filter", filter);
        }
        result.setArguments(bundle);
        return result;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.page_stats);
            case 1:
                return resources.getString(R.string.page_all);
            case 2:
                return resources.getString(R.string.page_correct);
            case 3:
                return resources.getString(R.string.page_incorrect);
            case 4:
                return resources.getString(R.string.page_unanswered);
            case 5:
                return resources.getString(R.string.page_review);
            default:
                return null;
        }
    }
}

package in.testpress.testpress.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.testpress.testpress.R;
import com.viewpagerindicator.TitlePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Fragment which houses the View pager.
 */
public class CarouselFragment extends Fragment {

    @InjectView(R.id.tpi_header)
    protected TitlePageIndicator indicator;

    @InjectView(R.id.vp_pages)
    protected ViewPager pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carousel, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ButterKnife.inject(this, getView());

        pager.setAdapter(new BootstrapPagerAdapter(getResources(), getChildFragmentManager()));
        indicator.setViewPager(pager);
        pager.setCurrentItem(1);

    }
}
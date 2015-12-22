package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.AttemptsPager;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

public class ReviewFragment extends Fragment {
    @InjectView(R.id.tpi_header)
    protected PagerSlidingTabStrip indicator;
    @Inject protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.vp_pages)
    protected ViewPager pager;
    AttemptsPager attemptsPager;

    Attempt attempt;
    Exam exam;
    String currentItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.attempt_actions, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (getActivity() == null) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.retake:
                if (exam.getAllowRetake() == true) {
                    Intent intent = new Intent(getActivity(), ExamActivity.class);
                    intent.putExtra("exam", exam);
                    startActivity(intent);
                    return true;
                } else {
                    Toaster.showShort(getActivity(), "Retakes are not allowed for this exam.");
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setViewPager(View view) {
        view.findViewById(R.id.pb_loading).setVisibility(View.GONE);
        view.findViewById(R.id.tabs_container).setVisibility(View.VISIBLE);
        pager.setAdapter(new ReviewPagerAdapter(this, this.exam, this.attempt));
        indicator.setViewPager(pager);
        indicator.setIndicatorColor(getResources().getColor(R.color.primary));
        if (currentItem != null) {
            pager.setCurrentItem(Integer.parseInt(currentItem));
        } else {
            pager.setCurrentItem(0);
        }
    }

    private void fetchAndRenderAttempt(final View view) {
        new SafeAsyncTask<Attempt>() {
            @Override
            public Attempt call() throws Exception {
                attemptsPager.next();
                return attemptsPager.getResources().get(0);
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                view.findViewById(R.id.pb_loading).setVisibility(View.GONE);
                view.findViewById(R.id.empty_container).setVisibility(View.VISIBLE);
            }

            @Override
            protected void onSuccess(final Attempt a) throws Exception {
                attempt = a;
                setViewPager(view);
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.review_fragment, container, false);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        this.exam = getArguments().getParcelable("exam");
        this.attempt = getArguments().getParcelable("attempt");
        currentItem = getArguments().getString("currentItem");
        if (attempt == null) {
            try {
                Ln.e("Activity is " + getActivity());
                attemptsPager = new AttemptsPager(exam, serviceProvider.getService(getActivity()));
                fetchAndRenderAttempt(view);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AccountsException e) {
                e.printStackTrace();
            }
        } else {
            setViewPager(view);
        }

        return view;
    }
}

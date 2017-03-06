package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.SubjectPager;
import in.testpress.testpress.models.Subject;
import in.testpress.testpress.util.CommonUtils;

public class AnalyticsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Subject>> {

    public static final String SUBJECT = "subject";
    public static final String SUBJECTS = "subjects";

    @Inject
    protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.subjects_layout) LinearLayout subjectsLayout;
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.tab_layout) TabLayout tabLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;
    @InjectView(R.id.progress_bar) ProgressBar progressBar;
    private SubjectPager pager;
    private Subject subject;
    private List<Subject> subjects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        if (getArguments() != null) {
            subject = getArguments().getParcelable(SUBJECT);
        }
        if (CommonUtils.isUserAuthenticated(getActivity())) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.analytics_fragment, container, false);
        ButterKnife.inject(this, view);
        subjectsLayout.setVisibility(View.GONE);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                getLoaderManager().restartLoader(0, null, AnalyticsFragment.this);
            }
        });
        return view;
    }

    private SubjectPager getPager() {
        if (pager == null) {
            try {
                pager = new SubjectPager(serviceProvider.getService(getActivity()));
                if (subject != null) {
                    pager.setQueryParams(Constants.Http.PARENT, subject.getId().toString());
                } else {
                    pager.setQueryParams(Constants.Http.PARENT, "null");
                }
            } catch (AccountsException | IOException e) {
                displayAuthenticationFailed();
            }
        }
        return pager;
    }

    @Override
    public Loader<List<Subject>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<Subject>>(getActivity(), subjects) {
            @Override
            public List<Subject> loadData() throws Exception {
                do {
                    getPager().next();
                    subjects = pager.getResources();
                } while (pager.hasNext());
                return subjects;
            }
        };
    }

    @SuppressLint("InflateParams")
    @Override
    public void onLoadFinished(final Loader<List<Subject>> loader, final List<Subject> items) {
        //noinspection ThrowableResultOfMethodCallIgnored
        Exception exception = ((ThrowableLoader<List<Subject>>) loader).clearException();
        if(exception != null) {
            if (exception.getMessage().equals("403 FORBIDDEN")) {
                displayAuthenticationFailed();
            } else if (exception.getCause() instanceof IOException) {
                setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);
            } else {
                setEmptyText(R.string.error_loading_analytics, R.string.try_after_sometime,
                        R.drawable.ic_error_outline_black_18dp);
            }
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (items.isEmpty()) {
            setEmptyText(R.string.no_analytics, R.string.no_analytics_description,
                    R.drawable.ic_bar_chart_24dp);
            retryButton.setVisibility(View.GONE);
            if (subject != null) {
                emptyDescView.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        subjectsLayout.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SUBJECTS, new ArrayList<>(items));
        AnalyticsPagerAdapter adapter = new AnalyticsPagerAdapter(getResources(),
                getChildFragmentManager(), bundle);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        progressBar.setVisibility(View.GONE);
    }

    private void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    private void displayAuthenticationFailed() {
        setEmptyText(R.string.authentication_failed, R.string.please_login,
                R.drawable.ic_error_outline_black_18dp);
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(final Loader<List<Subject>> loader) {
    }

}

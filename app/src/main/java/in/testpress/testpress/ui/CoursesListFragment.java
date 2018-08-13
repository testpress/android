package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Course;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.CoursePager;

public class CoursesListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Course>> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.recycler_view) RecyclerView recyclerView;
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.image_view) ImageView emptyImageView;
    @InjectView(R.id.retry_button) Button retryButton;
    private CourseListAdapter adapter;
    private CoursePager coursePager;
    private List<Course> courses = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swipe_refresh_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        adapter = new CourseListAdapter(getActivity(), courses);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPager().reset();
                refreshWithProgress();
            }
        });
        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.measure(View.MEASURED_SIZE_MASK, View.MEASURED_HEIGHT_STATE_SHIFT);
        swipeLayout.setRefreshing(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Course>> onCreateLoader(int loaderID, Bundle args) {
        return new in.testpress.util.ThrowableLoader<List<Course>>(getActivity(), null) {
            @Override
            public List<Course> loadData() throws TestpressException {
                do {
                    getPager().next();
                    courses = getPager().getResources();
                } while (getPager().hasNext());
                return courses;
            }
        };
    }

    protected CoursePager getPager() {
        if (coursePager == null) {
            coursePager = new CoursePager(new TestpressCourseApiClient(getActivity()));
        }
        return coursePager;
    }

    @Override
    public void onLoadFinished(Loader<List<Course>> loader, List<Course> items) {
        TestpressException exception = ((in.testpress.util.ThrowableLoader) loader).clearException();
        swipeLayout.setRefreshing(false);
        if (exception != null) {
            int error = getErrorMessage(exception);
            if (courses.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                showError(error);
            }
            return;
        }

        if (items == null || items.isEmpty()) {
            setEmptyText(R.string.testpress_no_courses, R.string.testpress_no_courses_description,
                    R.drawable.no_course);

            emptyImageView.setAlpha(1f);
            emptyView.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);
            return;
        }
        adapter.setCourses(items);
        recyclerView.getRecycledViewPool().clear();
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    protected int getErrorMessage(TestpressException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.testpress_authentication_failed, R.string.testpress_please_login,
                    R.drawable.testpress_alert_warning);

            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.network_error, R.string.no_internet,
                    R.drawable.testpress_no_wifi);

            return R.string.testpress_no_internet_try_again;
        }
        setEmptyText(R.string.testpress_error_loading_courses,
                R.string.testpress_some_thing_went_wrong_try_again,
                R.drawable.testpress_alert_warning);

        return R.string.testpress_some_thing_went_wrong_try_again;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.retry_button)
    protected void refreshWithProgress() {
        emptyView.setVisibility(View.GONE);
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
        getLoaderManager().restartLoader(0, null, this);
    }

    protected void setEmptyText(int title, int description, int imageResId) {
        emptyTitleView.setText(title);
        emptyTitleView.setTypeface(TestpressSdk.getRubikMediumFont(getContext()));
        emptyDescView.setText(description);
        emptyDescView.setTypeface(TestpressSdk.getRubikRegularFont(getContext()));
        emptyImageView.setImageResource(imageResId);
        retryButton.setVisibility(View.VISIBLE);
    }

    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

}
package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ExamPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Exam;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class ExamsListFragment extends PagedItemFragment<Exam> {

    String subclass;
    ExamPager pager;
    //List of courses got from the api. This is used to populate the spinner.
    ArrayList<String> courses = new ArrayList<String>();
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    private ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    private View mSpinnerContainer;
    private Boolean mFistTimeCallback = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        subclass = getArguments().getString("subclass");
        try {
            pager = new ExamPager(subclass, serviceProvider.getService(getActivity()));
        } catch (AccountsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(), getActivity().getResources(), true);
        mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_exams), false, 0);
        mTopLevelSpinnerAdapter.addHeader(getString(R.string.courses));
        Toolbar toolbar = ((ExamsListActivity)(getActivity())).getActionBarToolbar();
        mSpinnerContainer = getActivity().getLayoutInflater().inflate(R.layout.actionbar_spinner, toolbar, false);

        Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mTopLevelSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (!mFistTimeCallback) {
                    mFistTimeCallback = true;
                    return;
                }
                String filter = mTopLevelSpinnerAdapter.getTag(position);
                if (filter.isEmpty()) {
                    pager.removeQueryParams("course");
                } else {
                    pager.setQueryParams("course", filter);
                }
                refreshWithProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mSpinnerContainer.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_exams, R.string.no_exams_description, R.drawable.ic_error_outline_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
    }

    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> items) {

        //Return if no items are returned
        if (items.isEmpty()) {
            if(internetConnectivityChecker.isConnected()) {
                if (subclass.equals("history")) {
                    setEmptyText(R.string.no_attempts, R.string.no_attempts_description, R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.no_exams, R.string.no_exams_description, R.drawable.ic_error_outline_black_18dp);
                }
            } else {
                setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
            }
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
        //Populate the spinner with the courses
        List<String> coursesList = new ArrayList<String>();
        for (final Exam exam : items) {
            coursesList.add(exam.getCourse_category());
        }
        Set<String> uniqueCourses = new HashSet<String>(coursesList);
        for (final String course : uniqueCourses) {
            // Do not add the course if already present in the spinner
            if (!courses.contains(course)) {
                courses.add(course);
                mTopLevelSpinnerAdapter.addItem(course, course, true, 0);
            }
        }

        if ((mSpinnerContainer.getVisibility() == View.GONE) && !uniqueCourses.isEmpty()){
            mSpinnerContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isUsable()) {
            Toolbar toolbar = ((ExamsListActivity)(getActivity())).getActionBarToolbar();
            View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
            toolbar.removeView(view);
            toolbar.invalidate();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);
        }
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected ResourcePager<Exam> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        int layout = R.layout.available_exams_list_item;
        if (subclass != null) {
            if (subclass.equals("available")) {
                layout = R.layout.available_exams_list_item;
            } else if (subclass.equals("upcoming")) {
                layout = R.layout.available_exams_list_item;
                //return serviceProvider.getService(getActivity()).getUpcomingExams();
            } else if (subclass.equals("history")) {
                layout = R.layout.history_exams_list_item;
                return new HistoryListAdapter(getActivity(), items, layout);
            } else {
                layout = R.layout.available_exams_list_item;
            }
        }
        return new ExamsListAdapter(getActivity().getLayoutInflater(), items, layout);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Exam exam = ((Exam) l.getItemAtPosition(position));

        if (subclass.equals("available")) {
            Intent intent = new Intent(getActivity(), ExamActivity.class);
            intent.putExtra("exam", exam);
            startActivity(intent);
        }

        if (subclass.equals("history")) {
            Intent intent = new Intent(getActivity(), AttemptsListActivity.class);
            intent.putExtra("exam", exam);
            startActivity(intent);
        }
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.handleForbidden(getActivity(), serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.error_loading_exams;
    }
}

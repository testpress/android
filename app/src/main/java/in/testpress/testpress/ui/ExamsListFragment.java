package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import in.testpress.testpress.models.ExamCategory;
import in.testpress.testpress.util.SafeAsyncTask;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ExamsListFragment extends PagedItemFragment<Exam> {

    String subclass;
    //List of courses got from the api. This is used to populate the spinner.
    ArrayList<String> courses = new ArrayList<String>();
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    private ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    private View mSpinnerContainer;
    Spinner spinner;
    private Boolean mFistTimeCallback = false;
    int selectedCategoryPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        subclass = getArguments().getString("subclass");
        super.onCreate(savedInstanceState);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(), getActivity().getResources(), true);
        mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_exams), false, 0);
        mTopLevelSpinnerAdapter.addHeader(getString(R.string.courses));

        Toolbar toolbar = ((ExamsListActivity)(getActivity())).getActionBarToolbar();
        mSpinnerContainer = getActivity().getLayoutInflater().inflate(R.layout.actionbar_spinner, toolbar, false);

        spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        ArrayList<ExamCategory> categoryList = getArguments().getParcelableArrayList("categoryList");
        if (categoryList != null && !categoryList.isEmpty()) {
            for (ExamCategory category : categoryList) {
                mTopLevelSpinnerAdapter.addItem(category.getName(), category.getName(), true, 0);
            }
            selectedCategoryPosition = 0;
        } else {
            mSpinnerContainer.setVisibility(View.GONE);
        }
        spinner.setAdapter(mTopLevelSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (!mFistTimeCallback) {
                    mFistTimeCallback = true;
                } else if ((selectedCategoryPosition != position)) { //omit callback if position is already selected position
                    selectedCategoryPosition = position;
                    String filter = mTopLevelSpinnerAdapter.getTag(position);
                    if (filter.isEmpty()) {
                        getPager().removeQueryParams("course");
                    } else {
                        getPager().setQueryParams("course", filter);
                    }
                    ExamsListFragment.super.refreshWithProgress();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_search, optionsMenu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_exams, R.string.no_exams_description, R.drawable
                .ic_error_outline_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
    }

    void loadExamCategories() {
        new SafeAsyncTask<List<ExamCategory>>() {
            public List<ExamCategory> call() throws Exception {
                return serviceProvider.getService(getActivity()).getExamsCourses().getResults();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                if (exception.getCause() instanceof UnknownHostException) {
                    Toaster.showShort(getActivity(), getResources().getString(R.string.no_internet));
                } else {
                    Toaster.showShort(getActivity(), exception.getMessage());
                }
            }

            @Override
            public void onSuccess(final List<ExamCategory> categoryList) {
                mTopLevelSpinnerAdapter.clear();
                if (categoryList != null && !categoryList.isEmpty()) {
                    mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_exams), false, 0);
                    mTopLevelSpinnerAdapter.addHeader(getString(R.string.courses));
                    for (ExamCategory category : categoryList) {
                        mTopLevelSpinnerAdapter.addItem(category.getName(), category.getName(), true, 0);
                    }
                    if (selectedCategoryPosition == -1) {
                        selectedCategoryPosition = 0;
                        mTopLevelSpinnerAdapter.notifyDataSetChanged();
                    } else {
                        mTopLevelSpinnerAdapter.notifyDataSetChanged();
                        spinner.setSelection(mTopLevelSpinnerAdapter.getItemPosition(spinner.getSelectedItem().toString()));
                    }
                } else {
                    mSpinnerContainer.setVisibility(View.GONE);
                    mTopLevelSpinnerAdapter.notifyDataSetChanged();
                }
            }

        }.execute();
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
    protected void refreshWithProgress() {
        super.refreshWithProgress();
        loadExamCategories();
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected ResourcePager<Exam> getPager() {
        if (pager == null) {
            try {
                pager = new ExamPager(subclass, serviceProvider.getService(getActivity()));
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        if (subclass != null) {
            if (subclass.equals("upcoming")) {
                return new UpcomingExamsListAdapter(getActivity().getLayoutInflater(), items, R.layout.upcoming_exams_list_item);
            } else if (subclass.equals("history")) {
                return new HistoryListAdapter(getActivity(), items, R.layout.history_exams_list_item);
            }
        }
        return new AvailableExamsListAdapter(getActivity(), items, R.layout.available_exams_list_item);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("subclass", subclass);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

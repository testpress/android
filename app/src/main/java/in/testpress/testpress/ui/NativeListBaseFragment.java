/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ExamPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Exam;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * List base example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class NativeListBaseFragment extends ItemListFragmenthistory<Exam> {

    String subclass;
    ArrayList<String> courses = new ArrayList<String>();
    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;

    private ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    private View mSpinnerContainer;
    private Boolean mFistTimeCallback = false;

    protected ScrollView mScrollView;

    protected ProgressBar progressBar;
    protected boolean listShown;
    protected TextView emptyView;
    ExamPager pager;


    Exam e;
    ArrayList<Card> cards = new ArrayList<Card>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        ButterKnife.inject(this.getActivity());
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
        Toolbar toolbar = ((MainActivity) (getActivity())).getActionBarToolbar();
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
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isUsable()) {
            Toolbar toolbar = ((MainActivity)(getActivity())).getActionBarToolbar();
            View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
            toolbar.removeView(view);
            toolbar.invalidate();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);
        }
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if ((exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.invalidateAuthToken();
            logoutService.logout(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    getActivity().startActivity(intent);
                }
            });
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.no_internet);
        }
        return R.string.error_loading_exams;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_exams);

    }




    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> items) {
        super.onLoadFinished(loader, items);

        //Return if no items are returned
        if (items == null) {
            return;
        }

        //Populate the spinner with the courses
        List<String> coursesList = new ArrayList<String>();
        for (final Exam exam : items) {
            coursesList.add(exam.getCourse());
        }
        Set<String> uniqueCourses = new HashSet<String>(coursesList);
        for (final String course : uniqueCourses) {
            // Do not add the course if already present in the spinner
            if (!courses.contains(course)) {
                courses.add(course);
                mTopLevelSpinnerAdapter.addItem(course, course, true, 0);
            }
        }

        if ((mSpinnerContainer.getVisibility() == View.GONE) && !uniqueCourses.isEmpty()) {
            mSpinnerContainer.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isUsable()) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.filter:
                refreshWithProgress();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroyView() {


        super.onDestroyView();
    }


    protected ResourcePager<Exam> getPager() {
        return pager;
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }


    @Override
    protected List<Card> initCards() {

        for (int i = 0; i < items.size(); i++) {

            e = items.get(i);
                CardExample card = new CardExample(getActivity(), e.getTitle(), e.getNumberOfQuestions(), e.getDuration());
                cards.add(card);

        }
        return cards;
    }

    class CardExample extends Card {

        @InjectView(R.id.exam_title)
        TextView examTitle;
        @InjectView(R.id.number_of_questions)
        TextView numberOfQuestions;
        @InjectView(R.id.exam_duration)
        TextView examDuration;

        public CardExample(Context context, String titleHeader, int q, String d) {
            super(context, R.layout.historycard);
            //CardHeader header = new CardHeader(getActivity());
         //   header.setTitle(titleHeader);
          //  addCardHeader(header);
            setTitle(titleHeader);
            numberOfQuestions.setText(d);
            examDuration.setText(d);


        }


    }


}

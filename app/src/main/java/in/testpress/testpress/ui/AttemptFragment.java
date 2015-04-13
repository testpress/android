package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.AttemptItem;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.SafeAsyncTask;

public class AttemptFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>> {
    @Inject protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.previous) TextView previous;
    @InjectView(R.id.next) TextView next;
    @InjectView(R.id.pager) TestpressViewPager pager;
    @InjectView(R.id.questions_list) ListView questionsListView;
    @InjectView(R.id.end) TextView endExamButton;
    @InjectView(R.id.sliding_layout) SlidingUpPanelLayout mLayout;

    ProgressDialog progress;
    ExamPagerAdapter pagerAdapter;

    Attempt mAttempt;
    List<AttemptItem> attemptItemList = Collections.emptyList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAttempt = getArguments().getParcelable("attempt");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.attempt_item_fragment, container, false);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        pager.setPagingEnabled(false);
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        mLayout.setEnabled(false);
        return view;
    }

    @OnClick(R.id.next) void showNextQuestion() {
        if (attemptItemList.isEmpty()) {
            return;
        }

        if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
            try {
                attemptItemList.get(pager.getCurrentItem()).saveResult(getActivity(), serviceProvider);
            }
            catch (Exception e) {
            }

        }
        if (pager.getCurrentItem() != pager.getChildCount()) {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }
    }

    @OnItemClick(R.id.questions_list) void goToQuestion(int position) {
        if (attemptItemList.isEmpty()) {
            return;
        }

        if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
        }
        pager.setCurrentItem(position);

    }

    @OnClick(R.id.previous) void showPreviousQuestion() {
        if (attemptItemList.isEmpty()) {
            return;
        }

        if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {
            try {
               attemptItemList.get(pager.getCurrentItem()).saveResult(getActivity(), serviceProvider);
            }
            catch (Exception e) {
            }
        }
         if (pager.getCurrentItem() != 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    @OnClick(R.id.end) void endExam() {
       endExam.execute();
    }

    @Override
    public Loader<List<AttemptItem>> onCreateLoader(int id, final Bundle args) {
        return new ThrowableLoader<List<AttemptItem>>(getActivity(), attemptItemList) {
            @Override
            public List<AttemptItem> loadData() throws Exception {
                List<AttemptItem> response = null;
                TestpressApiResponse<AttemptItem> apiresponse = new TestpressApiResponse<AttemptItem>();
                String fragment = null;
                URL url = new URL(mAttempt.getQuestionsUrl());
                do {
                    try {
                        fragment = url.getFile();
                    }
                    catch (Exception e) {
                        return null;
                    }

                    try {
                        apiresponse = serviceProvider.getService(getActivity()).getQuestions(fragment);
                        if (response != null) {
                            response.addAll(apiresponse.getResults());
                        } else {
                            response = apiresponse.getResults();
                        }
                        String next = apiresponse.getNext();
                        if (next != null) {
                            url = new URL(apiresponse.getNext());
                        }
                    } catch (OperationCanceledException e) {
                        return null;
                    }
                } while (apiresponse.getNext() != null);
                return response;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<List<AttemptItem>> loader, final List<AttemptItem> items) {
        attemptItemList = items;
        if (progress.isShowing()) {
            progress.hide();
            progress.dismiss();
        }

        pagerAdapter = new ExamPagerAdapter(getFragmentManager(), attemptItemList);
        pagerAdapter.setcount(attemptItemList.size());
        pager.setAdapter(pagerAdapter);
        pagerAdapter.notifyDataSetChanged();
        List<String> questionslist = new ArrayList<String>();
        for(int i = 0 ; i < attemptItemList.size() ; i++) {
            questionslist.add((i + 1) + ". " + Html.fromHtml(attemptItemList.get(i)
                    .getAttemptQuestion().getQuestionHtml()).toString());
        }
        questionsListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.drawer_list_item, questionslist));
    }

    @Override
    public void onLoaderReset(final Loader<List<AttemptItem>> loader) {
    }

    protected void returnToHistory() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("currentItem", "2");
        startActivity(intent);
        getActivity().finish();
    }

    SafeAsyncTask<Attempt> endExam = new SafeAsyncTask<Attempt>() {
        @Override
        public Attempt call() throws Exception {
            return  serviceProvider.getService(getActivity()).endExam(mAttempt.getUrlFrag() + Constants.Http.URL_END_EXAM_FRAG);
        }
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onSuccess(Attempt result) {
        }

        @Override
        protected void onException(Exception e) {

        }

        @Override
        protected void onFinally() {
            returnToHistory();
        }
    };
}

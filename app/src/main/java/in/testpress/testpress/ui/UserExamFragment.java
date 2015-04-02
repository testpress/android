package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Questions;

public class UserExamFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Questions>> {
    @Inject protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.start_exam) Button startExam;
    @InjectView(R.id.previous) Button previous;
    @InjectView(R.id.next) Button next;
    @InjectView(R.id.pager) TestpressViewPager pager;

    ProgressDialog progress;
    FragmentManager startExamFragmentManager;
    ExamPagerAdapter pagerAdapter;

    String examId, userExamId;
    List<Questions> questions;
    protected List<Questions> items = Collections.emptyList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        examId = getArguments().getString("examId");
        userExamId = getArguments().getString("userExamId");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_user_exam, container, false);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        progress = new ProgressDialog(getActivity());
        return view;
    }

    @OnClick(R.id.start_exam) void startExam() {
        getLoaderManager().initLoader(0, null, this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        startExamFragmentManager = getFragmentManager();
        pager.setPagingEnabled(false);
        startExam.setVisibility(View.INVISIBLE);
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.next) void showNextQuestion() {
        if (pager.getCurrentItem() != pager.getChildCount()) {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }
    }

    @OnClick(R.id.previous) void showPreviousQuestion() {
        if (pager.getCurrentItem() != 0) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    @Override
    public Loader<List<Questions>> onCreateLoader(int id, final Bundle args) {
        final List<Questions> initialItems = items;
        return new ThrowableLoader<List<Questions>>(getActivity(), items) {
            @Override
            public List<Questions> loadData() throws Exception {
                try {
                    return serviceProvider.getService(getActivity()).getQuestions(examId, userExamId);
                } catch (OperationCanceledException e) {
                    return initialItems;
                }

            }
        };
    }

    public void onLoadFinished(final Loader<List<Questions>> loader, final List<Questions> items) {
        if (progress.isShowing())
            progress.hide();
        questions=items;
        pagerAdapter = new ExamPagerAdapter(startExamFragmentManager,questions);
        pager.setAdapter(pagerAdapter);
    }

    @Override
    public void onLoaderReset(final Loader<List<Questions>> loader) {

    }
}

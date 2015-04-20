package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    @InjectView(R.id.pause_exam) TextView pauseExamButton;
    @InjectView(R.id.sliding_layout) SlidingUpPanelLayout mLayout;
    @InjectView(R.id.timer) TextView timer;

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
        mLayout.setEnabled(true);
        mLayout.setTouchEnabled(false);
        return view;
    }

    @OnClick(R.id.question_list) void openPanel() {
        if(mLayout.getPanelState().equals(PanelState.EXPANDED)) {
            collapsePanel();
        }
        else {
            expandPanel();
        }

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
            try {
                attemptItemList.get(pager.getCurrentItem()).saveResult(getActivity(), serviceProvider);
            }
            catch (Exception e) {
            }
        }
        pager.setCurrentItem(position);
        collapsePanel();
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
        final DialogAlert dialog = new DialogAlert(getActivity(), "end");
        dialog.show();
    }

    @OnClick(R.id.pause_exam) void pauseExam() {
        final DialogAlert dialog = new DialogAlert(getActivity(), "pause");
        dialog.show();
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
        questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), attemptItemList, R.layout.panel_list_item));
        CountDownTimer Timer = new CountDownTimer(formatMillisecond(mAttempt.getRemainingTime()), 1000) {

            public void onTick(long millisUntilFinished) {
                final String formattedTime = formatTime(millisUntilFinished);
                timer.setText(formattedTime);
            }

            public void onFinish() {
                endExam.execute();
            }
        }.start();
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

    public class DialogAlert extends Dialog {
        String selection;
        public DialogAlert(Context context, String selection) {
            super(context, R.style.ActivityDialog);
            this.selection = selection;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            window.setGravity(Gravity.CENTER);
            setContentView(R.layout.dialog_layout);
            TextView dialogMessage = (TextView) findViewById(R.id.dialog_message);
            TextView option = (TextView) findViewById(R.id.option);
            TextView cancel = (TextView) findViewById(R.id.cancel);
            if(selection.equals("pause")) {
                dialogMessage.setText(R.string.pause_message);
                option.setText(R.string.pause);
                option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        returnToHistory();
                        dismiss();
                    }
                });
            }
            else {
                dialogMessage.setText(R.string.end_message);
                option.setText(R.string.end);
                option.setTextColor(Color.parseColor("#d9534f"));
                option.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        endExam.execute();
                        dismiss();
                    }
                });
            }
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   dismiss();
                }
            });
        }
    }

    public static String formatTime(final long millis) {
        return String.format("%02d:%02d:%02d",
                millis / (1000 * 60 * 60),
                (millis / (1000 * 60)) % 60,
                (millis / 1000) % 60
        );
    }

    public  long formatMillisecond(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = simpleDateFormat.parse(inputString);
        }
        catch (ParseException e) {
        }
        return date.getTime();
    }

    public void collapsePanel() {
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
    }

    public void expandPanel() {
        mLayout.setPanelState(PanelState.EXPANDED);
        previous.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
    }
}

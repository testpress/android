package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
    @InjectView(R.id.filter) Spinner filter;

    ProgressDialog progress;
    ExamPagerAdapter pagerAdapter;

    Attempt mAttempt;
    List<AttemptItem> attemptItemList = Collections.emptyList();
    CountDownTimer countDownTimer;

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
        String[] filters= { "All", "Answered", "Unanswered", "Marked for review" };
        ArrayAdapter<String> adapter =new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        setFilter("all");
                        break;
                    case 1:
                        setFilter("answered");
                        break;
                    case 2:
                        setFilter("unanswered");
                        break;
                    case 3:
                        setFilter("marked");
                        break;
                    default:
                        setFilter("all");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

        if (next.getText().equals(getResources().getString(R.string.end))) {
            endExamAlert();
            return;
        }

        if (pager.getCurrentItem() != pager.getChildCount()) {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }

        int currentPosition = pager.getCurrentItem();
        if (currentPosition > 0) {
            previous.setClickable(true);
            previous.setTextColor(getResources().getColor(R.color.primary));
        }

        if (currentPosition + 1 >= attemptItemList.size()) {
            next.setTextColor(Color.parseColor("#d9534f"));
            next.setText(R.string.end);
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
        AttemptItem item = ((AttemptItem) questionsListView.getItemAtPosition(position));
        pager.setCurrentItem(item.getIndex() - 1);
        collapsePanel();

        if (position == 0) {
            previous.setClickable(false);
            previous.setTextColor(getResources().getColor(R.color.nav_button_disabled));
        } else {
            previous.setClickable(true);
            previous.setTextColor(getResources().getColor(R.color.primary));
        }

        if ((position + 1) == attemptItemList.size()) {
            next.setTextColor(Color.parseColor("#d9534f"));
            next.setText(R.string.end);
        } else {
            next.setTextColor(getResources().getColor(R.color.primary));
            next.setText(R.string.next);
        }
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

        int currentPosition = pager.getCurrentItem();
        if (currentPosition < attemptItemList.size()) {
            next.setTextColor(getResources().getColor(R.color.primary));
            next.setText(R.string.next);
        }

        if (currentPosition == 0) {
            previous.setClickable(false);
            previous.setTextColor(getResources().getColor(R.color.nav_button_disabled));
            return;
        }
    }

    @OnClick(R.id.end) void endExamAlert() {
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
                        fragment = url.getFile().substring(1);
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
        for (int i = 0; i<attemptItemList.size(); i++) {
            attemptItemList.get(i).setIndex(i + 1);
        }
        questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), attemptItemList, R.layout.panel_list_item));
        countDownTimer = new CountDownTimer(formatMillisecond(mAttempt.getRemainingTime()), 1000) {

            public void onTick(long millisUntilFinished) {
                final String formattedTime = formatTime(millisUntilFinished);
                timer.setText(formattedTime);
                if(((millisUntilFinished / 1000) % 180) == 0) {
                    sendHeartBeat.execute();
                }
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("currentItem", "2");
        startActivity(intent);
        getActivity().finish();
    }

    protected void showReview() {
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("exam", mAttempt.getExam());
        intent.putExtra("attempt", mAttempt);
        startActivity(intent);
        getActivity().finish();
    }

    SafeAsyncTask<Attempt> sendHeartBeat = new SafeAsyncTask<Attempt>() {
        @Override
        public Attempt call() throws Exception {
            return  serviceProvider.getService(getActivity()).heartbeat(mAttempt.getHeartBeatUrlFrag());
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

        }
    };

    SafeAsyncTask<Attempt> endExam = new SafeAsyncTask<Attempt>() {
        @Override
        public Attempt call() throws Exception {
            mAttempt = serviceProvider.getService(getActivity()).endExam(mAttempt.getUrlFrag() + Constants.Http.URL_END_EXAM_FRAG);
            return mAttempt;
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
            countDownTimer.cancel();
            showReview();
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
                        countDownTimer.cancel();
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

    public void setFilter (String filter) {
        List<AttemptItem> answereditems = new ArrayList<>();
        List<AttemptItem> unanswereditems = new ArrayList<>();
        List<AttemptItem> markeditems = new ArrayList<>();
        for(int i = 0; i< attemptItemList.size(); i++) {
            try {
                if (attemptItemList.get(i).getReview() || attemptItemList.get(i).getCurrentReview()) {
                    markeditems.add(attemptItemList.get(i));
                }
            }
            catch (Exception e) {
            }

            if(!attemptItemList.get(i).getSelectedAnswers().isEmpty() || !attemptItemList.get(i).getSavedAnswers().isEmpty()) {
                answereditems.add(attemptItemList.get(i));
            }
            else
                unanswereditems.add(attemptItemList.get(i));
        }
        switch (filter) {
            case "answered":
                questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), answereditems, R.layout.panel_list_item));
                break;
            case "unanswered":
                questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), unanswereditems, R.layout.panel_list_item));
                break;
            case "marked":
                questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), markeditems, R.layout.panel_list_item));
                break;
            default:
                questionsListView.setAdapter(new PanelListAdapter(getActivity().getLayoutInflater(), attemptItemList, R.layout.panel_list_item));
                break;
        }
    }
}

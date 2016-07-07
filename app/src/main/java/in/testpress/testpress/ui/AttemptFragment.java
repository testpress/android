package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

public class AttemptFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AttemptItem>> {
    @Inject protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.previous) TextView previous;
    @InjectView(R.id.next) TextView next;
    @InjectView(R.id.pager) TestpressViewPager pager;
    @InjectView(R.id.questions_list) ListView questionsListView;
    @InjectView(R.id.sliding_layout) SlidingUpPanelLayout mLayout;
    @InjectView(R.id.timer) TextView timer;
    @InjectView(R.id.filter) Spinner filter;
    @InjectView(R.id.subjectFilter) Spinner subjectFilter;
    @InjectView(R.id.spinnerContainer) RelativeLayout spinnerContainer;

    ExamPagerAdapter pagerAdapter;
    List<AttemptItem> filterItems = new ArrayList<>();
    PanelListAdapter mPanelAdapter;
    MaterialDialog progressDialog;

    Attempt mAttempt;
    Exam mExam;
    List<AttemptItem> attemptItemList = new ArrayList<AttemptItem>();
    CountDownTimer countDownTimer;

    ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    Boolean mFistTimeCallback = false;
    HashMap<String, Integer> subjects = new HashMap<>();
    int currentOffset;
    boolean navigationButtonPressed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAttempt = getArguments().getParcelable("attempt");
        mExam = getArguments().getParcelable("exam");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.attempt_item_fragment, container, false);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        progressDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .show();
        pager.setPagingEnabled(false);
        previous.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        mLayout.setEnabled(true);
        mLayout.setTouchEnabled(false);
        mPanelAdapter = new PanelListAdapter(getActivity().getLayoutInflater(), filterItems, R.layout.panel_list_item);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(), getActivity().getResources(), false);
        subjectFilter.setAdapter(mTopLevelSpinnerAdapter);
        subjectFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (!mFistTimeCallback) {
                    mFistTimeCallback = true;
                    return;
                }
                if (attemptItemList.get(pager.getCurrentItem()).hasChanged()) {  //save current answer if changed
                    try {
                        attemptItemList.get(pager.getCurrentItem()).saveResult(getActivity(), serviceProvider);
                    } catch (Exception e) {
                    }
                }
                String subject = mTopLevelSpinnerAdapter.getTag(position);
                if (navigationButtonPressed) {         //spinner item changed by clicking next or prev button
                    navigationButtonPressed = false;
                } else {
                    pager.setCurrentItem(subjects.get(subject));   //spinner item changed by changed by selecting in spinner
                }
                int currentPosition = pager.getCurrentItem();
                if (currentPosition > 0) {   //setting status of prev button
                    previous.setClickable(true);
                    previous.setTextColor(getResources().getColor(R.color.primary));
                } else {
                    previous.setClickable(false);
                    previous.setTextColor(getResources().getColor(R.color.nav_button_disabled));
                }
                if (currentPosition + 1 >= attemptItemList.size()) {  //setting status of next button
                    next.setTextColor(Color.parseColor("#d9534f"));
                    next.setText(R.string.end);
                } else {
                    next.setTextColor(getResources().getColor(R.color.primary));
                    next.setText(R.string.next);
                }
                currentOffset = subjects.get(subject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerContainer.setVisibility(View.GONE);
        return view;
    }

    @OnClick(R.id.question_list) void openPanel() {
        if(mLayout.getPanelState().equals(PanelState.EXPANDED)) {
            collapsePanel();
        }
        else {
            expandPanel();
        }
        questionsListView.setAdapter(mPanelAdapter);
        String[] filters= { "All", "Answered", "Unanswered", "Marked for review" };
        ArrayAdapter<String> adapter =new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long
                    l) {
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

        if (pager.getCurrentItem() < (pagerAdapter.getCount() - 1)) {
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }

        if(mExam.getTemplateType() == 2) {  //check whether navigated to next subject if true change the spinner item
            if (currentOffset != subjects.get(attemptItemList.get(pager.getCurrentItem()).getAttemptQuestion().getSubject())) {
                navigationButtonPressed = true;
                subjectFilter.setSelection(subjectFilter.getSelectedItemPosition() + 1);
                return;
            }
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

        if(mExam.getTemplateType() == 2) {  //check whether navigated to prev subject if true change the spinner item
            if (currentOffset != subjects.get(attemptItemList.get(pager.getCurrentItem()).getAttemptQuestion().getSubject())) {
                navigationButtonPressed = true;
                subjectFilter.setSelection(subjectFilter.getSelectedItemPosition() - 1);
                return;
            }
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
        new MaterialDialog.Builder(getActivity())
                .title(R.string.end_message)
                .positiveText(R.string.end)
                .negativeText(R.string.cancel)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        endExam.execute();
                    }
                })
                .show();
    }

    @OnClick(R.id.pause_exam) void pauseExam() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.pause_message)
                .content(R.string.pause_content)
                .positiveText(R.string.pause)
                .negativeText(R.string.cancel)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        countDownTimer.cancel();
                        returnToHistory();
                    }
                })
                .show();
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
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if(mExam.getTemplateType() == 2) {  //for IBPS templates only
            List<String> subjectsList = new ArrayList<>(); // Used to get subjects in order as it fetched
//          To Populate the spinner with the subjects
            HashMap<String, List<AttemptItem>> subjectsWiseItems = new HashMap<>();
            for (AttemptItem item : items) {
                if (item.getAttemptQuestion().getSubject() == null || item.getAttemptQuestion().getSubject().isEmpty()) {  //if subject is empty subject = "Uncategorized"
                    item.getAttemptQuestion().setSubject("Uncategorized");
                    Ln.e("Setting question subject to Uncategorized");
                }
                Ln.e("Subject of question is " + item.getAttemptQuestion().getSubject());
                if (subjectsWiseItems.containsKey(item.getAttemptQuestion().getSubject())) { //check subject is already added if added simply add the item it
                    subjectsWiseItems.get(item.getAttemptQuestion().getSubject()).add(item);
                } else {
                    subjectsWiseItems.put(item.getAttemptQuestion().getSubject(), new ArrayList<AttemptItem>()); //else add the subject & then add item to it
                    subjectsWiseItems.get(item.getAttemptQuestion().getSubject()).add(item);
                    subjectsList.add(item.getAttemptQuestion().getSubject());
                }
            }

            //store each set of subject items to attemptItemList
            for (String subject : subjectsList) {
                subjects.put(subject, attemptItemList.size());   //add subjects & it starting point
                attemptItemList.addAll(subjectsWiseItems.get(subject));
                mTopLevelSpinnerAdapter.addItem(subject, subject, true, 0);
            }

            if ((spinnerContainer.getVisibility() == View.GONE) && subjectsWiseItems.size() > 1) {  //show spinner only if #subjects > 1
                mTopLevelSpinnerAdapter.notifyDataSetChanged();
                spinnerContainer.setVisibility(View.VISIBLE);
            }

            subjectFilter.setSelection(0); //set 1st item as default selection
            currentOffset = 0;
        } else {
            attemptItemList =items;
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
        Intent intent = new Intent(getActivity(), ExamsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("currentItem", "2");
        startActivity(intent);
        getActivity().finish();
    }

    protected void showReview() {
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.putExtra("previousActivity", "ExamActivity");
        intent.putExtra("exam", mExam);
        intent.putExtra("attempt", mAttempt);
        intent.putExtra("isDeepLink", getArguments().getBoolean("isDeepLink", false));
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
            else {
                unanswereditems.add(attemptItemList.get(i));
            }
        }
        switch (filter) {
            case "answered":
                filterItems = answereditems;
                break;
            case "unanswered":
                filterItems = unanswereditems;
                break;
            case "marked":
                filterItems = markeditems;
                break;
            default:
                filterItems = attemptItemList;
                break;
        }
        mPanelAdapter.setItems(filterItems.toArray());
    }
}

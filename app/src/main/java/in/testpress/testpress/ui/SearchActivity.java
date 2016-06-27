package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.ExamPager;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.UIUtils;
import info.hoang8f.widget.FButton;

public class SearchActivity extends AppCompatActivity implements AbsListView.OnScrollListener,
        LoaderManager.LoaderCallbacks<List<Exam>> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.search_bar) EditText searchBar;
    @InjectView(R.id.left_drawable) ImageView leftDrawable;
    @InjectView(R.id.right_drawable) ImageView rightDrawable;
    @InjectView(R.id.result_list_card) CardView resultsLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) FButton retryButton;
    @InjectView(android.R.id.list) ListView listView;
    private static final int SPEECH_RESULT = 111;
    String queryText = "";
    String subclass = "";
    protected boolean listShown = true;
    protected List<Exam> items = Collections.emptyList();
    protected ExamPager pager;
    View loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Injector.inject(this);
        ButterKnife.inject(this);
        subclass = getIntent().getStringExtra("subclass");
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchBar.getText().toString().isEmpty()) {
                    leftDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
                    if (!SpeechRecognizer.isRecognitionAvailable(SearchActivity.this)) {
                        rightDrawable.setVisibility(View.GONE);
                    } else {
                        rightDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_black_24dp));
                    }
                } else {
                    if (rightDrawable.getVisibility() == View.GONE) {
                        rightDrawable.setVisibility(View.VISIBLE);
                    }
                    rightDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_black_24dp));
                    leftDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white_24dp));
                }
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onClickSearch();
                    return true;
                }
                return false;
            }
        });
        listView.setAdapter(createAdapter());
        loadingLayout = LayoutInflater.from(this).inflate(R.layout.loading_layout, null);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            rightDrawable.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.left_drawable) void onClickLeftDrawable() {
        if (searchBar.getText().toString().isEmpty()) { //Work as back arrow
            finish();
        } else { // Work as search icon
            onClickSearch();
        }
    }

    void onClickSearch() {
        queryText = searchBar.getText().toString().trim();
        if (queryText.length() != 0) {
            UIUtils.hideSoftKeyboard(this);
            refreshWithProgress();
        }
    }

    @OnClick(R.id.right_drawable) void onClickRightDrawable() {
        if (searchBar.getText().toString().isEmpty()) { //Work as speech recognizer
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(intent, SPEECH_RESULT);
                searchBar.setText("");
            } catch (ActivityNotFoundException a) {
                Toaster.showShort(this, getResources().getString(R.string.speech_recognition_not_supported));
            }
        } else { //Work as clear button
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                rightDrawable.setVisibility(View.GONE);
            } else {
                rightDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_black_24dp));
            }
            searchBar.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RESULT: {
                if (resultCode == MainActivity.RESULT_OK && null != data) {
                    searchBar.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
                    searchBar.setSelection(searchBar.getText().length());
                    searchBar.requestFocus();
                    onClickSearch();
                }
                break;
            }
        }
    }

    protected ExamPager getPager() {
        if (pager == null) {
            try {
                pager = new ExamPager(subclass, serviceProvider.getService(this));
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return pager;
    }

    @Override
    public Loader<List<Exam>> onCreateLoader(int id, Bundle args) {
        return new ThrowableLoader<List<Exam>>(this, items) {

            @Override
            public List<Exam> loadData() throws IOException {
                getPager().setQueryParams(Constants.Http.SEARCH_QUERY, queryText);
                getPager().next();
                return getPager().getResources();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Exam>> loader, List<Exam> items) {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
        }
        final Exception exception = getException(loader);
        if (exception != null) {
            Toaster.showLong(this, getErrorMessage(exception));
            setListShown(true);
            return;
        }
        if (items.isEmpty()) {
            setEmptyText(R.string.no_results_found, R.string.try_with_other_keyword, R.drawable.ic_error_outline_black_18dp);
            retryButton.setVisibility(View.GONE);
        }
        this.items = items;
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        setListShown(true);
    }

    @Override
    public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount, int totalItemCount)
    {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {  //if pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
            return;
        }
        if (getSupportLoaderManager().hasRunningLoaders())
            return;
        if (listView != null && pager != null
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            if(getListAdapter().getFootersCount() == 0) { //display loading footer if not present when loading next page
                getListAdapter().addFooter(loadingLayout);
            }
            refresh();
        }
    }

    @OnClick(R.id.retry_button)
    protected void refreshWithProgress() {
        if (pager != null) {
            getPager().reset();
        }
        pager = getPager();
        items.clear();
        setListShown(false);
        refresh();
    }

    private void refresh() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    protected HeaderFooterListAdapter<SingleTypeAdapter<Exam>> createAdapter() {
        SingleTypeAdapter<Exam> wrapped = null;
        if (subclass == null || subclass.equals("available")) {
            wrapped = new AvailableExamsListAdapter(this, items, R.layout.available_exams_list_item);
        } else if (subclass.equals("upcoming")) {
            wrapped = new UpcomingExamsListAdapter(getLayoutInflater(), items, R.layout.upcoming_exams_list_item);
        } else if (subclass.equals("history")) {
            wrapped = new HistoryListAdapter(this, items, R.layout.history_exams_list_item);
        }
        return new HeaderFooterListAdapter<SingleTypeAdapter<Exam>>(listView, wrapped);
    }

    public void setListShown(final boolean shown) {
        if (shown == listShown) {
            if (shown) {
                // List has already been shown so hide/show the empty view with
                // no fade effect
                if (items.isEmpty()) {
                    hide(resultsLayout).show(emptyView);
                } else {
                    hide(emptyView).show(resultsLayout);
                }
            }
            return;
        }
        listShown = shown;
        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).show(resultsLayout);
            } else {
                hide(progressBar).hide(resultsLayout).show(emptyView);
            }
        } else {
            hide(resultsLayout).hide(emptyView).show(progressBar);
        }
    }

    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.handleForbidden(this, serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else if (exception.getCause() instanceof UnknownHostException) {
            setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
            return R.string.no_internet;
        } else {
            setEmptyText(R.string.error_loading_exams, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.error_loading_exams;
    }

    protected SearchActivity show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    protected SearchActivity hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    protected HeaderFooterListAdapter<SingleTypeAdapter<Exam>> getListAdapter() {
        return (HeaderFooterListAdapter<SingleTypeAdapter<Exam>>) listView
                .getAdapter();
    }

    protected Exception getException(final Loader<List<Exam>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Exam>>) loader).clearException();
        } else {
            return null;
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<Exam>> loader) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

}

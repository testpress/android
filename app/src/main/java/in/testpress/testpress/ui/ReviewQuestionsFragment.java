package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.core.ReviewQuestionsPager;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.ReviewItem;

public class ReviewQuestionsFragment extends PagedItemFragment<ReviewItem> {
    Attempt attempt;
    Exam exam;
    String filter;
    @Inject protected TestpressServiceProvider serviceProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        setHasOptionsMenu(true);
        this.exam = getArguments().getParcelable("exam");
        this.attempt = getArguments().getParcelable("attempt");
        filter = getArguments().getString("filter");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_questions, R.string.no_questions_to_review, R.drawable.ic_help_outline_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
    }

    @Override
    protected SingleTypeAdapter<ReviewItem> createAdapter(List<ReviewItem> items) {
        return new ReviewListAdapter(R.layout.review_question, getActivity().getLayoutInflater(), items, getActivity());
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_questions;
    }

    @Override
    protected ResourcePager<ReviewItem> getPager() {
        if (pager == null) {
            try {
                pager = new ReviewQuestionsPager(attempt, filter, serviceProvider.getService(getActivity()));
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pager;
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        // Do nothing. As options menu is already inflated with retake option.
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (getActivity() == null) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.retake:
                if (exam.getAllowRetake() == true) {
                    Intent intent = new Intent(getActivity(), ExamActivity.class);
                    intent.putExtra("exam", exam);
                    startActivity(intent);
                    return true;
                } else {
                    Toaster.showShort(getActivity(), "Retakes are not allowed for this exam.");
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
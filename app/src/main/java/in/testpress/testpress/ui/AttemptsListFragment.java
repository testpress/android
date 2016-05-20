package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.AttemptsPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.util.Ln;

public class AttemptsListFragment extends PagedItemFragment<Attempt> {

    Exam exam;
    @Inject protected TestpressServiceProvider serviceProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        exam = getArguments().getParcelable("exam");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_attempts, R.string.no_attempts_description, R.drawable.ic_error_outline_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);


    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected ResourcePager<Attempt> getPager() {
        if (pager == null) {
            String state = getArguments().getString("state");
            try {
                pager = new AttemptsPager(exam, serviceProvider.getService(getActivity()));
                if (state != null && state.equals("paused")) {
                    pager.setQueryParams("state", "paused");
                }
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Attempt> createAdapter(List<Attempt> items) {
        return new AttemptsListAdapter(getActivity(), items, exam, R.layout.attempts_list_item);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Attempt attempt = ((Attempt) l.getItemAtPosition(position));
        if (attempt.getState().equals("Running")) {
            //Show Start Exam Activity
        } else {
            Intent intent = new Intent(getActivity(), ReviewActivity.class);
            intent.putExtra("exam", exam);
            intent.putExtra("attempt", attempt);
            startActivity(intent);
        }
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_attempts;
    }
}

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
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.AttemptsPager;
import in.testpress.testpress.core.ExamPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;

public class AttemptsListFragment extends PagedItemFragment<Attempt> {

    Exam exam;
    AttemptsPager pager;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        exam = getArguments().getParcelable("exam");
        try {
            pager = new AttemptsPager(exam, serviceProvider.getService(getActivity()));
        } catch (AccountsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_news);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);


    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isUsable()) {
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

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.attempt_actions, optionsMenu);
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected ResourcePager<Attempt> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Attempt> createAdapter(List<Attempt> items) {
        return new AttemptsListAdapter(getActivity().getLayoutInflater(), items, R.layout.attempts_list_item);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Attempt attempt = ((Attempt) l.getItemAtPosition(position));
        if (attempt.getState().equals("Running")) {
            //Show Start Exam Activity
            Intent intent = new Intent(getActivity(), ExamActivity.class);
            intent.putExtra("exam", exam);
            intent.putExtra("attempt", attempt);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), ReviewActivity.class);
            intent.putExtra("exam", exam);
            intent.putExtra("attempt", attempt);
            startActivity(intent);
        }
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_news;
    }
}

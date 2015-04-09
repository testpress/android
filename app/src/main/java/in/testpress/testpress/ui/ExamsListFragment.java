package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ExamPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Exam;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class ExamsListFragment extends PagedItemFragment<Exam> {

    String subclass;
    ExamPager pager;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        subclass = getArguments().getString("subclass");
        try {
            pager = new ExamPager(subclass, serviceProvider.getService(getActivity()));
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
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    protected ResourcePager<Exam> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        int layout = R.layout.available_exams_list_item;
        if (subclass != null) {
            if (subclass.equals("available")) {
                layout = R.layout.available_exams_list_item;
            } else if (subclass.equals("upcoming")) {
                layout = R.layout.available_exams_list_item;
                //return serviceProvider.getService(getActivity()).getUpcomingExams();
            } else if (subclass.equals("history")) {
                layout = R.layout.available_exams_list_item;
            } else {
                layout = R.layout.available_exams_list_item;
            }
        }
        return new ExamsListAdapter(getActivity().getLayoutInflater(), items, layout);
    }

  public void onListItemClick(ListView l, View v, int position, long id) {
      Exam exam = ((Exam) l.getItemAtPosition(position));

      if(subclass.equals("available")) {
          Intent intent = new Intent(getActivity(), ExamActivity.class);
          intent.putExtra("exam", exam);
          startActivity(intent);
      }

      if(subclass.equals("history")) {
          Intent intent = new Intent(getActivity(), ReviewActivity.class);
          intent.putExtra("exam", exam);
          startActivity(intent);
      }
   }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_news;
    }
}

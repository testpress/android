package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.AbsListView;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Exam;

public class ReviewFragment extends ItemListFragment<Exam> {

    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
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
    public Loader<List<Exam>> onCreateLoader(int id, final Bundle args) {
        final List<Exam> initialItems = items;
        return new ThrowableLoader<List<Exam>>(getActivity(), items) {

            @Override
            public List<Exam> loadData() throws Exception {
                try {
                    return serviceProvider.getService(getActivity()).getExams(Constants.Http.URL_AVAILABLE_EXAMS_FRAG).getResults();
                } catch (OperationCanceledException e) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                }
            }
        };
    }

    @Override
    protected SingleTypeAdapter<Exam> createAdapter(List<Exam> items) {
        int layout = R.layout.available_exams_list_item;
        return new ExamsListAdapter(getActivity().getLayoutInflater(), items, layout);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_news;
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }
}

package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.DocumentsPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Notes;

public class DocumentsListFragment extends PagedItemFragment<Notes> {

    @Inject protected TestpressServiceProvider serviceProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_documents, R.string.no_documents_description, R.drawable.ic_description_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(true);
    }

    @Override
    protected ResourcePager<Notes> getPager() {
        if (pager == null) {
            try {
                pager = new DocumentsPager(serviceProvider.getService(getActivity()));
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Notes> createAdapter(List<Notes> items) {
        return new DocumentsListAdapter(getActivity(), serviceProvider, items, R.layout.documents_list_item);
    }

    @Override
    public void onLoadFinished(Loader<List<Notes>> loader, List<Notes> items) {
        //Return if no items are returned
        if (items.isEmpty()) {
            setEmptyText(R.string.no_documents, R.string.no_documents_description, R.drawable.ic_description_black_18dp);
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
        return R.string.error_loading_documents;
    }
}

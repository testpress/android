package in.testpress.testpress.ui;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import in.testpress.testpress.BootstrapServiceProvider;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.CheckIn;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class CheckInsListFragment extends ItemListFragment<CheckIn> {

    @Inject protected BootstrapServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    protected void configureList(final Activity activity, final ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);

        getListAdapter()
                .addHeader(activity.getLayoutInflater()
                        .inflate(R.layout.checkins_list_item_labels, null));
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
    public Loader<List<CheckIn>> onCreateLoader(final int id, final Bundle args) {
        final List<CheckIn> initialItems = items;
        return new ThrowableLoader<List<CheckIn>>(getActivity(), items) {

            @Override
            public List<CheckIn> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        return serviceProvider.getService(getActivity()).getCheckIns();
                    } else {
                        return Collections.emptyList();
                    }
                } catch (final OperationCanceledException e) {
                    final Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                }
            }
        };
    }

    @Override
    protected SingleTypeAdapter<CheckIn> createAdapter(final List<CheckIn> items) {
        return new CheckInsListAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final CheckIn checkIn = ((CheckIn) l.getItemAtPosition(position));

        final String uri = String.format("geo:%s,%s?q=%s",
                checkIn.getLocation().getLatitude(),
                checkIn.getLocation().getLongitude(),
                checkIn.getName());

        // Show a chooser that allows the user to decide how to display this data, in this case, map data.
        startActivity(Intent.createChooser(
                new Intent(Intent.ACTION_VIEW, Uri.parse(uri)), getString(R.string.choose))
        );
    }

    @Override
    protected int getErrorMessage(final Exception exception) {
        return R.string.error_loading_checkins;
    }
}

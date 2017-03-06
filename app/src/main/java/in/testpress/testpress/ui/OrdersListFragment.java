package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.OrdersPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Order;

public class OrdersListFragment extends PagedItemFragment<Order> {

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        ButterKnife.inject(this.getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_orders, R.string.no_orders_description, R.drawable.ic_shopping_cart_black_18dp);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.list_divider));
        listView.setDivider(sage);
        listView.setDividerHeight(1);
    }

    @Override
    public void onLoadFinished(Loader<List<Order>> loader, List<Order> items) {

        //Return if no items are returned
        if (items.isEmpty()) {
            setEmptyText(R.string.no_orders, R.string.no_orders_description, R.drawable.ic_shopping_cart_black_18dp);
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
    }

    protected ResourcePager<Order> getPager() {
        if (pager == null) {
            try {
                pager = new OrdersPager(serviceProvider.getService(getActivity()));
            } catch (AccountsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Order> createAdapter(List<Order> items) {
        return new OrdersListAdapter(getActivity().getLayoutInflater(), items, R.layout.order_list_inner_content);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.logout(getActivity(), testpressService, serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.error_loading_orders;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }
}

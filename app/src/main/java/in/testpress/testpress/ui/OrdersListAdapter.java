package in.testpress.testpress.ui;

import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.util.FormatDate;

public class OrdersListAdapter extends SingleTypeAdapter<Order> {
    /**
     * @param inflater
     * @param items
     */
    public OrdersListAdapter(final LayoutInflater inflater, final List<Order> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.orderId,
                R.id.status, R.id.date, R.id.price};
    }

    @Override
    protected void update(final int position, final Order item) {
        if(item.getOrderItems().size() != 0)
            setText(0, item.getOrderItems().get(0).getProduct().split("/")[6]);
        setText(1, "OrderId: " + item.getOrderId());
        setText(2, "Status: " + item.getStatus());
        FormatDate date = new FormatDate();
        setText(3, date.formatDateTime(item.getDate()));
        setText(4, "₹ " + item.getAmount());
    }
}

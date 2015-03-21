
package in.testpress.testpress.ui;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;

import java.util.ArrayList;

/**
 * Utility adapter that supports adding headers and footers
 *
 * @param <E>
 */
public class HeaderFooterListAdapter<E extends BaseAdapter> extends
        HeaderViewListAdapter {

    private final ListView list;

    private final ArrayList<FixedViewInfo> headers;

    private final ArrayList<FixedViewInfo> footers;

    private final E wrapped;

    /**
     * Create header footer adapter
     *
     * @param view
     * @param adapter
     */
    public HeaderFooterListAdapter(final ListView view, final E adapter) {
        this(new ArrayList<FixedViewInfo>(), new ArrayList<FixedViewInfo>(),
                view, adapter);
    }

    private HeaderFooterListAdapter(final ArrayList<FixedViewInfo> headerViewInfos,
                                    final ArrayList<FixedViewInfo> footerViewInfos, final ListView view, final E adapter) {
        super(headerViewInfos, footerViewInfos, adapter);

        headers = headerViewInfos;
        footers = footerViewInfos;
        list = view;
        wrapped = adapter;
    }
    
   /**
     * Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged()
    {
        wrapped.notifyDataSetChanged();
    }
    
    /**
     * Add non-selectable header view with no data
     *
     * @param view
     * @return this adapter
     * @see #addHeader(View, Object, boolean)
     */
    public HeaderFooterListAdapter<E> addHeader(final View view) {
        return addHeader(view, null, false);
    }

    /**
     * Add header
     *
     * @param view
     * @param data
     * @param isSelectable
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addHeader(final View view, final Object data,
                                                final boolean isSelectable) {
        final FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        headers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    /**
     * Add non-selectable footer view with no data
     *
     * @param view
     * @return this adapter
     * @see #addFooter(View, Object, boolean)
     */
    public HeaderFooterListAdapter<E> addFooter(final View view) {
        return addFooter(view, null, false);
    }

    /**
     * Add footer
     *
     * @param view
     * @param data
     * @param isSelectable
     * @return this adapter
     */
    public HeaderFooterListAdapter<E> addFooter(final View view, final Object data,
                                                final boolean isSelectable) {
        final FixedViewInfo info = list.new FixedViewInfo();
        info.view = view;
        info.data = data;
        info.isSelectable = isSelectable;

        footers.add(info);
        wrapped.notifyDataSetChanged();
        return this;
    }

    @Override
    public boolean removeHeader(final View v) {
        final boolean removed = super.removeHeader(v);
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    /**
     * Remove all headers
     *
     * @return true if headers were removed, false otherwise
     */
    public boolean clearHeaders() {
        boolean removed = false;
        if (!headers.isEmpty()) {
            final FixedViewInfo[] infos = headers.toArray(new FixedViewInfo[headers
                    .size()]);
            for (final FixedViewInfo info : infos) {
                removed = super.removeHeader(info.view) || removed;
            }
        }
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    /**
     * Remove all footers
     *
     * @return true if headers were removed, false otherwise
     */
    public boolean clearFooters() {
        boolean removed = false;
        if (!footers.isEmpty()) {
            final FixedViewInfo[] infos = footers.toArray(new FixedViewInfo[footers
                    .size()]);
            for (final FixedViewInfo info : infos) {
                removed = super.removeFooter(info.view) || removed;
            }
        }
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    @Override
    public boolean removeFooter(final View v) {
        final boolean removed = super.removeFooter(v);
        if (removed) {
            wrapped.notifyDataSetChanged();
        }
        return removed;
    }

    @Override
    public E getWrappedAdapter() {
        return wrapped;
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }
}

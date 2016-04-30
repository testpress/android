package in.testpress.testpress.ui;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.testpress.testpress.R;

/** Adapter that provides views for our top-level Action Bar spinner. */
class ExploreSpinnerAdapter extends BaseAdapter {
    private int mDotSize;
    private boolean mTopLevel;
    private LayoutInflater inflater;
    private Resources resources;

    ExploreSpinnerAdapter(LayoutInflater inflater, Resources resources, boolean topLevel) {
        this.inflater = inflater;
        this.resources = resources;
        this.mTopLevel = topLevel;
    }

    class ExploreSpinnerItem {
        boolean isHeader;
        String tag, title;
        int color;
        boolean indented;

        ExploreSpinnerItem(boolean isHeader, String tag, String title, boolean indented, int color) {
            this.isHeader = isHeader;
            this.tag = tag;
            this.title = title;
            this.indented = indented;
            this.color = color;
        }
    }

    // pairs of (tag, title)
    private ArrayList<ExploreSpinnerItem> mItems = new ArrayList<ExploreSpinnerItem>();

    public void clear() {
        mItems.clear();
    }

    public void addItem(String tag, String title, boolean indented, int color) {
        mItems.add(new ExploreSpinnerItem(false, tag, title, indented, color));
    }

    public void addHeader(String title) {
        mItems.add(new ExploreSpinnerItem(true, "", title, false, 0));
    }

    public int getItemPosition(String title) {
        for (ExploreSpinnerItem item : mItems) {
            if (item.title.equals(title)) {
                return mItems.indexOf(item);
            }
        }
        return -1;
    }

    public int getItemPositionFromTag(String tag) {
        for (ExploreSpinnerItem item : mItems) {
            if (item.tag.equals(tag)) {
                return mItems.indexOf(item);
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isHeader(int position) {
        return position >= 0 && position < mItems.size()
                && mItems.get(position).isHeader;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.explore_spinner_item_dropdown,
                    parent, false);
            view.setTag("DROPDOWN");
        }

        TextView headerTextView = (TextView) view.findViewById(R.id.header_text);
        View dividerView = view.findViewById(R.id.divider_view);
        TextView normalTextView = (TextView) view.findViewById(android.R.id.text1);

        if (isHeader(position)) {
            headerTextView.setText(getTitle(position));
            headerTextView.setVisibility(View.VISIBLE);
            normalTextView.setVisibility(View.GONE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            headerTextView.setVisibility(View.GONE);
            normalTextView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.GONE);

            setUpNormalDropdownView(position, normalTextView);
        }

        return view;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = inflater.inflate(mTopLevel
                            ? R.layout.explore_spinner_item_actionbar
                            : R.layout.explore_spinner_item,
                    parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));
        return view;
    }

    private String getTitle(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).title : "";
    }

    private int getColor(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).color : 0;
    }

    String getTag(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position).tag : "";
    }

    private void setUpNormalDropdownView(int position, TextView textView) {
        textView.setText(getTitle(position));
        ShapeDrawable colorDrawable = (ShapeDrawable) textView.getCompoundDrawables()[2];
        int color = getColor(position);
        if (color == 0) {
            if (colorDrawable != null) {
                textView.setCompoundDrawables(null, null, null, null);
            }
        } else {
            if (mDotSize == 0) {
                mDotSize = resources.getDimensionPixelSize(
                        R.dimen.tag_color_dot_size);
            }
            if (colorDrawable == null) {
                colorDrawable = new ShapeDrawable(new OvalShape());
                colorDrawable.setIntrinsicWidth(mDotSize);
                colorDrawable.setIntrinsicHeight(mDotSize);
                colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, colorDrawable, null);
            }
            colorDrawable.getPaint().setColor(color);
        }

    }

    @Override
    public boolean isEnabled(int position) {
        return !isHeader(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
}
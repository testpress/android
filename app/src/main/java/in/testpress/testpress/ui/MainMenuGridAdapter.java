package in.testpress.testpress.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import in.testpress.testpress.R;
import in.testpress.testpress.models.InstituteSettings;

public class MainMenuGridAdapter extends BaseAdapter {
    private Context mContext;
    private LinkedHashMap<Integer, Integer> mMenuItemIds;
    private ArrayList<Integer> mMenuItemTitleIds;
    private InstituteSettings instituteSettings;

    public MainMenuGridAdapter(Context c, LinkedHashMap<Integer, Integer> items, InstituteSettings Settings) {
        mContext = c;
        mMenuItemIds = items;
        mMenuItemTitleIds = new ArrayList<>(mMenuItemIds.keySet());
        instituteSettings = Settings;
    }

    @Override
    public int getCount() {
        return mMenuItemIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenuItemTitleIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMenuItemTitleIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = inflater.inflate(R.layout.menu_grid_item, null);
            TextView textView = (TextView) grid.findViewById(R.id.menuName);
            ImageView imageView = (ImageView)grid.findViewById(R.id.menuIcon);
            int titleResId = mMenuItemTitleIds.get(position);
            if (getMenuItemName(titleResId)!="") {
                textView.setText(getMenuItemName(titleResId));
            }
            else {
                textView.setText(titleResId);
            }
            imageView.setImageResource(mMenuItemIds.get(titleResId));
        } else {
            grid = (View) convertView;
        }
        return grid;
    }

    private String getMenuItemName(int titleResId) {
        switch (titleResId) {
            case R.string.documents:
                return instituteSettings.getDocumentsLabel();
            case R.string.store:
                return instituteSettings.getStoreLabel();
            case R.string.posts:
                return instituteSettings.getPostsLabel();
            default:
                return "";
        }
    }

}
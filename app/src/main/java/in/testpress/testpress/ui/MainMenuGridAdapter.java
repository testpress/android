package in.testpress.testpress.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.testpress.testpress.R;

public class MainMenuGridAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] menuItemNames;
    private final int[] menuItemImageId;

    public MainMenuGridAdapter(Context c,String[] menuItemNames,int[] menuItemImageId ) {
        mContext = c;
        this.menuItemImageId = menuItemImageId;
        this.menuItemNames = menuItemNames;
    }

    @Override
    public int getCount() {
        return menuItemNames.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
            textView.setText(menuItemNames[position]);
            imageView.setImageResource(menuItemImageId[position]);
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}
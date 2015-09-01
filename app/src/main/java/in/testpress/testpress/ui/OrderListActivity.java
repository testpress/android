package in.testpress.testpress.ui;

import android.os.Bundle;

import in.testpress.testpress.R;

public class OrderListActivity extends NavigationDrawer {

    @Override
    public void onInt(Bundle savedInstanceState) {
        super.onInt(savedInstanceState);
        initUser();
        with(this)
                .startingPosition(2) //Starting position in the list
                .addAllHelpItem(mHelpLiveo.getHelp())
                .backgroundList(R.color.native_background)
                .build();
    }

    @Override
    public void onItemClick(int position) {
        if(position == 2) {
            initScreen();
        } else {
            super.onItemClick(position);
        }
    }

    protected void initScreen() {

    }
}

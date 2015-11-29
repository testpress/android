package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import in.testpress.testpress.R;

public class MainMenuFragment extends Fragment {

    GridView grid;
    String[] menuItemNames = {
            "My Exams",
            "Store",
            "Orders",
            "Articles",
            "Logout"
    } ;
    int[] menuItemImageId = {
            R.drawable.exams,
            R.drawable.store,
            R.drawable.cart,
            R.drawable.posts,
            R.drawable.logout
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_menu_grid_view, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainMenuGridAdapter adapter = new MainMenuGridAdapter(getActivity(), menuItemNames, menuItemImageId);
        grid=(GridView)view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(getActivity(), ExamsListActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getActivity(), ProductsListActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(getActivity(), OrdersListActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(getActivity(), PostsListActivity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        ((MainActivity)getActivity()).logout();
                        break;
                }
            }
        });
    }
}

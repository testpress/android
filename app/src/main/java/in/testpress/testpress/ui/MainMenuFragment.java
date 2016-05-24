package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

public class MainMenuFragment extends Fragment {

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    GridView grid;
    @InjectView(R.id.recyclerview) RecyclerView recyclerView;
    @InjectView(R.id.quick_links_container)
    LinearLayout quickLinksContainer;
    Account[] account;

    //Menu for authorized users
    String[] menuItemNames = {
            "My Exams",
            "Store",
            "Documents",
//            "Orders",
            "Posts",
            "Profile",
            "Share",
            "Rate Us",
            "Logout"
    } ;
    int[] menuItemImageId = {
            R.drawable.exams,
            R.drawable.store,
            R.drawable.documents,
//            R.drawable.cart,
            R.drawable.posts,
            R.drawable.ic_profile_details,
            R.drawable.share,
            R.drawable.heart,
            R.drawable.logout
    };

    //Menu for unauthorized users
    String[] menuNames = {
            "Store",
            "Posts",
            "Share",
            "Rate Us",
            "Login"
    } ;
    int[] menuImageId = {
            R.drawable.store,
            R.drawable.posts,
            R.drawable.share,
            R.drawable.heart,
            R.drawable.login
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Injector.inject(this);
        return inflater.inflate(R.layout.main_menu_grid_view, null);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        fetchStarredCategories();
        AccountManager manager = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        account = manager.getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        MainMenuGridAdapter adapter;
        if (account.length > 0) {
            adapter = new MainMenuGridAdapter(getActivity(), menuItemNames, menuItemImageId);
        } else {
            adapter = new MainMenuGridAdapter(getActivity(), menuNames, menuImageId);
        }
        grid=(GridView)view.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent intent;
                if (account.length > 0) {
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
                            intent = new Intent(getActivity(), DocumentsListActivity.class);
                            startActivity(intent);
                            break;
//                    case 2:
//                        intent = new Intent(getActivity(), OrdersListActivity.class);
//                        startActivity(intent);
//                        break;
                        case 3:
                            intent = new Intent(getActivity(), PostsListActivity.class);
                            intent.putExtra("userAuthenticated", true);
                            startActivity(intent);
                            break;
                        case 4:
                            intent = new Intent(getActivity(), ProfileDetailsActivity.class);
                            startActivity(intent);
                            break;
                        case 5:
                            //Share
                            shareApp();
                            break;
                        case 6:
                            //Rate
                            rateApp();
                            break;
                        case 7:
                            ((MainActivity) getActivity()).logout();
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            intent = new Intent(getActivity(), ProductsListActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(getActivity(), PostsListActivity.class);
                            intent.putExtra("userAuthenticated", false);
                            startActivity(intent);
                            break;
                        case 2:
                            //Share
                            shareApp();
                            break;
                        case 3:
                            //Rate
                            rateApp();
                            break;
                        default:
                            intent = new Intent(getActivity(), LoginActivity.class);
                            intent.putExtra("deeplinkTo", "home");
                            startActivity(intent);
                            break;
                    }
                }
            }
        });
    }

    void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message) + getActivity().getPackageName());
        startActivity(Intent.createChooser(share, "Share with"));
    }

    void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }

    public void fetchStarredCategories() {
        new SafeAsyncTask<List<Category>>() {
            @Override
            public List<Category> call() throws Exception {
                Map<String, String> queryParams = new LinkedHashMap<String, String>();
                queryParams.put("starred", "true");
                if (account.length > 0) {
                    return serviceProvider.getService(getActivity())
                            .getCategories(Constants.Http.URL_CATEGORIES_FRAG, queryParams).getResults();
                } else {
                    return testpressService
                            .getCategories(Constants.Http.URL_CATEGORIES_FRAG, queryParams).getResults();
                }
            }

            protected void onSuccess(final List<Category> categories) throws Exception {
                Ln.e("On success");
                if (categories.isEmpty() == true) {
                    quickLinksContainer.setVisibility(View.GONE);
                } else {
                    quickLinksContainer.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new StarredCategoryAdapter(getActivity(),
                            categories));
                }
            }
        }.execute();
    }


    public static class StarredCategoryAdapter
            extends RecyclerView.Adapter<StarredCategoryAdapter.ViewHolder> {


        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<Category> mValues;
        public class ViewHolder extends RecyclerView.ViewHolder {
            public Long mCategoryId;

            public final View mView;
            public final TextView categoryView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                categoryView = (TextView) view.findViewById(R.id.category);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + categoryView.getText();
            }
        }

        public Category getValueAt(int position) {
            return mValues.get(position);
        }

        public StarredCategoryAdapter(Context context, List<Category> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_menu_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mCategoryId = mValues.get(position).getId();
            holder.categoryView.setText(mValues.get(position).getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, PostsListActivity.class);
                    intent.putExtra("category_filter", holder.mCategoryId);

                    context.startActivity(intent);
                }
            });
            ShapeDrawable colorDrawable = (ShapeDrawable) holder.categoryView.getCompoundDrawables()[2];
            if (colorDrawable == null) {
                colorDrawable = new ShapeDrawable(new OvalShape());
                int mDotSize = holder.mView.getResources().getDimensionPixelSize(
                        R.dimen.tag_color_dot_size);
                colorDrawable.setIntrinsicWidth(mDotSize);
                colorDrawable.setIntrinsicHeight(mDotSize);
                colorDrawable.getPaint().setStyle(Paint.Style.FILL);
                holder.categoryView.setCompoundDrawablesWithIntrinsicBounds(colorDrawable, null,
                        null, null);
            }
            colorDrawable.getPaint().setColor(Color.parseColor("#" + mValues.get(position).getColor()));
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}

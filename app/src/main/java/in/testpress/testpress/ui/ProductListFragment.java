package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.ProductsPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Product;

public class ProductListFragment extends PagedItemFragment<Product> {

    @Inject protected TestpressService testpressService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        ButterKnife.inject(this.getActivity());
        pager = new ProductsPager(testpressService);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_products, R.string.no_products_description, R.drawable.box);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
    }

    protected ResourcePager<Product> getPager() {
        return pager;
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return new ProductsListAdapter(getActivity().getLayoutInflater(), items, R.layout.product_list_inner_content);
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> items) {

        //Return if no items are returned
        if (items.isEmpty()) {
            setEmptyText(R.string.no_products, R.string.no_products_description, R.drawable.box);
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Product product = ((Product) l.getItemAtPosition(position));
        Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
        startActivity(intent);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
        return R.string.error_loading_products;
    }
}

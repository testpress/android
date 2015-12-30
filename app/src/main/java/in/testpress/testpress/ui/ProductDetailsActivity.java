package in.testpress.testpress.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.payu.india.Payu.PayuConstants;

import java.net.URL;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.InternetConnectivityChecker;

import retrofit.RetrofitError;

public class ProductDetailsActivity extends TestpressFragmentActivity implements LoaderManager.LoaderCallbacks<ProductDetails>{

    @Inject TestpressServiceProvider serviceProvider;
    @InjectView(R.id.thumbnail_image) ImageView image;
    @InjectView(R.id.title) TextView titleText;
    @InjectView(R.id.number_of_exams) TextView numberOfExamsText;
    @InjectView(R.id.number_of_notes) TextView numberOfNotesText;
    @InjectView(R.id.date) TextView dateText;
    @InjectView(R.id.categories) TextView categoriesText;
    @InjectView(R.id.price) TextView priceText;
    @InjectView(R.id.description_title) TextView descriptionTitleText;
    @InjectView(R.id.description) TextView descriptionText;
    @InjectView(R.id.examsTitle) TextView examsTitleText;
    @InjectView(R.id.examListView) ListView examListViewText;
    @InjectView(R.id.notesTitle) TextView notesTitleText;
    @InjectView(R.id.notesListView) ListView notesListViewText;
    @InjectView(R.id.productDetails) RelativeLayout productDetailsView;
    @InjectView(R.id.buyButton) Button buyButton;

    ProgressBar progressBar;
    ProductDetails productDetails;
    String productUrl;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportLoaderManager().initLoader(0, null, this);
        setContentView(R.layout.product_details_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        productUrl = getIntent().getStringExtra("productUrl");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        productDetailsView.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public Loader<ProductDetails> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<ProductDetails>(this, productDetails) {

            @Override
            public ProductDetails loadData() throws Exception {
                String productUrlFragment;
                URL url = new URL(productUrl);
                try {
                    productUrlFragment = url.getFile().substring(1);
                } catch (Exception e) {
                    return null;
                }
                try {
                    return serviceProvider.getService(ProductDetailsActivity.this).getProductDetail(productUrlFragment);
                } catch (RetrofitError retrofitError) {
                    return null;
                }
            }
        };
    }

    public void onLoadFinished(final Loader<ProductDetails> loader, final ProductDetails productDetails) {
        progressBar.setVisibility(View.GONE);
        productDetailsView.setVisibility(View.VISIBLE);
        FormatDate date = new FormatDate();
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.icon)
                .showImageOnFail(R.drawable.icon)
                .showImageOnLoading(R.drawable.icon).build();
        //download and display image from url
        imageLoader.displayImage(productDetails.getImage(), image, options);
        titleText.setText(productDetails.getTitle());
        try {
            if (Float.parseFloat(productDetails.getPrice()) == 0) {
                buyButton.setText("Start Now - Free");
            }
        } catch (Exception e) {
        }
        if(productDetails.getExams().size() != 0) {
            numberOfExamsText.setText(productDetails.getExams().size() + " Exams");
            numberOfExamsText.setVisibility(View.VISIBLE);
        } else {
            numberOfExamsText.setVisibility(View.GONE);
        }
        if(productDetails.getNotes().size() != 0) {
            numberOfNotesText.setText(productDetails.getNotes().size() + " Documents");
            numberOfNotesText.setVisibility(View.VISIBLE);
        } else {
            numberOfNotesText.setVisibility(View.GONE);
        }
        if(date.getDate(productDetails.getStartDate(), productDetails.getEndDate()) != null) {
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(date.getDate(productDetails.getStartDate(), productDetails.getEndDate()));
        }
        String categories = Arrays.toString(productDetails.getCategories().toArray());
        categoriesText.setText(categories.substring(1, categories.length()-1));
        priceText.setText("₹ " + productDetails.getPrice());
        if(!productDetails.getDescription().isEmpty()) {
            descriptionText.setText(productDetails.getDescription());
            descriptionText.setVisibility(View.VISIBLE);
            descriptionTitleText.setVisibility(View.VISIBLE);
        }
        if(!productDetails.getExams().isEmpty()) {
            examsTitleText.setVisibility(View.VISIBLE);
        }
        examListViewText.setFocusable(false);
        examListViewText.setAdapter(new UpcomingExamsListAdapter(this.getLayoutInflater(), productDetails.getExams(), R.layout.upcoming_exams_list_item));
        setListViewHeightBasedOnChildren(examListViewText);
        if(!productDetails.getNotes().isEmpty()) {
            notesTitleText.setVisibility(View.VISIBLE);
        }
        notesListViewText.setFocusable(false);
        notesListViewText.setAdapter(new NotesListAdapter(this.getLayoutInflater(), productDetails.getNotes(), R.layout.upcoming_exams_list_item));
        setListViewHeightBasedOnChildren(notesListViewText);
        this.productDetails = productDetails;
    }

    @OnClick(R.id.buyButton) public void order() {
        InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
        if (internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(ProductDetailsActivity.this, OrderConfirmActivity.class);
            intent.putExtra("productDetails", productDetails);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<ProductDetails> loader) {
        // Intentionally left blank
    }

    //Used to put ListView inside ScrollView
    void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}

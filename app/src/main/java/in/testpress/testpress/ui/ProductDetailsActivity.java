package in.testpress.testpress.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.payu.india.Payu.PayuConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.ProductDetails;
import in.testpress.testpress.models.RawExam;
import in.testpress.testpress.util.FormatDate;

import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;
import in.testpress.util.UIUtils;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;

public class ProductDetailsActivity extends TestpressFragmentActivity
        implements LoaderManager.LoaderCallbacks<ProductDetails> {

    public static final String PRODUCT_SLUG = "productSlug";
    @Inject protected TestpressService testpressService;
    @InjectView(R.id.thumbnail_image) ImageView image;
    @InjectView(R.id.title) TextView titleText;
    @InjectView(R.id.total_exams_container) View totalExamsContainer;
    @InjectView(R.id.total_exams) TextView totalExams;
    @InjectView(R.id.total_notes_container) View totalNotesContainer;
    @InjectView(R.id.total_notes) TextView totalNotes;
    @InjectView(R.id.date) TextView dateText;
    @InjectView(R.id.categories) TextView categoriesText;
    @InjectView(R.id.price) TextView priceText;
    @InjectView(R.id.description_container) View descriptionContainer;
    @InjectView(R.id.description) TextView descriptionText;
    @InjectView(R.id.exams_list_container) View examsListContainer;
    @InjectView(R.id.exams_list) ListView examsListView;
    @InjectView(R.id.notes_list_container) View notesListContainer;
    @InjectView(R.id.notes_list) ListView notesListView;
    @InjectView(R.id.main_content) View productDetailsView;
    @InjectView(R.id.buy_button) Button buyButton;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;

    ProgressBar progressBar;
    ProductDetails productDetails;
    String productSlug;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        if (getIntent().getStringExtra(PRODUCT_SLUG) != null) {
            productSlug = getIntent().getStringExtra(PRODUCT_SLUG);
        } else if (getIntent().getParcelableExtra("productDetails") != null) {
            productSlug = ((ProductDetails) getIntent().getParcelableExtra("productDetails")).getSlug();
        }
        getSupportLoaderManager().initLoader(0, null, this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
    }

    @Override
    public Loader<ProductDetails> onCreateLoader(int id, Bundle bundle) {
        return new ThrowableLoader<ProductDetails>(this, productDetails) {

            @Override
            public ProductDetails loadData() throws Exception {
                return testpressService.getProductDetail(productSlug);
            }
        };
    }

    public static class ProductExamsAdapter extends ArrayAdapter<RawExam> {
        // View lookup cache
        private static class ViewHolder {
            TextView title;
            TextView date;
            TextView noOfQuestions;
            TextView duration;
            TextView courseCategory;
        }

        public ProductExamsAdapter(Context context, List<RawExam> exams) {
            super(context, R.layout.product_exams_list_item, exams);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            RawExam exam = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.product_exams_list_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.exam_title);
                viewHolder.date = (TextView) convertView.findViewById(R.id.exam_date);
                viewHolder.noOfQuestions = (TextView) convertView.findViewById(R.id.number_of_questions);
                viewHolder.duration = (TextView) convertView.findViewById(R.id.exam_duration);
                viewHolder.courseCategory = (TextView) convertView.findViewById(R.id.course_category);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // Populate the data into the template view using the data object
            viewHolder.title.setText(exam.getTitle());
            viewHolder.date.setText(exam.getFormattedStartDate() + " to " + exam.getFormattedEndDate());
            viewHolder.noOfQuestions.setText(exam.getNumberOfQuestionsString());
            viewHolder.duration.setText(exam.getDuration());
            viewHolder.courseCategory.setText(exam.getCourse_category());
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public void onLoadFinished(final Loader<ProductDetails> loader, final ProductDetails productDetails) {
        if (productDetails == null) {
            //noinspection ThrowableResultOfMethodCallIgnored
            Exception exception = ((ThrowableLoader<ProductDetails>) loader).clearException();
            exception.printStackTrace();
            if (exception.getMessage() != null && exception.getMessage().equals("404 NOT FOUND")) {
                gotoMainActivity();
            } else if (exception.getCause() instanceof IOException) {
                setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                        R.drawable.ic_error_outline_black_18dp);
            } else {
                setEmptyText(R.string.error_loading_products, R.string.try_after_sometime,
                        R.drawable.ic_error_outline_black_18dp);
            }
            progressBar.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        productDetailsView.setVisibility(View.VISIBLE);
        FormatDate date = new FormatDate();
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.mipmap.ic_launcher)
                .showImageOnFail(R.mipmap.ic_launcher)
                .showImageOnLoading(R.mipmap.ic_launcher)
                .build();

        //download and display image from url
        imageLoader.displayImage(productDetails.getImages()[0].getOriginal(), image, options);
        titleText.setText(productDetails.getTitle());
        try {
            if (Float.parseFloat(productDetails.getPrice()) == 0) {
                buyButton.setText("Start Now - Free");
            }
        } catch (Exception e) {
        }

        if(productDetails.getExams().size() != 0) {
            totalExams.setText(productDetails.getExams().size() + " Exams");
            totalExamsContainer.setVisibility(View.VISIBLE);
        } else {
            totalExamsContainer.setVisibility(View.GONE);
        }

        if(productDetails.getNotes().size() != 0) {
            totalNotes.setText(productDetails.getNotes().size() + " Documents");
            totalNotesContainer.setVisibility(View.VISIBLE);
        } else {
            totalNotesContainer.setVisibility(View.GONE);
        }

        if(date.getDate(productDetails.getStartDate(), productDetails.getEndDate()) != null) {
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(date.getDate(productDetails.getStartDate(), productDetails.getEndDate()));
        }

        //Price & Categories
        String categories = Arrays.toString(productDetails.getCategories().toArray());
        categoriesText.setText(categories.substring(1, categories.length() - 1));
        priceText.setText(productDetails.getPrice());

        //Update product description
        if(productDetails.getDescription().isEmpty()) {
            descriptionContainer.setVisibility(View.GONE);
        } else {
            descriptionContainer.setVisibility(View.VISIBLE);
            Spanned html = Html.fromHtml(productDetails.getDescription(), new UILImageGetter(descriptionText, this), null);
            ZoomableImageString zoomableImageHtml = new ZoomableImageString(this);
            descriptionText.setText(zoomableImageHtml.convertString(html), TextView.BufferType
                    .SPANNABLE);
            descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
            descriptionText.setVisibility(View.VISIBLE);
        }

        //Update exams list
        if(productDetails.getExams().isEmpty()) {
            examsListContainer.setVisibility(View.GONE);
        } else {
            examsListContainer.setVisibility(View.VISIBLE);
            examsListView.setFocusable(false);
            examsListView.setAdapter(new ProductExamsAdapter(this.getApplicationContext(),
                    productDetails.getExams()));
            setListViewHeightBasedOnChildren(examsListView);
        }

        //Update notes list
        if(productDetails.getNotes().isEmpty()) {
            notesListContainer.setVisibility(View.GONE);
        } else {
            notesListContainer.setVisibility(View.VISIBLE);
            notesListView.setFocusable(false);
            notesListView.setAdapter(new NotesListAdapter(this.getLayoutInflater(),
                    productDetails.getNotes(), R.layout.product_notes_list_item));
            setListViewHeightBasedOnChildren(notesListView);
        }

        this.productDetails = productDetails;
    }

    @OnClick(R.id.buy_button) public void order() {
        Ln.e("Buy Button Clicked");
        if (productDetails == null) {
            return;
        }
        if (this.productDetails.getPaymentLink().isEmpty()) {
            AccountManager manager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
            Account[] account = manager.getAccountsByType(APPLICATION_ID);
            if (account.length > 0) {
                Intent intent = new Intent(ProductDetailsActivity.this, OrderConfirmActivity.class);
                intent.putExtra("productDetails", productDetails);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constants.DEEP_LINK_TO, Constants.DEEP_LINK_TO_PAYMENTS);
                intent.putExtra("productDetails", productDetails);
                startActivity(intent);
            }
        } else {
            Uri uri = Uri.parse(this.productDetails.getPaymentLink()); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
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
    public void onBackPressed() {
        if(getIntent().getBooleanExtra(Constants.IS_DEEP_LINK, false)) {
            gotoMainActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
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

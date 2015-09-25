package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.ProductsPager;
import in.testpress.testpress.core.ResourcePager;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.util.FormatDate;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

public class ProductNativeGridBaseFragment extends PagedItemFragment<Product> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    ExploreSpinnerAdapter mTopLevelSpinnerAdapter;
    View mSpinnerContainer;
    Boolean mFistTimeCallback = false;
    ProductsPager pager;
    //List of courses got from the api. This is used to populate the spinner.
    ArrayList<String> courses = new ArrayList<String>();
    ArrayList<Card> cards = new ArrayList<Card>();
    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisc(true).resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.drawable.icon)
            .imageScaleType(ImageScaleType.EXACTLY)
            .showImageOnFail(R.drawable.icon)
            .showImageOnLoading(R.drawable.icon).build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Injector.inject(this);
        ButterKnife.inject(this.getActivity());
        try {
            pager = new ProductsPager(serviceProvider.getService(getActivity()));
        } catch (AccountsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        mTopLevelSpinnerAdapter = new ExploreSpinnerAdapter(getActivity().getLayoutInflater(), getActivity().getResources(), true);
        mTopLevelSpinnerAdapter.addItem("", getString(R.string.all_categories), false, 0);
        Toolbar toolbar = ((ProductsListActivity)(getActivity())).getActionBarToolbar();
        mSpinnerContainer = getActivity().getLayoutInflater().inflate(R.layout.actionbar_spinner, toolbar, false);
        Spinner spinner = (Spinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(mTopLevelSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                if (!mFistTimeCallback) {
                    mFistTimeCallback = true;
                    return;
                }
                String filter = mTopLevelSpinnerAdapter.getTag(position);
                if (filter.isEmpty()) {
                    pager.removeQueryParams("category");
                } else {
                    pager.setQueryParams("category", filter);
                }
                refreshWithProgress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mSpinnerContainer.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_card_grid_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_products);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void setElements(View view){
        gridView = (CardGridView) view.findViewById(R.id.carddemo_grid_base1);
    }

    @Override
    public void onLoadFinished(Loader<List<Product>> loader, List<Product> items) {

        //Return if no items are returned
        if (items.isEmpty()) {
            setEmptyText(R.string.no_products);
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
        //Populate the spinner with the courses
        List<String> coursesList = new ArrayList<String>();
        for (final Product product : items) {
            for (String course : product.getCategories()) {
                coursesList.add(course);
            }
        }
        Set<String> uniqueCourses = new HashSet<String>(coursesList);
        for (final String course : uniqueCourses) {
            // Do not add the course if already present in the spinner
            if (!courses.contains(course)) {
                courses.add(course);
                mTopLevelSpinnerAdapter.addItem(course, course, true, 0);
            }
        }
        if ((mSpinnerContainer.getVisibility() == View.GONE) && !uniqueCourses.isEmpty()){
            mSpinnerContainer.setVisibility(View.VISIBLE);
            Toolbar toolbar = ((ProductsListActivity)(getActivity())).getActionBarToolbar();
            View view = toolbar.findViewById(R.id.actionbar_spinnerwrap);
            toolbar.removeView(view);
            toolbar.invalidate();
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(mSpinnerContainer, lp);
        }
    }

    protected ResourcePager<Product> getPager() {
        return pager;
    }

    @Override
    protected void refreshWithProgress() {
        if(cardArrayAdapter != null)
        cardArrayAdapter.clear();
        super.refreshWithProgress();
    }

    @Override
    protected void setScroll(){
        gridView.setOnScrollListener(this);
        gridView.setFastScrollEnabled(true);
    }

    @Override
    protected void setItemsToAdapter(){
        cardArrayAdapter=new CardGridArrayAdapter(getActivity(),initCards());
        cardArrayAdapter.setInnerViewTypeCount(1);
        gridView.setAdapter(cardArrayAdapter);
    }

    protected ArrayList<Card> initCards() {
        for (int i = 0; i < items.size(); i++) {
            final Product product = items.get(i);
            ProductCard card = new ProductCard(getActivity());
            card.mainTitle = product.getTitle();
            card.numberOfExams = product.getExamsCount();
            card.notesCount = product.getNotesCount();
            card.startDate = product.getStartDate();
            card.endDate = product.getEndDate();
            card.categories = product.getCategories();
            card.price = product.getPrice();
            MyThumbnail thumb = new MyThumbnail(getActivity(), product.getImage());
            thumb.setExternalUsage(true);
            card.addCardThumbnail(thumb);
            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card clickedCard, View view) {
                    if(internetConnectivityChecker.isConnected()) {
                        Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                        intent.putExtra("productUrl", product.getUrl());
                        startActivity(intent);
                    } else {
                        internetConnectivityChecker.showAlert();
                    }
                }
            });
            cards.add(card);
        }
        return cards;
    }

    class ProductCard extends Card {

        String mainTitle;
        int numberOfExams;
        int notesCount;
        String startDate;
        String endDate;
        List<String> categories;
        String price;

        public ProductCard(Context context) {
            super(context, R.layout.grid_native_inner_content);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            FormatDate date = new FormatDate();
            TextView titleTextView = (TextView) parent.findViewById(R.id.title);
            TextView examsCountTextView = (TextView) parent.findViewById(R.id.number_of_exams);
            TextView notesCountTextView = (TextView) parent.findViewById(R.id.number_of_notes);
            TextView dateTextView = (TextView) parent.findViewById(R.id.date);
            TextView categoriesTextView = (TextView) parent.findViewById(R.id.categories);
            TextView priceTextView = (TextView) parent.findViewById(R.id.price);
            titleTextView.setText(mainTitle);
            titleTextView.setSelected(true);
            examsCountTextView.setText(numberOfExams + " Exams");
            notesCountTextView.setText(notesCount + " Documents");
            if(date.getDate(startDate,endDate) != null) {
                dateTextView.setVisibility(View.VISIBLE);
                dateTextView.setText(date.getDate(startDate, endDate));
            } else {
                dateTextView.setVisibility(View.GONE);
            }
            categoriesTextView.setText(Arrays.toString(categories.toArray()));
            categoriesTextView.setSelected(true);
            priceTextView.setText("₹ " + price);
        }
    }

    class MyThumbnail extends CardThumbnail {
        String imageUrl;

        MyThumbnail(Context context,String imageUrl) {
            super(context);
            this.imageUrl = imageUrl;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {
            ImageView image = (ImageView) viewImage;
            imageLoader.displayImage(imageUrl,image,options); //download image from url & set to imageView using universal loader
        }
    }

    public ItemListFragment<Product> setListShown(final boolean shown, final boolean animate) {
        if (!isUsable()) {
            return this;
        }
        if (shown == listShown) {
            if (shown) {
                // List has already been shown so hide/show the empty view with
                // no fade effect
                if (items.isEmpty()) {
                    hide(gridView).show(emptyView);
                } else {
                    hide(emptyView).show(gridView);
                }
            }
            return this;
        }
        listShown = shown;
        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).fadeIn(gridView, animate)
                        .show(gridView);
            } else {
                hide(progressBar).hide(gridView).fadeIn(emptyView, animate)
                        .show(emptyView);
            }
        } else {
            hide(gridView).hide(emptyView).fadeIn(progressBar, animate)
                    .show(progressBar);
        }
        return this;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        if((exception.getMessage() != null) && (exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.handleForbidden(getActivity(), serviceProvider, logoutService);
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.no_internet);
        }
        return R.string.error_loading_products;
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return null;
    }
}

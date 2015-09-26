package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import it.gmariotti.cardslib.library.internal.CardThumbnail;

public class ProductListFragment extends PagedItemFragment<Product> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    ProductsPager pager;
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(R.string.no_products);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
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
            setEmptyText(R.string.no_products);
            super.onLoadFinished(loader, items);
            return;
        }
        super.onLoadFinished(loader, items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Product product = ((Product) l.getItemAtPosition(position));
        if(internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
            intent.putExtra("productUrl", product.getUrl());
            startActivity(intent);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    protected ArrayList<Card> initCards() {
        ArrayList<Card> cards = new ArrayList<Card>();
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
            Thumbnail thumb = new Thumbnail(getActivity(), product.getImage());
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
            super(context, R.layout.product_list_inner_content);
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
            if(numberOfExams == 0) {
                examsCountTextView.setVisibility(View.GONE);
            } else {
                examsCountTextView.setText(numberOfExams + " Exams");
                examsCountTextView.setVisibility(View.VISIBLE);
            }
            if(notesCount == 0) {
                notesCountTextView.setVisibility(View.GONE);
            } else {
                notesCountTextView.setText(notesCount + " Documents");
                notesCountTextView.setVisibility(View.VISIBLE);
            }
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

    class Thumbnail extends CardThumbnail {
        String imageUrl;

        Thumbnail(Context context,String imageUrl) {
            super(context);
            this.imageUrl = imageUrl;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {
            ImageView image = (ImageView) viewImage;
            imageLoader.displayImage(imageUrl,image,options); //download image from url & set to imageView using universal loader
        }
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
}

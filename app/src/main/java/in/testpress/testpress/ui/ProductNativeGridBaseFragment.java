package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

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
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

public class ProductNativeGridBaseFragment extends PagedItemFragment<Product> {

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    ProductsPager pager;
    Product product;
    ArrayList<Card> cards = new ArrayList<Card>();

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
        super.onLoadFinished(loader, items);
    }

    protected ResourcePager<Product> getPager() {
        return pager;
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    protected void refreshWithProgress() {
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
            product = items.get(i);
            ProductCard card = new ProductCard(getActivity());
            card.mainTitle = product.getTitle();
            card.numberOfExams = product.getExamsCount();
            card.notesCount = product.getNotesCount();
            card.startDate = product.getStartDate();
            card.endDate = product.getEndDate();
            card.categories = product.getCategories();
            card.price = product.getPrice();
            CardThumbnail thumb = new CardThumbnail(getActivity());
            if (product.getImage() != null) {
                thumb.setUrlResource(product.getImage());
            } else {
                thumb.setDrawableResource(R.drawable.icon);
            }
            card.addCardThumbnail(thumb);

            card.setOnClickListener(new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {

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
        if ((exception.getMessage()).equals("403 FORBIDDEN")) {
            serviceProvider.invalidateAuthToken();
            logoutService.logout(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    getActivity().startActivity(intent);
                }
            });
            return R.string.authentication_failed;
        } else {
            setEmptyText(R.string.no_internet);
        }
        return R.string.error_loading_exams;
    }

    @Override
    protected SingleTypeAdapter<Product> createAdapter(List<Product> items) {
        return null;
    }
}

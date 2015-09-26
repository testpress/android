package in.testpress.testpress.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.Arrays;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Order;
import in.testpress.testpress.models.Product;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.Ln;

public class ProductsListAdapter extends SingleTypeAdapter<Product> {

    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisc(true).resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.drawable.icon)
            .imageScaleType(ImageScaleType.EXACTLY)
            .showImageOnFail(R.drawable.icon)
            .showImageOnLoading(R.drawable.icon).build();

    /**
     * @param inflater
     * @param items
     */
    public ProductsListAdapter(final LayoutInflater inflater, final List<Product> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.number_of_exams,
                R.id.number_of_notes, R.id.date, R.id.price, R.id.categories, R.id.thumbnail_image};
    }

    @Override
    protected void update(int position, View view, Product item) {
        this.setCurrentView(view);
        View[] views = getChildren(view);
        setText(0, item.getTitle());
        if(item.getExamsCount() ==0 ){
            views[1].setVisibility(View.GONE);
        } else {
            setText(1, item.getExamsCount() + " Exams");
            views[1].setVisibility(View.VISIBLE);
        }
        if(item.getNotesCount() ==0 ){
            views[2].setVisibility(View.GONE);
        } else {
            setText(2, item.getNotesCount() + " Documents");
            views[2].setVisibility(View.VISIBLE);
        }
        FormatDate date = new FormatDate();
        if(date.getDate(item.getStartDate(), item.getEndDate()) != null) {
            views[3].setVisibility(View.VISIBLE);
            setText(3, date.getDate(item.getStartDate(), item.getEndDate()));
        } else {
            views[3].setVisibility(View.GONE);
        }
        setText(4, "₹ " + item.getPrice());
        String categories = Arrays.toString(item.getCategories().toArray());
        setText(5, categories.substring(1, categories.length()-1));
        views[5].setSelected(true);
        imageLoader.displayImage(item.getImage(), (ImageView)views[6], options); //download image from url & set to imageView using universal loader
    }

    @Override
    protected void update(final int position, final Product item) {
        //empty implementation due to abstract method in parent
    }
}
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
import in.testpress.testpress.models.Product;

public class ProductsListAdapter extends SingleTypeAdapter<Product> {

    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.drawable.testpress_placeholder_icon)
            .imageScaleType(ImageScaleType.EXACTLY)
            .showImageOnFail(R.drawable.testpress_placeholder_icon)
            .showImageOnLoading(R.drawable.testpress_placeholder_icon)
            .build();

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
        return new int[]{R.id.title, R.id.total_exams,
                R.id.total_notes, R.id.price, R.id.categories, R.id.thumbnail_image};
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
        setText(3, item.getPrice());
        String categories = Arrays.toString(item.getCategories().toArray());
        setText(4, categories.substring(1, categories.length() - 1));
        views[4].setSelected(true);
        if (item.getImages().length > 0) {
            imageLoader.displayImage(item.getImages()[0].getMedium(), (ImageView)views[5], options); //download image from url & set to imageView using universal loader
        }
    }

    @Override
    protected void update(final int position, final Product item) {
        //empty implementation due to abstract method in parent
    }
}
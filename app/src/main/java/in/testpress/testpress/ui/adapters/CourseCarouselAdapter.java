package in.testpress.testpress.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.testpress.course.ui.ChapterDetailActivity;
import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.models.greendao.Product;
import in.testpress.store.ui.ProductDetailsActivity;
import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.util.ImageUtils;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.IntegerList;

import static in.testpress.store.TestpressStore.STORE_REQUEST_CODE;


public class CourseCarouselAdapter extends RecyclerView.Adapter<CourseCarouselAdapter.MyViewHolder> {
    private DashboardResponse response;
    private DashboardSection section;
    private List<Product> products = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;
    private HashMap<Long, Integer> contentCountHashMap = new HashMap<>();
    private HashMap<Long, Integer> chapterCountHashMap = new HashMap<>();

    public CourseCarouselAdapter(DashboardResponse response, DashboardSection currentSection, Context context) {
        this.response = response;
        this.context = context;
        this.section = currentSection;
        imageLoader = in.testpress.util.ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        populateCourses();
        populateContentsCount();
    }

    private void populateContentsCount() {
        for (Product product: products) {
            int contentCount = 0;
            int chapterCount = 0;

            for (Integer item: product.getCourseIds()) {
                long courseId = Long.valueOf(item);
                contentCount += response.getCourseHashMap().get(courseId).getContentsCount();
                chapterCount += response.getCourseHashMap().get(courseId).getChaptersCount();
            }
            contentCountHashMap.put(product.getId(), contentCount);
            chapterCountHashMap.put(product.getId(), chapterCount);
        }
    }

    private void populateCourses() {
        IntegerList items = section.getItems();
        for (Integer item : items) {
            this.products.add(this.response.getProductHashMap().get(Long.valueOf(item)));
        }
    }

    @Override
    public CourseCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_carousel_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseCarouselAdapter.MyViewHolder holder, int position) {
        try {
            final Product product = products.get(position);
            imageLoader.displayImage(product.getImage(), holder.image, options);
            holder.title.setText(product.getTitle());
            displayContentsCount(holder, product);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Activity activity = (Activity) context;
                    if (product.getCourseIds().size() > 1) {
                        activity.startActivity(CoursePreviewActivity.createIntent(product.getCourseIds(), activity, product.getSlug()));
                    } else if (product.getCourseIds().size() == 1 ) {
                        openChapters(product, activity);
                    } else {
                        Intent intent = new Intent(activity, ProductDetailsActivity.class);
                        intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
                        activity.startActivityForResult(intent, STORE_REQUEST_CODE);
                    }
                }
            });
        } catch (Exception e) {}
    }

    private void openChapters(Product product, Activity activity) {
        activity.startActivity(ChapterDetailActivity.createIntent(
                product.getTitle(),
                product.getCourseIds().get(0).toString(),
                activity, product.getSlug()));
    }

    private void displayContentsCount(CourseCarouselAdapter.MyViewHolder holder, Product product) {
        showOrHideVideoCount(holder, product);
        showOrHideChapterCount(holder, product);
    }

    private void showOrHideVideoCount(CourseCarouselAdapter.MyViewHolder holder, Product product) {
        Integer contentCount = contentCountHashMap.get(product.getId());
        String videosCountText = context.getResources().getQuantityString(
                R.plurals.content_count, contentCount, contentCount);
        holder.contentCount.setText(videosCountText);
    }

    private void showOrHideChapterCount(CourseCarouselAdapter.MyViewHolder holder, Product product) {
        Integer chapterCount = chapterCountHashMap.get(product.getId());
        String examsCountText = context.getResources().getQuantityString(
                R.plurals.chapter_count, chapterCount, chapterCount);
        holder.chapterCount.setText(examsCountText);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, chapterCount, contentCount;
        ImageView image;
        LinearLayout chapterLayout, contentLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            chapterCount = (TextView) itemView.findViewById(R.id.chapter_count);
            contentCount = (TextView) itemView.findViewById(R.id.content_count);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            chapterLayout = itemView.findViewById(R.id.chapter_layout);
            contentLayout = itemView.findViewById(R.id.content_layout);

            title.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            chapterCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            contentCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }
}
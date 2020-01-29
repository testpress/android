package in.testpress.testpress.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
    private HashMap<Long, Integer> notesCountHashMap = new HashMap<>();
    private HashMap<Long, Integer> videosCountHashMap = new HashMap<>();
    private HashMap<Long, Integer> examsCountHashMap = new HashMap<>();

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
            int notesCount = 0;
            int videosCount = 0;
            int examsCount = 0;

            for (Integer item: product.getCourseIds()) {
                long courseId = Long.valueOf(item);
                notesCount += response.getCourseHashMap().get(courseId).getHtmlContentsCount();
                videosCount += response.getCourseHashMap().get(courseId).getVideosCount();
                examsCount += response.getCourseHashMap().get(courseId).getExamsCount();
            }
            notesCountHashMap.put(product.getId(), notesCount);
            videosCountHashMap.put(product.getId(), videosCount);
            examsCountHashMap.put(product.getId(), examsCount);
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
        final Product product = products.get(position);
        imageLoader.displayImage(product.getImage(), holder.image, options);
        holder.title.setText(product.getTitle());
        displayContentsCount(holder, product);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                if (product.getCourseIds().size() > 0) {
                    activity.startActivity(CoursePreviewActivity.createIntent(product.getCourseIds(), activity, product.getSlug()));
                } else {
                    Intent intent = new Intent(activity, ProductDetailsActivity.class);
                    intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, product.getSlug());
                    activity.startActivityForResult(intent, STORE_REQUEST_CODE);
                }
            }
        });
    }

    private void displayContentsCount(CourseCarouselAdapter.MyViewHolder holder, Product product) {
        String notesCountText = context.getResources().getQuantityString(
                R.plurals.notes_count, notesCountHashMap.get(product.getId()), notesCountHashMap.get(product.getId()));
        String videosCountText = context.getResources().getQuantityString(
                R.plurals.videos_count, videosCountHashMap.get(product.getId()), videosCountHashMap.get(product.getId()));
        String examsCountText = context.getResources().getQuantityString(
                R.plurals.exams_count, examsCountHashMap.get(product.getId()), examsCountHashMap.get(product.getId()));

        holder.notesCount.setText(notesCountText);
        holder.videosCount.setText(videosCountText);
        holder.examsCount.setText(examsCountText);

        if (product.getCourseIds().size() == 0) {
            holder.notesCount.setText("Offline");
            holder.videoContentLayout.setVisibility(View.GONE);
            holder.examsContentLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, examsCount, videosCount, notesCount;
        ImageView image;
        LinearLayout examsContentLayout, notesContentLayout, videoContentLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            examsCount = (TextView) itemView.findViewById(R.id.exams_count);
            videosCount = (TextView) itemView.findViewById(R.id.videos_count);
            notesCount = (TextView) itemView.findViewById(R.id.notes_count);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            examsContentLayout = itemView.findViewById(R.id.exam_content_layout);
            notesContentLayout = itemView.findViewById(R.id.notes_content_layout);
            videoContentLayout = itemView.findViewById(R.id.video_content_layout);

            title.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            examsCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            videosCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            notesCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }
}
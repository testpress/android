package in.testpress.testpress.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.course.ui.CoursePreviewActivity;
import in.testpress.models.greendao.Product;
import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.ImageUtils;
import in.testpress.util.IntegerList;


public class CourseCarouselAdapter extends RecyclerView.Adapter<CourseCarouselAdapter.MyViewHolder> {
    private DashboardResponse response;
    private DashboardSection section;
    private List<Product> products = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public CourseCarouselAdapter(DashboardResponse response, DashboardSection currentSection, Context context) {
        this.response = response;
        this.context = context;
        this.section = currentSection;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        populateCourses();
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                activity.startActivity(CoursePreviewActivity.createIntent(product.getCourseIds(), activity, product.getSlug()));

            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, examsCount, videosCount, notesCount;
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            examsCount = (TextView) itemView.findViewById(R.id.exams_count);
            videosCount = (TextView) itemView.findViewById(R.id.videos_count);
            notesCount = (TextView) itemView.findViewById(R.id.notes_count);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            title.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            examsCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            videosCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            notesCount.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }
}
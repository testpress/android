package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.content.Intent;
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

import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.util.ImageUtils;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.IntegerList;

public class PostCarouselAdapter extends RecyclerView.Adapter<PostCarouselAdapter.ItemViewHolder> {

    private DashboardResponse response;
    private DashboardSection section;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;
    private List<Post> posts = new ArrayList<>();


    public PostCarouselAdapter(DashboardResponse response, DashboardSection section, Context context) {
        this.response = response;
        this.section = section;
        this.context = context;
        imageLoader = in.testpress.util.ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        populatePosts();
    }

    private void populatePosts() {
        IntegerList items = section.getItems();
        for (Integer item : items) {
            this.posts.add(this.response.getPostHashMap().get(Long.valueOf(item)));
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_carousel_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        imageLoader.displayImage(posts.get(position).getCoverImage(), holder.image, options);
        holder.title.setText(posts.get(position).getTitle());
        holder.itemView.setOnClickListener(new ItemOnClickListener(posts.get(position).getShortLink()));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public ItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            title.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }

    public class ItemOnClickListener implements View.OnClickListener {
        String shortLink;

        public ItemOnClickListener(String shortLink) {
            this.shortLink = shortLink;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("shortWebUrl", shortLink);
            context.startActivity(intent);
        }
    }
}
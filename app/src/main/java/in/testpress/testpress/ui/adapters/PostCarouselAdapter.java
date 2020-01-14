package in.testpress.testpress.ui.adapters;

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

import in.testpress.models.greendao.Course;
import in.testpress.testpress.R;
import in.testpress.testpress.models.Post;
import in.testpress.util.ImageUtils;

public class PostCarouselAdapter extends RecyclerView.Adapter<PostCarouselAdapter.MyViewHolder> {

    List<Post> data = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public PostCarouselAdapter(List<Post> data, Context context) {
        this.data = data;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
    }

    @Override
    public PostCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_carousel_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostCarouselAdapter.MyViewHolder holder, int position) {
        imageLoader.displayImage("https://picsum.photos/500/300?random=" + position+30, holder.image, options);
        holder.title.setText(data.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, pubDate;
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            image = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }
}
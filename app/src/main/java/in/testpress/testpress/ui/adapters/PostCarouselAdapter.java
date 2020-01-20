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
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.util.UIUtils;
import in.testpress.util.ImageUtils;

public class PostCarouselAdapter extends RecyclerView.Adapter<PostCarouselAdapter.MyViewHolder> {

    List<Post> data = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public PostCarouselAdapter(List<Post> data, Context context) {
        this.data = data;
        this.context = context;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
    }

    @Override
    public PostCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_carousel_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostCarouselAdapter.MyViewHolder holder, final int position) {
        imageLoader.displayImage(data.get(position).getCoverImage(), holder.image, options);
        holder.title.setText(data.get(position).getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("shortWebUrl", data.get(position).getShortLink());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constants.IS_DEEP_LINK, true);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            title.setTypeface(UIUtils.getLatoBoldFont(context));
        }
    }
}
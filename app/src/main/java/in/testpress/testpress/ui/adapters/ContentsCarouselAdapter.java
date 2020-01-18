package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.graphics.Color;
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

import in.testpress.R;
import in.testpress.core.TestpressSdk;
import in.testpress.models.greendao.Content;
import in.testpress.util.ImageUtils;

public class ContentsCarouselAdapter extends RecyclerView.Adapter<ContentsCarouselAdapter.MyViewHolder> {
    private List<Content> data = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public ContentsCarouselAdapter(List<Content> data, Context context) {
        this.data = data;
        this.context = context;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
    }

    @Override
    public ContentsCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_carousel_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ContentsCarouselAdapter.MyViewHolder holder, int position) {
        imageLoader.displayImage("https://picsum.photos/500/250?random=" + position, holder.image, options);
        if (data.get(0).getImage().isEmpty()) {
            holder.image.setColorFilter(Color.parseColor("#888888"));
        } else {
            holder.image.setColorFilter(Color.parseColor("#77000000"));
        }
        holder.title.setText(data.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            image =  itemView.findViewById(R.id.image_view);
            title = (TextView) itemView.findViewById(R.id.title);
            title.setTypeface(TestpressSdk.getLatoBoldFont(context));
        }
    }
}
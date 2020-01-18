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

import in.testpress.testpress.R;
import in.testpress.testpress.models.Banner;
import in.testpress.util.ImageUtils;

public class OffersCarouselAdapter extends RecyclerView.Adapter<OffersCarouselAdapter.MyViewHolder> {

    List<Banner> data = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public OffersCarouselAdapter(List<Banner> data, Context context) {
        this.data = data;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
    }

    @Override
    public OffersCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offers_carousel_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OffersCarouselAdapter.MyViewHolder holder, int position) {
        imageLoader.displayImage(data.get(position).getImage(), holder.image, options);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }
}
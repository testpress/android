package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.Banner;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.WebViewActivity;
import in.testpress.util.ImageUtils;
import in.testpress.util.IntegerList;

public class OffersCarouselAdapter extends RecyclerView.Adapter<OffersCarouselAdapter.MyViewHolder> {

    private DashboardResponse response;
    private DashboardSection section;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;
    private List<Banner> banners = new ArrayList<>();

    public OffersCarouselAdapter(DashboardResponse response, DashboardSection section, Context context) {
        this.response = response;
        this.section = section;
        this.context = context;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        populateBanners();
    }


    private void populateBanners() {
        IntegerList items = section.getItems();
        for (Integer item : items) {
            this.banners.add(this.response.getBannerHashMap().get(Long.valueOf(item)));
        }
    }

    @Override
    public OffersCarouselAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offers_carousel_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OffersCarouselAdapter.MyViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL_TO_OPEN, banners.get(position).getUrl());
                context.startActivity(intent);
            }
        });
        imageLoader.displayImage(banners.get(position).getImage(), holder.image, options);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }
}
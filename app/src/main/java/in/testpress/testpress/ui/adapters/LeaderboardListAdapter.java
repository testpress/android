package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.models.greendao.Course;
import in.testpress.testpress.R;
import in.testpress.testpress.models.LeaderboardItem;
import in.testpress.ui.view.RoundedImageView;
import in.testpress.util.ImageUtils;

public class LeaderboardListAdapter extends RecyclerView.Adapter<LeaderboardListAdapter.MyViewHolder> {

    List<LeaderboardItem> data = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public LeaderboardListAdapter(List<LeaderboardItem> data, Context context) {
        this.data = data;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        this.context = context;

    }

    @Override
    public LeaderboardListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LeaderboardListAdapter.MyViewHolder holder, int position) {
        String rank = position + 1 + "";
        holder.rank.setText(rank);
        LeaderboardItem leaderboardItem = data.get(position);
        holder.username.setText(data.get(position).getUser().getDisplayName());
        imageLoader.displayImage(data.get(position).getUser().getMediumImage(), holder.userImage, options);
        int trophies_count = (int) Double.parseDouble(data.get(position).getTrophiesCount());
        Log.d("LeaderboardListAdapter", "onBindViewHolder: " + trophies_count);
        holder.trophies.setText(String.valueOf(trophies_count));

        Integer difference = leaderboardItem.getDifference() == null ?
                0 : leaderboardItem.getDifference();

        holder.difference.setText(difference.toString());
        if (difference > 0) {
            holder.difference.setTextColor(ContextCompat.getColor(context, in.testpress.course.R.color.testpress_green_light));
            holder.differenceImage.setImageResource(in.testpress.course.R.drawable.testpress_arrow_up_green);
        } else {
            holder.difference.setTextColor(ContextCompat.getColor(context, in.testpress.course.R.color.testpress_red_incorrect));
            holder.differenceImage.setImageResource(in.testpress.course.R.drawable.testpress_arrow_down_red);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rank, username, trophies, difference;
        ImageView differenceImage;
        RoundedImageView userImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            rank = (TextView) itemView.findViewById(R.id.rank);
            username = (TextView) itemView.findViewById(R.id.username);
            trophies = (TextView) itemView.findViewById(R.id.trophies);
            difference = (TextView) itemView.findViewById(R.id.difference);
            userImage = (RoundedImageView) itemView.findViewById(R.id.user_image);
            differenceImage = (ImageView) itemView.findViewById(R.id.difference_image);
        }
    }
}
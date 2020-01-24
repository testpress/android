package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.models.pojo.LeaderboardItem;
import in.testpress.testpress.util.UIUtils;
import in.testpress.ui.view.RoundedImageView;
import in.testpress.util.ImageUtils;
import in.testpress.util.IntegerList;

import static in.testpress.testpress.util.Strings.capitalize;
import static in.testpress.testpress.util.Strings.capitalizeEachWord;

public class LeaderboardListAdapter extends RecyclerView.Adapter<LeaderboardListAdapter.MyViewHolder> {

    List<LeaderboardItem> leaderboardItems = new ArrayList<>();
    private DashboardResponse response;
    private DashboardSection section;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public LeaderboardListAdapter(DashboardResponse response, DashboardSection currentSection, Context context) {
        this.response = response;
        this.context = context;
        this.section = currentSection;
        this.context = context;
        imageLoader = ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
        populateLeaderboard();
    }

    private void populateLeaderboard() {
        IntegerList items = section.getItems();

        for (Integer item : items) {
            this.leaderboardItems.add(this.response.getLeaderboardItemHashMap().get(Long.valueOf(item)));
        }
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
        LeaderboardItem leaderboardItem = leaderboardItems.get(position);
        holder.username.setText(capitalizeEachWord(leaderboardItem.getUser().getDisplayName()));
        imageLoader.displayImage(leaderboardItem.getUser().getMediumImage(), holder.userImage, options);
        int trophies_count = (int) Double.parseDouble(leaderboardItem.getTrophiesCount());
        holder.trophies.setText(String.valueOf(trophies_count));

        Integer difference = leaderboardItem.getDifference() == null ?
                0 : leaderboardItem.getDifference();
    }

    @Override
    public int getItemCount() {
        return leaderboardItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rank, username, trophies;
        ImageView differenceImage;
        RoundedImageView userImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            rank = (TextView) itemView.findViewById(R.id.rank);
            username = (TextView) itemView.findViewById(R.id.username);
            trophies = (TextView) itemView.findViewById(R.id.trophies);
            userImage = (RoundedImageView) itemView.findViewById(R.id.user_image);
            differenceImage = (ImageView) itemView.findViewById(R.id.difference_image);
            rank.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            username.setTypeface(UIUtils.getLatoSemiBoldFont(context));
            trophies.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }
}
package in.testpress.testpress.ui.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.view_holders.BaseCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.ContentsCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.CourseCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.LeaderboardViewHolder;
import in.testpress.testpress.ui.view_holders.OffersCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.PostsCarouselViewHolder;


public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DashboardSection> sections;
    private DashboardResponse response;
    private Context context;
    private TestpressServiceProvider serviceProvider;
    private InstituteSettings instituteSettings;

    private final int CONTENT_CAROUSEL = 1;
    private final int COURSE_CAROUSEL = 2;
    private final int POST_CAROUSEL = 3;
    private final int OFFERS_CAROUSEL = 5;
    private final int LEADERBOARD_LIST = 6;
    private final int STATS_CHART = 7;


    public DashboardAdapter(Context context, DashboardResponse response, TestpressServiceProvider serviceProvider, InstituteSettings instituteSettings ) {
        this.context = context;
        this.serviceProvider = serviceProvider;
        this.instituteSettings = instituteSettings;
        this.setResponse(response);
    }

    public void setResponse(DashboardResponse response) {
        this.response = response;
        this.sections = response.getAvailableSections();
    }

    @Override
    public int getItemViewType(int position) {
        String contentType = sections.get(position).getContentType();
        switch (contentType) {
            case "post":
                return POST_CAROUSEL;
            case "chapter_content":
                return CONTENT_CAROUSEL;
            case "banner_ad":
                return OFFERS_CAROUSEL;
            case "chapter_content_attempt":
                return CONTENT_CAROUSEL;
            case "products":
                return COURSE_CAROUSEL;
            case "trophy_leaderboard":
                if (instituteSettings.getLeaderboardEnabled())
                return LEADERBOARD_LIST;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view, parent, false);
        RecyclerView.ViewHolder holder;

        switch (viewType) {
            case CONTENT_CAROUSEL:
                holder = new ContentsCarouselViewHolder(view, context);
                break;
            case POST_CAROUSEL:
                holder = new PostsCarouselViewHolder(view, context);
                break;
            case OFFERS_CAROUSEL:
                holder = new OffersCarouselViewHolder(view, context);
                break;
            case COURSE_CAROUSEL:
                holder = new CourseCarouselViewHolder(view, context);
                break;
            case LEADERBOARD_LIST:
                holder = new LeaderboardViewHolder(view, context);
                break;
            default:
                holder = new BaseCarouselViewHolder(view, context);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CONTENT_CAROUSEL:
                ((ContentsCarouselViewHolder) holder).display(response, context);
                break;
            case POST_CAROUSEL:
                ((PostsCarouselViewHolder) holder).display(response, context);
                break;
            case OFFERS_CAROUSEL:
                ((OffersCarouselViewHolder) holder).display(response, context, serviceProvider);
                break;
            case COURSE_CAROUSEL:
                ((CourseCarouselViewHolder) holder).display(response, context);
                break;
            case LEADERBOARD_LIST:
                ((LeaderboardViewHolder) holder).display(response, context);
                break;
        }
    }
}
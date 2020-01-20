package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.ui.view_holders.BaseCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.ContentsCarouselViewHolder;
import in.testpress.testpress.ui.view_holders.PostsCarouselHolder;


public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DashboardSection> sections;
    private Context context;

    private final int CONTENT_CAROUSEL = 1;
    private final int COURSE_CAROUSEL = 2;
    private final int POST_CAROUSEL = 3;
    private final int OFFERS_CAROUSEL = 5;
    private final int LEADERBOARD_LIST = 6;
    private final int STATS_CHART = 7;


    public DashboardAdapter(Context context, List<DashboardSection> sections) {
        this.context = context;
        this.sections = sections;
    }

    public void setSections(List<DashboardSection> sections) {
        this.sections = sections;
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
            case "trophy_leaderboard":
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
            case COURSE_CAROUSEL:
            case POST_CAROUSEL:
                holder = new PostsCarouselHolder(view, context);
                break;
            case OFFERS_CAROUSEL:
            case LEADERBOARD_LIST:
            default:
                holder = new BaseCarouselViewHolder(view, context);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DashboardSection section = sections.get(holder.getAdapterPosition());

        switch (holder.getItemViewType()) {
            case CONTENT_CAROUSEL:
                ((ContentsCarouselViewHolder) holder).display(sections, context);
                break;
            case POST_CAROUSEL:
                ((PostsCarouselHolder) holder).display(section, context);
                break;
        }
    }
}

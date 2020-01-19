package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Banner;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.LeaderboardItem;
import in.testpress.testpress.models.LeaderboardItemDao;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;


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
            case "post": return POST_CAROUSEL;
            case "chapter_content": return CONTENT_CAROUSEL;
            case "banner_ad": return OFFERS_CAROUSEL;
            case "chapter_content_attempt": return CONTENT_CAROUSEL;
            case "trophy_leaderboard": return LEADERBOARD_LIST;
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
                holder = new CarouselViewHolder(view);
                break;
            case COURSE_CAROUSEL:
                holder = new CarouselViewHolder(view);
                break;
            case POST_CAROUSEL:
                holder = new CarouselViewHolder(view);
                break;
            case OFFERS_CAROUSEL:
                holder = new CarouselViewHolder(view);
                break;
            case LEADERBOARD_LIST:
                holder = new CarouselViewHolder(view);
                break;
//            case STATS_CHART:
//                view = inflater.inflate(R.layout.user_stats, parent, false);
//                holder = new UserStatsViewHolder(view);
//                break;
            default:
                holder = new CarouselViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == POST_CAROUSEL) {
            postCarouselView((CarouselViewHolder) holder);
        } else if (holder.getItemViewType() == OFFERS_CAROUSEL) {
            offersCarouselView((CarouselViewHolder) holder);
        } else if (holder.getItemViewType() == CONTENT_CAROUSEL) {
            contentsCarouselView((CarouselViewHolder) holder);
        } else if (holder.getItemViewType() == LEADERBOARD_LIST) {
            Log.d("DashboardAdapter", "onBindViewHolder: ");
            leaderboardListView((CarouselViewHolder) holder);
        }
//        else {
//            contentsCarouselView((CarouselViewHolder) holder);
//        }

//        if (holder.getItemViewType() == CONTENT_CAROUSEL)
//            contentsCarouselView((CarouselViewHolder) holder);
//        else if (holder.getItemViewType() == COURSE_CAROUSEL)
//            courseCarouselView((CarouselViewHolder) holder);
//        else if (holder.getItemViewType() == POST_CAROUSEL)
//            postCarouselView((CarouselViewHolder) holder);
//        else if (holder.getItemViewType() == OFFERS_CAROUSEL)
//            offersCarouselView((CarouselViewHolder) holder);
//        else if (holder.getItemViewType() == LEADERBOARD_LIST)
//            leaderboardListView((CarouselViewHolder) holder);
//        else if (holder.getItemViewType() == STATS_CHART)
//            userstatsView((UserStatsViewHolder) holder);
    }

    private void offersCarouselView(CarouselViewHolder holder) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        List<Banner> banners = daoSession.getBannerDao().queryBuilder().list();
        OffersCarouselAdapter adapter = new OffersCarouselAdapter(banners, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);
        holder.title.setText(sections.get(holder.getAdapterPosition()).getDisplayName());
        holder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.offers, 0, 0, 0);
    }

    private void postCarouselView(CarouselViewHolder holder) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();

        DashboardSection section = sections.get(holder.getAdapterPosition());
        List<Post> posts = daoSession.getPostDao().queryBuilder()
                .where(PostDao.Properties.Id.in(section.getItems())).list();

        PostCarouselAdapter adapter = new PostCarouselAdapter(posts, context);
        if (posts.size() > 8) {
            holder.recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false));
        } else {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
        holder.recyclerView.setAdapter(adapter);
        holder.title.setText(sections.get(holder.getAdapterPosition()).getDisplayName());
        holder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.post, 0, 0, 0);
    }

    private void leaderboardListView(CarouselViewHolder holder) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        DashboardSection section = sections.get(holder.getAdapterPosition());
        List<LeaderboardItem> leaderboardItems = daoSession.getLeaderboardItemDao().queryBuilder()
                .where(LeaderboardItemDao.Properties.Id.in(section.getItems())).list();
        LeaderboardListAdapter adapter = new LeaderboardListAdapter(leaderboardItems, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(adapter);
        holder.title.setText(sections.get(holder.getAdapterPosition()).getDisplayName());
        holder.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.leaderboard, 0, 0, 0);
    }

    private void contentsCarouselView(CarouselViewHolder holder) {
        DashboardSection section = sections.get(holder.getAdapterPosition());
        List<Integer> contentIds = new ArrayList<>();
        if (section.getContentType().equals("chapter_content_attempt")) {
            List<CourseAttempt> contentAttempts = TestpressSDKDatabase.getCourseAttemptDao(context)
                    .queryBuilder().where(CourseAttemptDao.Properties.Id.in(section.getItems())).list();

            for (CourseAttempt contentAttempt: contentAttempts) {
                contentIds.add(contentAttempt.getChapterContentId().intValue());
            }
        } else if (section.getContentType().equals("chapter_content")) {
            contentIds.addAll(section.getItems());
        }

        List<Content> contents = TestpressSDKDatabase.getContentDao(context).queryBuilder()
                .where(ContentDao.Properties.Id.in(contentIds)).list();
        ContentsCarouselAdapter adapter1 = new ContentsCarouselAdapter(contents, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter1);
        holder.title.setText(sections.get(holder.getAdapterPosition()).getDisplayName());
    }

    public class CarouselViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        TextView title;
        CarouselViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            title.setTypeface(TestpressSdk.getRubikMediumFont(context));
            recyclerView = (RecyclerView) itemView.findViewById(R.id.inner_recyclerView);
        }
    }
}

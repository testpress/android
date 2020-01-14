package in.testpress.testpress.ui.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.course.network.LeaderboardPager;
import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.Course;
import in.testpress.models.greendao.CourseDao;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.ui.temp.Reputation;

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<String> items;
    private final int CONTENT_CAROUSEL = 1;
    private final int COURSE_CAROUSEL = 2;
    private final int POST_CAROUSEL = 3;
    private final int OFFERS_CAROUSEL = 5;
    private final int LEADERBOARD_LIST = 6;
    private final int STATS_CHART = 7;

    public DashboardAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position)) {
            case "CONTENT_CAROUSEL":
                return CONTENT_CAROUSEL;
            case "COURSE_CAROUSEL":
                return COURSE_CAROUSEL;
            case "POST_CAROUSEL":
                return POST_CAROUSEL;
            case "OFFERS_CAROUSEL":
                return OFFERS_CAROUSEL;
            case "LEADERBOARD_LIST":
                return LEADERBOARD_LIST;
            case "STATS_CHART":
                return STATS_CHART;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.horizontal, parent, false);
        TextView title = view.findViewById(R.id.title);;
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case CONTENT_CAROUSEL:
                holder = new CarouselViewHolder(view);
                break;
            case COURSE_CAROUSEL:
                title.setText("NEW COURSES");
                holder = new CarouselViewHolder(view);
                break;
            case POST_CAROUSEL:
                title.setText("RECENT POSTS");
                holder = new CarouselViewHolder(view);
                break;
            case OFFERS_CAROUSEL:
                title.setText("OFFERS");
                holder = new CarouselViewHolder(view);
                break;
            case LEADERBOARD_LIST:
                title.setText("LEADERBOARD");
                holder = new CarouselViewHolder(view);
                break;
            case STATS_CHART:
                view = inflater.inflate(R.layout.user_stats, parent, false);
                holder = new UserStatsViewHolder(view);
                break;
            default:
                holder = new CarouselViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == CONTENT_CAROUSEL)
            contentsCarouselView((CarouselViewHolder) holder);
        else if (holder.getItemViewType() == COURSE_CAROUSEL)
            courseCarouselView((CarouselViewHolder) holder);
        else if (holder.getItemViewType() == POST_CAROUSEL)
            postCarouselView((CarouselViewHolder) holder);
        else if (holder.getItemViewType() == OFFERS_CAROUSEL)
            offersCarouselView((CarouselViewHolder) holder);
        else if (holder.getItemViewType() == LEADERBOARD_LIST)
            leaderboardListView((CarouselViewHolder) holder);
        else if (holder.getItemViewType() == STATS_CHART)
            userstatsView((UserStatsViewHolder) holder);
    }

    private void contentsCarouselView(CarouselViewHolder holder) {
        List<Content> contents = TestpressSDKDatabase.getContentDao(context).queryBuilder().where(ContentDao.Properties.Active.eq(true)).limit(5).list();
        ContentsCarouselAdapter adapter1 = new ContentsCarouselAdapter(contents, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter1);
    }


    private void courseCarouselView(CarouselViewHolder holder) {
        List<Course> courses = TestpressSDKDatabase.getCourseDao(context).queryBuilder().where(CourseDao.Properties.Active.eq(true)).limit(5).list();
        CourseCarouselAdapter adapter = new CourseCarouselAdapter(courses, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);
    }

    private void postCarouselView(CarouselViewHolder holder) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        PostDao postDao = daoSession.getPostDao();
        List<Post> posts = postDao.queryBuilder().limit(10).list();
        PostCarouselAdapter adapter = new PostCarouselAdapter(posts, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);
    }

    private void offersCarouselView(CarouselViewHolder holder) {
        ArrayList<String> data = new ArrayList<>();
        for(int i=0; i<5; i++) {
            data.add("https://picsum.photos/550/300?random=" + i + 80);
        }
        OffersCarouselAdapter adapter = new OffersCarouselAdapter(data, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);
    }

    private void leaderboardListView(CarouselViewHolder holder) {
        List<Reputation> data = new ArrayList<>();
        Random rand = new Random();
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();

        List<User> users= daoSession.getUserDao().queryBuilder().limit(5).list();

        for (User user: users) {
            Reputation reputation = new Reputation();
            reputation.setId(user.getId().intValue());
            reputation.setDifference(rand.nextInt(30));
            reputation.setTrophiesCount(rand.nextInt(100));
            reputation.setUser(user);
            reputation.setRank(rand.nextInt(10));
            data.add(reputation);
        }

        LeaderboardListAdapter adapter = new LeaderboardListAdapter(data, context);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerView.setAdapter(adapter);
    }

    private void userstatsView(UserStatsViewHolder holder) {

    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CarouselViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;

        CarouselViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.inner_recyclerView);
        }
    }


    public class UserStatsViewHolder extends RecyclerView.ViewHolder {

        TextView userStats;

        UserStatsViewHolder(View itemView) {
            super(itemView);

        }
    }


}
package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.LeaderboardItem;
import in.testpress.testpress.models.LeaderboardItemDao;
import in.testpress.testpress.ui.adapters.LeaderboardListAdapter;

public class LeaderboardViewHolder extends BaseCarouselViewHolder {
    public LeaderboardViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(List<DashboardSection> sections, Context context) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        DashboardSection section = sections.get(getAdapterPosition());
        List<LeaderboardItem> leaderboardItems = daoSession.getLeaderboardItemDao().queryBuilder()
                .where(LeaderboardItemDao.Properties.Id.in(section.getItems()))
                .orderDesc(LeaderboardItemDao.Properties.TrophiesCount).list();
        LeaderboardListAdapter adapter = new LeaderboardListAdapter(leaderboardItems, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        title.setText(sections.get(getAdapterPosition()).getDisplayName());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_leaderboard, 0, 0, 0);
    }

}

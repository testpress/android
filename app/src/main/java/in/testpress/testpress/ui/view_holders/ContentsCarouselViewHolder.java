package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.adapters.ContentsCarouselAdapter;

public class ContentsCarouselViewHolder extends BaseCarouselViewHolder {
    public ContentsCarouselViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(DashboardResponse response, Context context) {
        List<DashboardSection> sections = response.getAvailableSections();
        DashboardSection section = sections.get(getAdapterPosition());
        ContentsCarouselAdapter adapter = new ContentsCarouselAdapter(response, section, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        title.setText(sections.get(getAdapterPosition()).getDisplayName());
        setTitleIcon(section);
    }

    private void setTitleIcon(DashboardSection section) {
        if (section.getSlug().equals("completed")) {
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_completed_black, 0, 0, 0);
        } else {
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black, 0, 0, 0);
        }
    }
}
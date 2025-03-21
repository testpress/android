package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.testpress.course.ui.AvailableCoursesListActivity;
import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.adapters.CourseCarouselAdapter;


public class CourseCarouselViewHolder extends BaseCarouselViewHolder {

    TextView seeAll;

    public CourseCarouselViewHolder(View itemView, Context context) {
        super(itemView, context);
        seeAll = itemView.findViewById(R.id.see_all);
    }

    public void display(DashboardResponse response, Context context) {
        List<DashboardSection> sections = response.getAvailableSections();
        DashboardSection section = sections.get(getAdapterPosition());
        CourseCarouselAdapter adapter = new CourseCarouselAdapter(response, section, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        title.setText(sections.get(getAdapterPosition()).getDisplayName());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_black, 0, 0, 0);

        if (section.getItems().size() > 2) {
            showPageIndicator();
        }
        seeAll.setVisibility(View.VISIBLE);
        initOnClickListeners(context);
    }

    private void initOnClickListeners(Context context) {
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AvailableCoursesListActivity.class);
                context.startActivity(intent);
            }
        });
    }

}

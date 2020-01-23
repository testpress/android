package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.adapters.PostCarouselAdapter;

public class PostsCarouselViewHolder extends BaseCarouselViewHolder {
    public PostsCarouselViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(DashboardResponse response, Context context) {
        DashboardSection section = response.getDashboardSections().get(getAdapterPosition());
        PostCarouselAdapter adapter = new PostCarouselAdapter(response, section, context);

        if (section.getItems().size() > 8) {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
        recyclerView.setAdapter(adapter);
        title.setText(section.getDisplayName());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_news_black, 0, 0, 0);
    }
}
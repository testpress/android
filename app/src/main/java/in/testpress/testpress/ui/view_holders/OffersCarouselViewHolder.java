package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.ui.adapters.OffersCarouselAdapter;

public class OffersCarouselViewHolder extends BaseCarouselViewHolder {
    public OffersCarouselViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(DashboardResponse response, Context context, TestpressServiceProvider serviceProvider) {
        DashboardSection section = response.getAvailableSections().get(getAdapterPosition());
        OffersCarouselAdapter adapter = new OffersCarouselAdapter(response, section, context, serviceProvider);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        title.setText(section.getDisplayName());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_offer_black, 0, 0, 0);
    }
}
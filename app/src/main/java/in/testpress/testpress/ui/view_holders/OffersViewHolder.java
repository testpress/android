package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Banner;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.ui.adapters.OffersCarouselAdapter;

public class OffersViewHolder extends BaseCarouselViewHolder {
    public OffersViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(List<DashboardSection> sections, Context context) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        List<Banner> banners = daoSession.getBannerDao().queryBuilder().list();
        OffersCarouselAdapter adapter = new OffersCarouselAdapter(banners, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
//        title.setText(sections.get(getAdapterPosition()).getDisplayName());
        title.setText("Quick Links");
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.offers, 0, 0, 0);
    }
}

package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.ui.utils.RecyclerViewAttacher;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class BaseCarouselViewHolder extends RecyclerView.ViewHolder {
    RecyclerView recyclerView;
    TextView title;
    ScrollingPagerIndicator recyclerIndicator;

    public BaseCarouselViewHolder(View itemView, Context context) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        title.setTypeface(TestpressSdk.getRubikMediumFont(context));
        recyclerView = (RecyclerView) itemView.findViewById(R.id.inner_recyclerView);
        recyclerIndicator = itemView.findViewById(R.id.indicator);
        recyclerIndicator.setVisibility(View.GONE);
    }

    public void showPageIndicator() {
        recyclerIndicator.attachToPager(recyclerView, new RecyclerViewAttacher(50));
        recyclerIndicator.setVisibility(View.VISIBLE);
    }

    public BaseCarouselViewHolder(View itemView) {
        super(itemView);
        LinearLayout rootLayout = itemView.findViewById(R.id.root_layout);
        rootLayout.setVisibility(View.GONE);
    }
}
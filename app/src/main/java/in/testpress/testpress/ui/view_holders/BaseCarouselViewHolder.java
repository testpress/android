package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;

public class BaseCarouselViewHolder extends RecyclerView.ViewHolder {
    RecyclerView recyclerView;
    TextView title;

    public BaseCarouselViewHolder(View itemView, Context context) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        title.setTypeface(TestpressSdk.getRubikMediumFont(context));
        recyclerView = (RecyclerView) itemView.findViewById(R.id.inner_recyclerView);
    }
}
package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.adapters.PostCarouselAdapter;

public class PostsCarouselHolder extends BaseCarouselViewHolder {
    public PostsCarouselHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(DashboardSection section, Context context) {
        DaoSession daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();

        List<Post> posts = daoSession.getPostDao().queryBuilder()
                .where(PostDao.Properties.Id.in(section.getItems())).list();

        PostCarouselAdapter adapter = new PostCarouselAdapter(posts, context);
        if (posts.size() > 8) {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }
        recyclerView.setAdapter(adapter);
        title.setText(section.getDisplayName());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.post, 0, 0, 0);
    }
}

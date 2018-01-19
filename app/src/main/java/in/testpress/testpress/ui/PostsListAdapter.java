package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.Ln;
import in.testpress.util.ViewUtils;

public class PostsListAdapter extends BaseAdapter {

    private Activity activity;
    private final int layout;
    PostDao postDao;
    long filter;

    public PostsListAdapter(final Activity activity, final int layoutResourceId) {
        postDao = ((TestpressApplication) activity.getApplicationContext()).getDaoSession().getPostDao();
        this.activity = activity;
        this.layout = layoutResourceId;
        this.filter = -1;
    }

    public void setCategoryFilter(long category) {
        this.filter = category;
    }

    public void clearCategoryFilter() {
        this.filter = -1;
    }

    @Override
    public int getCount() {
        if (this.filter != -1)
            return (int) postDao.queryBuilder().where(PostDao.Properties.CategoryId.eq(this.filter), PostDao.Properties.Is_active.eq(true)).count();
        return (int) postDao.queryBuilder().where(PostDao.Properties.Is_active.eq(true)).count();
    }

    @Override
    public Post getItem(int position) {
        if (this.filter != -1)
            return postDao.queryBuilder().where(PostDao.Properties.CategoryId.eq(this.filter), PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position);
        Ln.d("PostsListAdapter getItem at position " + position + " item - " + postDao.queryBuilder().where(PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position).getTitle());
        return postDao.queryBuilder().where(PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        if (this.filter != -1)
            return postDao.queryBuilder().where(PostDao.Properties.CategoryId.eq(this.filter), PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position).getId();
        Ln.d("PostsListAdapter getItemId at position " + position + " item - " + postDao.queryBuilder().where(PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position).getId());
        return postDao.queryBuilder().where(PostDao.Properties.Is_active.eq(true)).orderDesc(PostDao.Properties.Published).listLazy().get(position).getId();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Post post = getItem(position);
        Ln.d("PostsListAdapter getView at position " + position);
        Ln.d("PostsListAdapter getView post = " + post.getTitle());
        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(layout, null);
        }

        TextView title = (TextView)convertView.findViewById(R.id.title);
        title.setText(post.getTitle());
        TextView date = (TextView)convertView.findViewById(R.id.date);
        date.setText(FormatDate.getAbbreviatedTimeSpan(post.getPublished()));
        TextView categoryView = (TextView)convertView.findViewById(R.id.category);
        View categoryLayout = convertView.findViewById(R.id.category_layout);
        if(post.getCategory() != null) {
            categoryView.setText(post.getCategory().getName());
            categoryLayout.setVisibility(View.VISIBLE);
        } else {
            categoryLayout.setVisibility(View.GONE);
        }
        convertView.findViewById(R.id.ripple_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("url", post.getShort_web_url());
                activity.startActivity(intent);
            }
        });
        TextView commentsCount = (TextView)convertView.findViewById(R.id.comments_count);
        View commentsLayout = convertView.findViewById(R.id.comments_layout);
        if (post.getCommentsCount() == 0) {
            commentsLayout.setVisibility(View.INVISIBLE);
        } else {
            commentsCount.setText(post.getCommentsCount().toString());
            commentsLayout.setVisibility(View.VISIBLE);
        }
        title.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        ViewUtils.setTypeface(new TextView[] {commentsCount, date, categoryView},
                TestpressSdk.getRubikRegularFont(activity));

        return convertView;
    }
}

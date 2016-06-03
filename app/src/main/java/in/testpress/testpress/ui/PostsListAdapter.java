package in.testpress.testpress.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.ShareUtil;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Post post = getItem(position);
        Ln.d("PostsListAdapter getView at position " + position);
        Ln.d("PostsListAdapter getView post = " + post.getTitle());
        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(layout, null);
        }

        ((TextView)convertView.findViewById(R.id.title)).setText(post.getTitle());
        ((TextView)convertView.findViewById(R.id.date)).setText(DateUtils.getRelativeTimeSpanString(post.getPublished()));
        TextView categoryView = (TextView)convertView.findViewById(R.id.category);

        if(post.getCategory() != null) {
            int backgroundColor = Color.parseColor("#" + post.getCategory().getColor());
            categoryView.setText(post.getCategory().getName());
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(4);
            drawable.setColor(backgroundColor);
            categoryView.setBackgroundDrawable(drawable);
            double grayScale = ( 0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor));
            Ln.d("Grayscale for " + post.getCategory().getColor() + " is " + grayScale);
            if (grayScale > 186) {
                categoryView.setTextColor(Color.BLACK);
            } else {
                categoryView.setTextColor(Color.WHITE);
            }
            categoryView.setVisibility(View.VISIBLE);
        } else {
            categoryView.setVisibility(View.GONE);
        }
        convertView.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtil.shareUrl(activity, post.getTitle(), post.getShort_web_url());
            }
        });
        return convertView;
    }
}

package in.testpress.testpress.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;
import java.util.Objects;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.Ln;

public class PostsListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final int layout;
    PostDao postDao;

    public PostsListAdapter(final Activity activity, final int layoutResourceId) {
        postDao = ((TestpressApplication) activity.getApplicationContext()).getDaoSession().getPostDao();
        this.inflater = activity.getLayoutInflater();
        this.layout = layoutResourceId;
    }

    @Override
    public int getCount() {
        return (int) postDao.count();
    }

    @Override
    public Object getItem(int position) {
        Ln.d("PostsListAdapter getItem at position " + position + " item - " + postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position).getTitle());
        return postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        Ln.d("PostsListAdapter getItemId at position " + position + " item - " + postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position).getId());
        return postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).listLazy().get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = postDao.queryBuilder().orderDesc(PostDao.Properties.CreatedDate).list() .get(position);
        Ln.d("PostsListAdapter getView at position " + position);
        Ln.d("PostsListAdapter getView post = " + post.getTitle());
        if(convertView == null) {
            convertView = inflater.inflate(layout, null);
        }

        ((TextView)convertView.findViewById(R.id.title)).setText(post.getTitle());
        ((TextView)convertView.findViewById(R.id.summary)).setText(post.getSummary());
        ((TextView)convertView.findViewById(R.id.date)).setText(DateUtils.getRelativeTimeSpanString(post.getCreatedDate()));
        TextView categoryView = (TextView)convertView.findViewById(R.id.category);
//        View borderView = convertView.findViewById(R.id.category_color_border);

        if(post.getCategory() != null) {
            int backgroundColor = Color.parseColor("#" + post.getCategory().getColor());
//            borderView.setBackgroundColor(backgroundColor);
            categoryView.setText(post.getCategory().getName());
            ((GradientDrawable)categoryView.getBackground()).setColor(backgroundColor);
            double grayScale = ( 299 * Color.red(backgroundColor) + 587 * Color.green(backgroundColor) + 114 * Color.blue(backgroundColor))/1000;
            Ln.d("Grayscale for " + post.getCategory().getColor() + " is " + grayScale);
            if (grayScale > 139) {
                categoryView.setTextColor(Color.BLACK);
            } else {
                categoryView.setTextColor(Color.WHITE);
            }
            categoryView.setVisibility(View.VISIBLE);
        } else {
            categoryView.setVisibility(View.GONE);
        }
        return convertView;
    }
}

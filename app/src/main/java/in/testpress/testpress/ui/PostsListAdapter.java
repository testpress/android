package in.testpress.testpress.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.util.FormatDate;

public class PostsListAdapter extends SingleTypeAdapter<Post> {
    /**
     * @param inflater
     * @param items
     */
    public PostsListAdapter(final LayoutInflater inflater, final List<Post> items, int layout) {
        super(inflater, layout);
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.summary, R.id.date, R.id.category};
    }

    @Override
    protected void update(int position, View view, Post item) {
        this.setCurrentView(view);
        View[] views = getChildren(view);
        setText(0, item.getTitle());
        setText(1, item.getSummary());
        FormatDate date = new FormatDate();
        setText(2, date.formatDateTime(item.getModified()));
        if(item.getCategory() != null) {
            int backgroundColor = Color.parseColor("#" + item.getCategory().getColor());
            GradientDrawable drawable = (GradientDrawable) views[3].getBackground();
            setText(3, item.getCategory().getName());
            drawable.setColor(backgroundColor);
            TextView textView = (TextView)views[3];
            double grayScale = 1 - ( 0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor))/255;
            if (grayScale < 0.5) {
                textView.setTextColor(Color.BLACK);
            } else {
                textView.setTextColor(Color.WHITE);
            }
        }
        else {
            views[3].setVisibility(View.GONE);
        }
    }

    @Override
    protected void update(final int position, final Post item) {
    }
}

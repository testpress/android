package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.models.RssItem;
import in.testpress.testpress.models.RssItemDao;
import in.testpress.testpress.util.FormatDate;
import in.testpress.util.ImageUtils;
import in.testpress.util.SingleTypeAdapter;

import static in.testpress.testpress.ui.RssFeedDetailActivity.LINK_URL;

public class DrupalRssListAdapter extends SingleTypeAdapter<RssItem> {

    private final Activity mActivity;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private QueryBuilder<RssItem> queryBuilder;
    private List<RssItem> items;

    DrupalRssListAdapter(Activity activity, RssItemDao rssFeedDao) {
        super(activity.getLayoutInflater(), R.layout.rss_list_item);
        mActivity = activity;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
        queryBuilder = rssFeedDao.queryBuilder().orderDesc(RssItemDao.Properties.PublishDate);
        items = queryBuilder.listLazy();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public RssItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.thumbnail_image, R.id.title, R.id.date, R.id.ripple_layout };
    }

    @Override
    public void notifyDataSetChanged() {
        items = queryBuilder.listLazy();
        super.notifyDataSetChanged();
    }

    @Override
    protected void update(final int position, final RssItem feed) {
        textView(1).setTypeface(TestpressSdk.getRubikMediumFont(mActivity));
        textView(2).setTypeface(TestpressSdk.getRubikRegularFont(mActivity));
        setText(1, feed.getTitle());
        setText(2, FormatDate.getAbbreviatedTimeSpan(feed.getPublishDate()));

        if (feed.getImage() == null || feed.getImage().isEmpty()) {
            setGone(0, true);
        } else {
            setGone(0, false);
            mImageLoader.displayImage(feed.getImage(), imageView(0), mOptions);
        }
        view(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, RssFeedDetailActivity.class);
                intent.putExtra(LINK_URL, feed.getLink());
                mActivity.startActivity(intent);
            }
        });
    }

}

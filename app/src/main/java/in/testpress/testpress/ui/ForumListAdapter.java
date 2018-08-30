package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.inject.Inject;

import de.greenrobot.dao.query.QueryBuilder;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.ForumDao;
import in.testpress.testpress.ui.view.RoundedImageView;
import in.testpress.testpress.util.FormatDate;
import in.testpress.util.ViewUtils;

import static in.testpress.testpress.ui.ForumActivity.URL;
import static in.testpress.testpress.ui.ForumListFragment.CHOOSE_A_FILTER;
import static in.testpress.testpress.ui.ForumListFragment.MOST_UPVOTED;
import static in.testpress.testpress.ui.ForumListFragment.MOST_VIEWED;
import static in.testpress.testpress.ui.ForumListFragment.OLD_TO_NEW;
import static in.testpress.testpress.ui.ForumListFragment.RECENTLY_ADDED;

public class ForumListAdapter extends BaseAdapter{

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;

    private Activity activity;
    private final int layout;
    private ForumDao forumDao;
    private long filter;
    private String sortBy = "";
    private SimpleDateFormat simpleDateFormat;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    @SuppressLint("SimpleDateFormat")
    ForumListAdapter(TestpressServiceProvider serviceProvider, Activity activity,
                     int layoutResourceId) {

        forumDao = TestpressApplication.getDaoSession().getForumDao();
        this.activity = activity;
        this.layout = layoutResourceId;
        this.filter = -1;
        this.serviceProvider = serviceProvider;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.profile_image_place_holder)
                .showImageOnLoading(R.drawable.profile_image_place_holder).build();
    }

    public void setCategoryFilter(long category) {
        this.filter = category;
    }

    public void clearCategoryFilter() {
        this.filter = -1;
    }

    public void clearSortBy() {
        this.sortBy = CHOOSE_A_FILTER;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    private QueryBuilder<Forum> getQueryMaker() {
        switch (sortBy) {
            case CHOOSE_A_FILTER:
            case RECENTLY_ADDED:
                return getQueryBuilder().orderDesc(ForumDao.Properties.Published);
            case MOST_VIEWED:
                return getQueryBuilder().orderDesc(ForumDao.Properties.ViewsCount);
            case MOST_UPVOTED:
                return getQueryBuilder().orderDesc(ForumDao.Properties.Upvotes);
            case OLD_TO_NEW:
                return getQueryBuilder().orderAsc(ForumDao.Properties.Published);
            default :
                return getQueryBuilder().orderDesc(ForumDao.Properties.Published);
        }
    }

    private QueryBuilder<Forum> getQueryBuilder() {
        if (filter != -1) {
            return forumDao.queryBuilder().where(
                    ForumDao.Properties.CategoryId.eq(filter),
                    ForumDao.Properties.IsActive.eq(true)
            );
        }
        return forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true));
    }

    @Override
    public int getCount() {
        return (int) getQueryMaker().count();
    }

    @Override
    public Forum getItem(int position) {
        return getQueryMaker().listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getQueryMaker().listLazy().get(position).getId();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Forum forum = getItem(position);
        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(layout, null);
        }
        TextView title = convertView.findViewById(R.id.title);
        TextView date = convertView.findViewById(R.id.date);
        TextView viewsCount = convertView.findViewById(R.id.viewsCount);
        TextView status = convertView.findViewById(R.id.status);
        title.setText(forum.getTitle());
        long time = 0;
        try {
            time = simpleDateFormat.parse(forum.getLastCommentedTime()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (forum.getCommentsCount() == 0 || forum.getLastCommentedBy() == null) {
            status.setText(forum.getCreatedBy().getDisplayName() + " started " +
                        FormatDate.getAbbreviatedTimeSpan(time));
        } else {
            status.setText(forum.getLastCommentedBy().getDisplayName() + " replied " +
                    FormatDate.getAbbreviatedTimeSpan(time));
        }
        try {
            date.setText(FormatDate
                    .getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getCreated()).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        RoundedImageView roundedImageView = convertView.findViewById(R.id.display_picture);
        imageLoader.displayImage(forum.getCreatedBy().getMediumImage(), roundedImageView, options);

        viewsCount.setText(forum.getViewsCount() + " views");

        convertView.findViewById(R.id.ripple_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ForumActivity.class);
                intent.putExtra(URL, forum.getUrl());
                activity.startActivity(intent);
            }
        });
        ViewUtils.setTypeface(new TextView[] { status, title },
                TestpressSdk.getRubikMediumFont(activity));
        ViewUtils.setTypeface(new TextView[] { date, viewsCount },
                TestpressSdk.getRubikRegularFont(activity));

        return convertView;
    }

}

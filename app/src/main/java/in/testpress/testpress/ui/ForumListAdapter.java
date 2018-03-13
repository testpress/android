package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.inject.Inject;

import de.greenrobot.dao.query.QueryBuilder;
import in.testpress.core.TestpressSdk;
import in.testpress.exam.models.Vote;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.ForumDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.testpress.ui.view.RoundedImageView;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ViewUtils;
import retrofit.RetrofitError;

public class ForumListAdapter extends BaseAdapter{

    private Activity activity;
    private final int layout;
    ForumDao forumDao;
    UserDao userDao;
    long filter;
    long sortBy;

    SimpleDateFormat simpleDateFormat;
    int grayColor;
    int primaryColor;
    private static final int DOWNVOTE = -1;
    private static final int UPVOTE = 1;
    ProgressDialog progressDialog;
    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    ImageLoader imageLoader;
    private DisplayImageOptions options;

    public ForumListAdapter(TestpressServiceProvider TSP, Activity activity, final int layoutResourceId) {
        forumDao = ((TestpressApplication) activity.getApplicationContext()).getDaoSession().getForumDao();
        userDao = ((TestpressApplication) activity.getApplicationContext()).getDaoSession().getUserDao();
        this.activity = activity;
        this.layout = layoutResourceId;
        this.filter = -1;
        this.serviceProvider = TSP;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        grayColor = ContextCompat.getColor(activity, R.color.testpress_text_gray_medium);
        primaryColor = ContextCompat.getColor(activity, R.color.testpress_vote_indicator);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please Wait..");
        imageLoader = ImageLoader.getInstance();options = new DisplayImageOptions.Builder().cacheInMemory(true)
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
        this.sortBy = -1;
    }

    public void setSortBy(long sortBy) {
        this.sortBy = sortBy;
    }

    private QueryBuilder<Forum> getQueryMaker() {
        switch ((int) this.sortBy) {
            case -1 :
            case 0 :
                return getQueryBuilder().orderDesc(ForumDao.Properties.Published);
            case 1 :
                return getQueryBuilder().orderDesc(ForumDao.Properties.ViewsCount);
            case 2 :
                return getQueryBuilder().orderDesc(ForumDao.Properties.Upvotes);
            case 3 :
                return getQueryBuilder().orderAsc(ForumDao.Properties.Published);
            default :
                return getQueryBuilder().orderDesc(ForumDao.Properties.Published);
        }
    }

    private QueryBuilder<Forum> getQueryBuilder() {
        if (this.filter != -1)
            return forumDao.queryBuilder().where(ForumDao.Properties.CategoryId.eq(this.filter), ForumDao.Properties.IsActive.eq(true));
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

        TextView title = (TextView)convertView.findViewById(R.id.title);
        TextView date = (TextView)convertView.findViewById(R.id.date);
        TextView viewsCount = (TextView) convertView.findViewById(R.id.viewsCount);
        TextView status = (TextView) convertView.findViewById(R.id.status);

        if (forum.getCommentsCount() == 0 || forum.getLastCommentedBy() == null) {
            try {
                status.setText(forum.getCreatedBy().getDisplayName() + " started " +
                        FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getLastCommentedTime()).getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                status.setText(forum.getLastCommentedBy().getDisplayName() + " replied " +
                        FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getLastCommentedTime()).getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        RoundedImageView roundedImageView = (RoundedImageView) convertView.findViewById(R.id.display_picture);

        title.setText(forum.getTitle());
        try {
            date.setText(FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getCreated()).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imageLoader.displayImage(forum.getCreatedBy().getMediumImage(), roundedImageView, options);

        viewsCount.setText(forum.getViewsCount() + " views");

        convertView.findViewById(R.id.ripple_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ForumActivity.class);
                intent.putExtra("Url", forum.getUrl());
                activity.startActivity(intent);
            }
        });
        status.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        title.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        ViewUtils.setTypeface(new TextView[] {date, viewsCount},
                TestpressSdk.getRubikRegularFont(activity));

        return convertView;
    }

    /**
     * Call this method only from async task
     *
     * @return TestpressService
     */
    TestpressService getService() {
        if (CommonUtils.isUserAuthenticated(activity)) {
            try {
                testpressService = serviceProvider.getService(activity);
            } catch (IOException | AccountsException e) {
                e.printStackTrace();
            }
        }
        return testpressService;
    }
}

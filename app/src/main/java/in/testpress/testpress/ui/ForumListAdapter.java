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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.inject.Inject;

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

    @Override
    public int getCount() {
        if (this.filter != -1)
            return (int) forumDao.queryBuilder().where(ForumDao.Properties.CategoryId.eq(this.filter), ForumDao.Properties.IsActive.eq(true)).count();
        return (int) forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true)).count();
    }

    @Override
    public Forum getItem(int position) {
        if (this.filter != -1)
            return forumDao.queryBuilder().where(ForumDao.Properties.CategoryId.eq(this.filter), ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position);
        Ln.d("ForumListAdapter getItem at position " + position + " item - " + forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position).getTitle());
        return forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        if (this.filter != -1)
            return forumDao.queryBuilder().where(ForumDao.Properties.CategoryId.eq(this.filter), ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position).getId();
        Ln.d("ForumListAdapter getItemId at position " + position + " item - " + forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position).getId());
        return forumDao.queryBuilder().where(ForumDao.Properties.IsActive.eq(true)).orderDesc(ForumDao.Properties.Published).listLazy().get(position).getId();
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

        if (forum.getCommentsCount() == 0) {
            try {
                status.setText(forum.getCreatedBy().getDisplayName() + " started " +
                        FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getLastCommentedTime()+"").getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                status.setText(forum.getLastCommentedBy().getDisplayName() + " replied " +
                        FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getLastCommentedTime()+"").getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        RoundedImageView roundedImageView = (RoundedImageView) convertView.findViewById(R.id.display_picture);

        title.setText(forum.getTitle());
        try {
            date.setText(FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(forum.getCreated()+"").getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imageLoader.displayImage(forum.getCreatedBy().getMediumImage(), roundedImageView, options);
//        votesCount.setText("" + (forum.getUpvotes() - forum.getDownvotes()));

        viewsCount.setText(forum.getViewsCount() + " views");

//        if (forum.getTypeOfVote() == null) {
//            upvoteButton.setColorFilter(grayColor);
//            votesCount.setTextColor(grayColor);
//            downvoteButton.setColorFilter(grayColor);
//        } else if (forum.getTypeOfVote() == -1) {
//            upvoteButton.setColorFilter(grayColor);
//            votesCount.setTextColor(primaryColor);
//            downvoteButton.setColorFilter(primaryColor);
//        } else {
//            upvoteButton.setColorFilter(primaryColor);
//            votesCount.setTextColor(primaryColor);
//            downvoteButton.setColorFilter(grayColor);
//        }

        convertView.findViewById(R.id.ripple_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ForumActivity.class);
                intent.putExtra("Url", forum.getUrl());
                activity.startActivity(intent);
            }
        });

//        upvoteLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                voteForumPost(position, v, UPVOTE);
//            }
//        });
//
//        downvoteLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                voteForumPost(position, v, DOWNVOTE);
//            }
//        });
        status.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        title.setTypeface(TestpressSdk.getRubikMediumFont(activity));
        ViewUtils.setTypeface(new TextView[] {date, viewsCount},
                TestpressSdk.getRubikRegularFont(activity));

        return convertView;
    }

    private void voteForumPost(int position, final View view, final int typeOfVote) {
        Forum forum = getItem(position);
        if (isSelfVote(forum.getCreatedBy().getId())) {
            showSnackBar(view, R.string.testpress_self_vote_error);
            return;
        }
        progressDialog.show();
        if (forum.getVoteId() == null) {
            castVote(view, forum, typeOfVote);
        } else {
            if (forum.getTypeOfVote() == typeOfVote) {
                deleteVote(view, forum);
            } else {
                updateVote(view, forum, typeOfVote);
            }
        }
    }

    private void castVote(final View view, final Forum forum, final int typeOfVote) {
        new SafeAsyncTask<Vote<Forum>>() {
            @Override
            public Vote<Forum> call() throws Exception {
                Log.e("Inside", "Vote call");
                return getService().castVote(forum, typeOfVote);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                Log.e("Inside", "Exception");
                Log.e("Exception", exception.toString());
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(Vote<Forum> vote) throws Exception {
                Log.e("Inside", "Vote call success");
                progressDialog.dismiss();
                onVoteCasted(vote);
            }
        }.execute();
    }

    private void deleteVote(final View view, final Forum forum) {
        new SafeAsyncTask<String>() {
            @Override
            public String call() throws Exception {
                Log.e("Inside", "Delete Vote call");
                return getService().deleteCommentVote(forum);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                Log.e("Inside", "Delete vote Exception");
                Log.e("Error detected", exception.toString());
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(String response) throws Exception {
                Log.e("Inside", "Delete Vote call success");
                //Handle vote delete manually here
                if (forum.getTypeOfVote() == 1) {
                    forum.setUpvotes(forum.getUpvotes() - 1);
                } else {
                    forum.setDownvotes(forum.getDownvotes() - 1);
                }
                forum.setTypeOfVote(null);
                forum.setVoteId(null);
                forumDao.update(forum);
                notifyDataSetChanged();
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void updateVote(final View view, final Forum forum, final int typeOfVote) {
        new SafeAsyncTask<Vote<Forum>>() {
            @Override
            public Vote<Forum> call() throws Exception {
                Log.e("Inside", "Update Vote call");
                return getService().updateCommentVote(forum, typeOfVote);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                Log.e("Inside", "Update Exception");
                handleException(exception, forum, view);
            }

            @Override
            protected void onSuccess(Vote<Forum> vote) throws Exception {
                Log.e("Inside", "Update Vote call success");
                progressDialog.dismiss();
                onVoteCasted(vote);
            }
        }.execute();
    }

    private void onVoteCasted(Vote<Forum> vote) {
        showSnackBar(activity.findViewById(android.R.id.content),
                R.string.testpress_vote_casted);
        Forum forum = vote.getContentObject();
        User user = vote.getContentObject().createdBy;
        userDao.insertOrReplace(user);
        forum.setCreatorId(user.getId());
        user = vote.getContentObject().lastCommentedBy;
        userDao.insertOrReplace(user);
        forum.setCommentorId(user.getId());
        forum.setTypeOfVote(vote.getTypeOfVote());
        forum.setVoteId((long) vote.getId());
        forumDao.update(vote.getContentObject());
        notifyDataSetChanged();
        progressDialog.dismiss();
    }

    private void handleException(Exception exception, Forum forum, View view) {

        int error = R.string.testpress_some_thing_went_wrong_try_again;
        if (exception.getCause() instanceof IOException) {
            error = R.string.no_internet_try_again;
        } else if (exception instanceof RetrofitError) {
            if (((RetrofitError) exception).getResponse().getStatus() == 400) {
                error = R.string.testpress_self_vote_error;
                if (TestpressSdk.getTestpressUserId(activity) != forum.getCreatedBy().getId()) {
                    TestpressSdk.setTestpressUserId(activity, Integer.parseInt(forum.getCreatedBy().getId() + ""));
                }
            }
        }
        showSnackBar(view, error);
        progressDialog.dismiss();
    }

    private boolean isSelfVote(long id) {
        return TestpressSdk.isTestpressUserIdExist(activity) && (id == TestpressSdk.getTestpressUserId(activity));
    }

    public static void showSnackBar(View view, @StringRes int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
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

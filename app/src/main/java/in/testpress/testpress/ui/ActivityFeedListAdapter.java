package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.ActivityDao;
import in.testpress.testpress.models.Assessment;
import in.testpress.testpress.models.AttachmentContent;
import in.testpress.testpress.models.AttachmentContentDao;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.ChapterContent;
import in.testpress.testpress.models.ChapterContentAttempt;
import in.testpress.testpress.models.ChapterContentAttemptDao;
import in.testpress.testpress.models.ChapterContentDao;
import in.testpress.testpress.models.ContentType;
import in.testpress.testpress.models.ContentTypeDao;
import in.testpress.testpress.models.FeedAttachment;
import in.testpress.testpress.models.FeedAttachmentDao;
import in.testpress.testpress.models.FeedChapter;
import in.testpress.testpress.models.FeedChapterDao;
import in.testpress.testpress.models.FeedExam;
import in.testpress.testpress.models.FeedExamDao;
import in.testpress.testpress.models.FeedHtmlContent;
import in.testpress.testpress.models.FeedHtmlContentDao;
import in.testpress.testpress.models.FeedPost;
import in.testpress.testpress.models.FeedPostDao;
import in.testpress.testpress.models.FeedVideo;
import in.testpress.testpress.models.FeedVideoDao;
import in.testpress.testpress.models.User;
import in.testpress.testpress.models.UserDao;
import in.testpress.util.FormatDate;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ActivityFeedListAdapter extends BaseAdapter {

    private final int layout;
    private Activity activity;
    private boolean flag = false;
    private ActivityDao activityDao;
    private UserDao userDao;
    private FeedAttachmentDao attachmentDao;
    private AttachmentContentDao attachmentContentDao;
    private CategoryDao categoryDao;
    private FeedVideoDao videoDao;
    private FeedPostDao postDao;
    private FeedChapterDao chapterDao;
    private ContentTypeDao contentTypeDao;
    private FeedHtmlContentDao htmlContentDao;
    private FeedExamDao examDao;
    private ChapterContentAttemptDao chapterContentAttemptDao;
    private ChapterContentDao chapterContentDao;
    private TextView timestamp, title, description, correctCount, incorrectCount, duration,
            studentCount, examDuration, totalQuestions, examStudentsCount;
    private ImageView feedIcon;
    private LinearLayout examInfoLayout, attemptInfoLayout;
    private TestpressSession testpressSession;
    private String filterVerb;

    ActivityFeedListAdapter(Activity activity, int layoutId) {
        this.activity = activity;
        this.layout = layoutId;
        testpressSession = TestpressSdk.getTestpressSession(activity);
        activityDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getActivityDao();
        userDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getUserDao();
        attachmentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedAttachmentDao();
        attachmentContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getAttachmentContentDao();
        categoryDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getCategoryDao();
        videoDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedVideoDao();
        postDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedPostDao();
        chapterDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedChapterDao();
        contentTypeDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getContentTypeDao();
        htmlContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedHtmlContentDao();
        examDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getFeedExamDao();
        chapterContentAttemptDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getChapterContentAttemptDao();
        chapterContentDao = ((TestpressApplication) getApplicationContext()).getDaoSession().getChapterContentDao();
        filterVerb = "all";
    }

    public void setFilterVerb(String verb) {
        filterVerb = verb;
    }

    @Override
    public int getCount() {
        if (filterVerb.equals("all")) {
            return (int) Math.min(activityDao.count(), 20);
        } else {
            return (int) Math.min(activityDao.queryBuilder()
                    .where(ActivityDao.Properties.Verb.eq(filterVerb)).count(), 20);
        }
    }

    @Override
    public Object getItem(int position) {
        if (filterVerb.equals("all")) {
            return activityDao.queryBuilder()
                    .orderDesc(ActivityDao.Properties.Timestamp).list().get(position);
        } else {
        return activityDao.queryBuilder()
                .where(ActivityDao.Properties.Verb.eq(filterVerb))
                .orderDesc(ActivityDao.Properties.Timestamp).list().get(position);
        }

    }

    @Override
    public long getItemId(int position) {
        if (filterVerb.equals("all")) {
            return activityDao.queryBuilder()
                    .orderDesc(ActivityDao.Properties.Timestamp).list().get(position).getId();
        } else {
            return activityDao.queryBuilder()
                    .where(ActivityDao.Properties.Verb.eq(filterVerb))
                    .orderDesc(ActivityDao.Properties.Timestamp).list().get(position).getId();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        in.testpress.testpress.models.Activity feedActivity = (in.testpress.testpress.models.Activity) getItem(position);
        long submitDateMillis = FormatDate.getDate(feedActivity.getTimestamp(),
                "yyyy-MM-dd'T'HH:mm:ss", "UTC").getTime();

        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(layout, null);
        }

        flag = false;

        timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        title = (TextView) convertView.findViewById(R.id.title);
        description = (TextView) convertView.findViewById(R.id.description);
        examInfoLayout = (LinearLayout) convertView.findViewById(R.id.exam_info_layout);
        attemptInfoLayout = (LinearLayout) convertView.findViewById(R.id.attempt_info_layout);
        feedIcon = (ImageView) convertView.findViewById(R.id.feed_icon);
        correctCount = (TextView) convertView.findViewById(R.id.correct_count);
        incorrectCount = (TextView) convertView.findViewById(R.id.incorrect_count);
        duration = (TextView) convertView.findViewById(R.id.duration);
        studentCount = (TextView) convertView.findViewById(R.id.student_count);
        examDuration = (TextView) convertView.findViewById(R.id.exam_duration);
        totalQuestions = (TextView) convertView.findViewById(R.id.total_questions);
        examStudentsCount = (TextView) convertView.findViewById(R.id.exam_student_count);

        examInfoLayout.setVisibility(View.GONE);
        attemptInfoLayout.setVisibility(View.GONE);
        
        timestamp.setText(FormatDate.getAbbreviatedTimeSpan(submitDateMillis));
        timestamp.setTypeface(TestpressSdk.getRubikRegularFont(activity));
        title.setText(Html.fromHtml(getTitleText(feedActivity, convertView)));
        title.setTypeface(TestpressSdk.getRubikRegularFont(activity));
        String descriptn = getDescription(feedActivity.getActionObjectContentType(),
                feedActivity.getActionObjectObjectId());
        if (descriptn == null || descriptn.equals("")) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(getDescription(feedActivity.getActionObjectContentType(),
                    feedActivity.getActionObjectObjectId()));
            description.setTypeface(TestpressSdk.getRubikRegularFont(activity));
        }
        setFeedIcon(feedIcon, feedActivity);
        return convertView;
    }

    private void setFeedIcon(ImageView feedIcon, in.testpress.testpress.models.Activity feedActivity) {
        String model = getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel();
        int image = R.drawable.new_article_icon;
        switch (model) {
            case "post" :
                image = R.drawable.new_article_icon;
                break;
            case "chapter" :
            case "chaptercontent" :
                image = R.drawable.new_file_icon;
                break;
            case "chaptercontentattempt" :
                ChapterContentAttempt chapterContentAttempt = chapterContentAttemptDao.queryBuilder()
                        .where(ChapterContentAttemptDao.Properties.Id.eq(feedActivity.getActionObjectObjectId()))
                        .list().get(0);
                if (chapterContentAttempt.assessment != null) {
                    image = R.drawable.new_attempt_icon;
                } else if (chapterContentAttempt.video != null) {
                    image = R.drawable.new_video_icon;
                } else if (chapterContentAttempt.attachment != null) {
                    image = R.drawable.new_file_icon;
                } else if (chapterContentAttempt.content != null) {
                    image = R.drawable.new_article_icon;
                }
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            feedIcon.setImageDrawable(activity.getDrawable(image));
        }
    }

    private String getDescription(int contentTypeId, String id) {
        String result = "", model = getContentTypeWithId(contentTypeId).getModel();
        switch (model) {
            case "post" :
                result = getPostWithId(Integer.parseInt(id)).getSummary();
                break;
            case "chapter" :
                result = getChapterWithId(Integer.parseInt(id)).getDescription();
                break;
            case "chaptercontent" :
                ChapterContent chapterContent = getChapterContentWithId(Integer.parseInt(id));
                if (chapterContent.getAttachmentContent() > 0) {
                    result += getAttachmentContentWithId(chapterContent.getAttachmentContent()).getDescription();
                }
                break;
            case "chaptercontentattempt" :
                ChapterContentAttempt chapterContentAttempt = getChapterContentAttemptWithId(Integer.parseInt(id));
                if (chapterContentAttempt.attachment != null) {
                    result = getAttachmentContentWithId(chapterContentAttempt.getAttachment().getAttachmentContent().intValue()).getDescription();
                }
                break;
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    private String getTitleText(in.testpress.testpress.models.Activity feedActivity, View view) {

        //Assuming actor will be user always
        // [Verified by logesh https://testpress.slack.com/archives/D6UFKS44C/p1516436283000070]

        String title,
                contentType = getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel();


        //ACTOR
        title = "<b>" + (feedActivity.getVerb().equals("added") ? getUserWithId(Integer.parseInt(feedActivity.getActorObjectId()))
                .getDisplayName() : "You") + "</b>";

        //VERB & ACTION
        switch (contentType) {
            case "chaptercontentattempt" :
                final ChapterContentAttempt chapterContentAttempt = getChapterContentAttemptWithId(
                        Integer.parseInt(feedActivity.getActionObjectObjectId()));
                if (chapterContentAttempt.getAssessmentId() != null) {
                    title += " " + feedActivity.getVerb() + " this exam <b>" +
                            getExamWithId(chapterContentAttempt.getAssessment().getExam()).getTitle();
                    final Assessment assessment = chapterContentAttempt.getAssessment();
                    attemptInfoLayout.setVisibility(View.VISIBLE);
                    correctCount.setText(assessment.getCorrectCount() + "");
                    incorrectCount.setText(assessment.getIncorrectCount() + "");
                    FeedExam exam = getExamWithId(assessment.getExam());
                    duration.setText(exam.getDuration());
                    studentCount.setText(exam.getStudentsAttemptedCount().toString());
//                    view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!(assessment.getId() + "").equals("")) {
//                                TestpressCourse.showContentDetail(activity, assessment.getId() + "", testpressSession);
//                            }
//                        }
//                    });
                } else if (chapterContentAttempt.getVideoId() != null) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " watched ") + " this video <b>" + getVideoWithId(chapterContentAttempt.getVideo().getVideoContent().intValue()).getTitle();
//                    view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!chapterContentAttempt.getVideo().getId().toString().equals("")) {
//                                TestpressCourse.showContentDetail(activity, chapterContentAttempt.getVideo().getId().toString(), testpressSession);
//                            }
//                        }
//                    });
                    //Can display/[make layout visible of] video here
                } else if (chapterContentAttempt.getAttachmentId() != null) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " viewed ") + " this file <b>" + getAttachmentContentWithId(chapterContentAttempt.getAttachment().getAttachmentContent().intValue()).getTitle();
//                    view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!chapterContentAttempt.getAttachment().getId().toString().equals("")) {
//                                TestpressCourse.showContentDetail(activity, chapterContentAttempt.getAttachment().getId().toString(), testpressSession);
//                            }
//                        }
//                    });
                    //Can display download link here
                } else if (chapterContentAttempt.getContentId() != null) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " read ") + " this article <b>" + getHtmlContentWithId(chapterContentAttempt.getContent().getTextContent()).getTitle();
//                    view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!chapterContentAttempt.getContent().getId().toString().equals("")) {
//                                TestpressCourse.showContentDetail(activity, chapterContentAttempt.getContent().getId().toString(), testpressSession);
//                            }
//                        }
//                    });
                }
                final Long id1 = chapterContentAttempt.getChapterContent();
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if (id1 != null) {
                        TestpressCourse.showContentDetail(activity, id1.toString(), testpressSession);
                    } else {
                        Toast.makeText(activity,
                                "Sorry, chapterContentId is unavailable",
                                Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            break;
            case "chaptercontent" :
                ChapterContent chapterContent = getChapterContentWithId(
                        Integer.parseInt(feedActivity.getActionObjectObjectId()));
                final String id = chapterContent.getId() + "";
                if (chapterContent.getExam() > 0) {
                    title += " " + feedActivity.getVerb() + " this exam <b>" + getExamWithId(chapterContent.getExam()).getTitle();
                    examInfoLayout.setVisibility(View.VISIBLE);
                    examDuration.setText(getExamWithId(chapterContent.getExam()).getDuration());
                    totalQuestions.setText(getExamWithId(chapterContent.getExam()).getNumberOfQuestions().toString());
                    examStudentsCount.setText(getExamWithId(chapterContent.getExam()).getStudentsAttemptedCount().toString());
                } else if (chapterContent.getHtmlContent() > 0) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " viewed ") + "this article <b>" + getHtmlContentWithId(chapterContent.getHtmlContent()).getTitle();
                } else if (chapterContent.getAttachmentContent() > 0) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " viewed ") + "this file <b>" + getAttachmentContentWithId(chapterContent.getAttachmentContent()).getTitle();
                } else if (chapterContent.getVideoContent() > 0) {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " watched ") + "this video <b>" + getVideoWithId(chapterContent.getVideoContent()).getTitle();
                } else {
                    title += (feedActivity.getVerb().equals("added") ? " added " : " viewed ") + " <b>" + getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel();
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!id.equals("")) {
                            TestpressCourse.showContentDetail(activity, id, testpressSession);
                        }
                    }
                });
                break;
            case "post" :
                final FeedPost feedPost = getPostWithId(Integer.parseInt(feedActivity.getActionObjectObjectId()));
                title += (feedActivity.getVerb().equals("added") ? " created " : " viewed ") + "this post <b>" + feedPost.getTitle();
                final String slug = feedPost.getSlug();
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (feedPost.getForum()) {
                            Intent intent = new Intent(activity, ForumActivity.class);
                            Log.e("url", Constants.Http.URL_BASE + "/api/v2.3/forum/" + slug + "/");
                            intent.putExtra("Url", Constants.Http.URL_BASE + "/api/v2.3/forum/" + slug + "/");
                            activity.startActivity(intent);
                        } else {
                            Log.e("url", Constants.Http.URL_BASE + "/posts/" + slug + "/");
                            Intent intent = new Intent(activity, PostActivity.class);
                            intent.putExtra("url", Constants.Http.URL_BASE + "/posts/" + slug + "/");
                            activity.startActivity(intent);
                        }
                    }
                });
                break;
            default:
                title += (feedActivity.getVerb().equals("added") ? " added " : " viewed ") + " <b>" + getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel();
            break;
        }
        title += "</b>";

        //TARGET
        if (feedActivity.getTargetObjectId() > 0) {
            if (getContentTypeWithId(feedActivity.getTargetContentType()).getModel().equals("chapter")) {
                if (getChapterWithId(feedActivity.getTargetObjectId()).getName() != null) {
                    title += " in <b>" + getChapterWithId(feedActivity.getTargetObjectId()).getName() + "</b>";
                }
            } else if (getContentTypeWithId(feedActivity.getActionObjectContentType()).getModel().equals("postcategory")) {
                title += " in <b>" + getPostCategory(feedActivity.getTargetObjectId()).getName() + "</b>";
            }
        }
        return title.substring(0, 3) + title.substring(3, 4).toUpperCase() + title.substring(4);
    }

    private ContentType getContentTypeWithId(int id) {
        if (contentTypeDao.queryBuilder().where(ContentTypeDao.Properties.Id.eq(id)).count() != 0) {
            return contentTypeDao.queryBuilder()
                    .where(ContentTypeDao.Properties.Id.eq(id)).list().get(0);
        }
        return new ContentType();
    }

    private FeedPost getPostWithId(int id) {
        if (postDao.queryBuilder().where(FeedPostDao.Properties.Id.eq(id)).count() != 0) {
            return postDao.queryBuilder()
                    .where(FeedPostDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedPost();
    }

    private FeedChapter getChapterWithId(int id) {
        if (chapterDao.queryBuilder().where(FeedChapterDao.Properties.Id.eq(id)).count() != 0) {
            return chapterDao.queryBuilder()
                    .where(FeedChapterDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedChapter();
    }

    private ChapterContent getChapterContentWithId(int id) {
        if (chapterContentDao.queryBuilder().where(ChapterContentDao.Properties.Id.eq(id)).count() != 0) {
            return chapterContentDao.queryBuilder()
                    .where(ChapterContentDao.Properties.Id.eq(id)).list().get(0);
        }
        return new ChapterContent();
    }

    private ChapterContentAttempt getChapterContentAttemptWithId(int id) {
        if (chapterContentAttemptDao.queryBuilder()
                .where(ChapterContentAttemptDao.Properties.Id.eq(id)).count() != 0) {
            return chapterContentAttemptDao.queryBuilder()
                    .where(ChapterContentAttemptDao.Properties.Id.eq(id)).list().get(0);
        }
        return new ChapterContentAttempt();
    }

    private User getUserWithId(int id) {
        if (userDao.queryBuilder().where(UserDao.Properties.Id.eq(id)).count() != 0) {
            return userDao.queryBuilder().where(UserDao.Properties.Id.eq(id)).list().get(0);
        }
        return new User();
    }

    private FeedExam getExamWithId(int id) {
        if (examDao.queryBuilder().where(FeedExamDao.Properties.Id.eq(id)).count() != 0) {
            return examDao.queryBuilder().where(FeedExamDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedExam();
    }

    private FeedVideo getVideoWithId(int id) {
        if (videoDao.queryBuilder().where(FeedVideoDao.Properties.Id.eq(id)).count() != 0) {
            return videoDao.queryBuilder().where(FeedVideoDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedVideo();
    }

    private AttachmentContent getAttachmentContentWithId(int id) {
        if (attachmentContentDao.queryBuilder().where(AttachmentContentDao.Properties.Id.eq(id)).count() != 0) {
            return attachmentContentDao.queryBuilder().where(AttachmentContentDao.Properties.Id.eq(id)).list().get(0);
        }
        return new AttachmentContent();
    }

    private FeedAttachment getAttachmentWithId(int id) {
        if (attachmentDao.queryBuilder().where(FeedAttachmentDao.Properties.Id.eq(id)).count() != 0) {
            return attachmentDao.queryBuilder().where(FeedAttachmentDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedAttachment();
    }

    private Category getPostCategory(int id) {
        if (categoryDao.queryBuilder().where(CategoryDao.Properties.Id.eq(id)).count() > 0) {
            return categoryDao.queryBuilder().where(CategoryDao.Properties.Id.eq(id)).list().get(0);
        }
        return new Category();
    }

    private FeedHtmlContent getHtmlContentWithId(int id) {
        if (htmlContentDao.queryBuilder().where(FeedHtmlContentDao.Properties.Id.eq(id)).count() > 0) {
            return htmlContentDao.queryBuilder().where(FeedHtmlContentDao.Properties.Id.eq(id)).list().get(0);
        }
        return new FeedHtmlContent();
    }
}
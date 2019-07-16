package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.models.greendao.Course;
import in.testpress.testpress.R;
import in.testpress.util.ImageUtils;

class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int[][] webLinks = {
            { R.string.babapedia_2020 , R.drawable.book, R.string.babapedia_url_2020 },
            { R.string.ilp_2020, R.drawable.paper_airplane, R.string.ilp_url_2020}
    };

    private Activity mActivity;
    private List<Course> mCourses = new ArrayList<>();
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    CourseListAdapter(Activity activity, List<Course> attempts) {
        mActivity = activity;
        mCourses = attempts;
        mImageLoader = ImageUtils.initImageLoader(activity);
        mOptions = ImageUtils.getPlaceholdersOption();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle;
        TextView externalLinkTitle;
        ImageView thumbnailImage;
        LinearLayout courseItemLayout;
        LinearLayout progressBarLayout;


        ViewHolder(View convertView, Context context) {
            super(convertView);
            courseTitle = ((TextView) convertView.findViewById(R.id.course_title));
            courseTitle.setTypeface(TestpressSdk.getRubikMediumFont(context));
            thumbnailImage = ((ImageView) convertView.findViewById(R.id.thumbnail_image));
            courseItemLayout = (LinearLayout) convertView.findViewById(R.id.course_item_layout);
            externalLinkTitle = (TextView) convertView.findViewById(R.id.external_link_title);
            courseItemLayout = (LinearLayout) convertView.findViewById(R.id.course_item_layout);
            progressBarLayout = ((LinearLayout) convertView.findViewById(R.id.progress_bar_layout));
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mCourses.size() + webLinks.length;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.testpress_course_list_item, parent, false);

        return new ViewHolder(v, mActivity);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        if (mCourses.size() > position) {
            final Course course = mCourses.get(position);
            holder.courseTitle.setText(course.getTitle());
            if (course.getImage() == null || course.getImage().isEmpty()) {
                holder.thumbnailImage.setVisibility(View.GONE);
            } else {
                holder.thumbnailImage.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(course.getImage(), holder.thumbnailImage, mOptions);
            }
            holder.progressBarLayout.setVisibility(View.GONE);
            setTextToTextView(course.getExternal_link_label(), holder.externalLinkTitle);
            toggleTextViewVisibility(!course.isCourseForRegistration(), holder.externalLinkTitle);
            holder.courseItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //noinspection ConstantConditions
                    openCourseContentsOrExternalLink(mActivity, course, !course.isCourseForRegistration());
                }
            });
        } else {
            final int webLinkPosition = position - mCourses.size();
            holder.courseTitle.setText(webLinks[webLinkPosition][0]);
            holder.thumbnailImage.setVisibility(View.VISIBLE);
            holder.thumbnailImage.setImageResource(webLinks[webLinkPosition][1]);
            holder.progressBarLayout.setVisibility(View.GONE);
            holder.courseItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, InAppBrowserActivity.class);
                    intent.putExtra(InAppBrowserActivity.TITLE,
                            mActivity.getString(webLinks[webLinkPosition][0]));

                    intent.putExtra(InAppBrowserActivity.URL,
                            mActivity.getString(webLinks[webLinkPosition][2]));

                    mActivity.startActivity(intent);
                }
            });
        }
    }

    void setCourses(List<Course> mCourses) {
        this.mCourses = mCourses;
    }

    public void toggleTextViewVisibility(boolean toHide, View view) {
        if (toHide) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void setTextToTextView(String textViewText, TextView textView) {
        if (!textViewText.equals("")) {
            textView.setText(textViewText);
        }
    }

    public void openCourseContentsOrExternalLink(Activity activity, Course course, boolean openCourseContent) {

        if (openCourseContent) {
            TestpressCourse.showChapters(
                    mActivity,
                    course.getTitle(),
                    course.getId().intValue(),
                    TestpressSdk.getTestpressSession(mActivity)
            );
        } else {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra("URL", course.getExternal_content_link());
            intent.putExtra("TITLE", course.getExternal_link_label());
            activity.startActivity(intent);
        }
    }

}
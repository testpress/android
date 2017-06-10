package in.testpress.testpress.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.models.Comment;
import in.testpress.testpress.util.FormatDate;
import in.testpress.testpress.util.UILImageGetter;
import in.testpress.testpress.util.ZoomableImageString;
import in.testpress.util.ViewUtils;

class CommentsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<Comment> comments = new ArrayList<>();

    CommentsListAdapter(Activity activity) {
        this.activity = activity;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.profile_image_place_holder)
                .showImageOnLoading(R.drawable.profile_image_place_holder).build();
    }

    private static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView comment;
        ImageView userImage;
        TextView submitDate;
        View divider;

        CommentsViewHolder(View convertView) {
            super(convertView);
            divider = convertView.findViewById(R.id.comment_seperator);
            userName = ((TextView) convertView.findViewById(R.id.user_name));
            comment = ((TextView) convertView.findViewById(R.id.comment));
            userImage = ((ImageView) convertView.findViewById(R.id.display_picture));
            submitDate = ((TextView) convertView.findViewById(R.id.submit_date));
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.comments_list_item, parent, false);

        return new CommentsViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CommentsViewHolder) {
            final CommentsViewHolder holder = (CommentsViewHolder) viewHolder;
            Comment comment = comments.get(position);
            imageLoader.displayImage(comment.getUser().getMediumImage(), holder.userImage, options);
            if (comment.getUser().getMediumImage().isEmpty()) {
                holder.userImage.setColorFilter(Color.parseColor("#888888"));
            } else {
                holder.userImage.clearColorFilter();
            }
            holder.userName.setText(comment.getUser().getDisplayName());
            Spanned htmlSpan = Html.fromHtml(comment.getComment(),
                    new UILImageGetter(holder.comment, activity), null);

            ZoomableImageString zoomableImageQuestion = new ZoomableImageString(activity);
            holder.comment.setText(zoomableImageQuestion.convertString(htmlSpan));
            holder.comment.setMovementMethod(LinkMovementMethod.getInstance());

            //noinspection ConstantConditions
            long submitDateMillis = FormatDate.getDate(comment.getSubmitDate(),
                    "yyyy-MM-dd'T'HH:mm:ss", "UTC").getTime();

            holder.submitDate.setText(FormatDate.getAbbreviatedTimeSpan(submitDateMillis));

            // Hide item separator for last item
            holder.divider.setVisibility((position + 1 == comments.size()) ? View.GONE : View.VISIBLE);

            holder.userName.setTypeface(TestpressSdk.getRubikMediumFont(activity));
            ViewUtils.setTypeface(new TextView[] {holder.submitDate, holder.comment},
                    TestpressSdk.getRubikRegularFont(activity));
        }
    }

    void addComments(List<Comment> comments) {
        this.comments.addAll(comments);
        Collections.sort(this.comments, new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                return FormatDate.compareDate(o1.getSubmitDate(), o2.getSubmitDate(),
                        "yyyy-MM-dd'T'HH:mm:ss", "UTC") ? 1 : -1;
            }
        });
        notifyDataSetChanged();
    }

    List<Comment> getComments() {
        return comments;
    }

}

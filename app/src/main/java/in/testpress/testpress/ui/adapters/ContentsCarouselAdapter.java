package in.testpress.testpress.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.models.greendao.Chapter;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.Exam;
import in.testpress.models.greendao.HtmlContent;
import in.testpress.models.greendao.VideoAttempt;
import in.testpress.models.greendao.Video;
import in.testpress.testpress.R;
import in.testpress.testpress.models.pojo.DashboardResponse;
import in.testpress.testpress.models.pojo.DashboardSection;
import in.testpress.testpress.util.UIUtils;
import in.testpress.testpress.util.ImageUtils;
import in.testpress.util.IntegerList;

public class ContentsCarouselAdapter extends RecyclerView.Adapter<ContentsCarouselAdapter.ItemViewHolder> {
    private DashboardResponse response;
    private DashboardSection section;
    private List<Content> contents = new ArrayList<>();
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;

    public ContentsCarouselAdapter(DashboardResponse response, DashboardSection currentSection, Context context) {
        this.response = response;
        this.section = currentSection;
        populateContents();
        this.context = context;
        imageLoader = in.testpress.util.ImageUtils.initImageLoader(context);
        options = ImageUtils.getPlaceholdersOption();
    }

    private void populateContents() {
        IntegerList items = section.getItems();
        if (section.getContentType().equals("chapter_content")) {
            for (Integer item : items) {
                this.contents.add(this.response.getContentHashMap().get(Long.valueOf(item)));
            }
        } else {
            for (Integer item : items) {
                CourseAttempt attempt = this.response.getContentAttemptHashMap().get(Long.valueOf(item));
                if (attempt != null) {
                    this.contents.add(this.response.getContentHashMap().get(attempt.getChapterContentId()));
                }
            }
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_carousel_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        final Content content = contents.get(position);
        if (content == null) {
            return;
        }

        showThumbnail(content, holder);
        setIconAndChapterTitle(content, holder);
        showOrHideVideoAccessories(content, holder);
        showOrHideExamAccessories(content, holder);
        showReadTimeForHtmlContent(content, holder);
        showIconForAttachmentContent(content, holder);
        showProgressBarForResumeVideos(position, holder);


        holder.title.setText(content.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = (Activity) context;
                TestpressSession session = TestpressSdk.getTestpressSession(context);
                TestpressCourse.showContentDetail(activity, content.getId().toString(), session);
            }
        });
    }

    private void showThumbnail(Content content, ItemViewHolder holder) {
        if (content.getCoverImageMedium() != null) {
            imageLoader.displayImage(content.getCoverImageMedium(), holder.image, options);
        } else {
            showThumbnailForVideo(content, holder);
        }
        holder.image.setColorFilter(Color.parseColor("#22000000"));

    }

    private void showThumbnailForVideo(Content content, ItemViewHolder holder) {
        imageLoader.displayImage(null, holder.image, options);
        if (content.getVideoId() != null) {
            Video video = response.getVideoHashMap().get(content.getVideoId());

            if (video != null && video.getThumbnailMedium() != null) {
                imageLoader.displayImage(video.getThumbnailMedium(), holder.image, options);
            } else {
                holder.image.setBackgroundColor(Color.parseColor("#77000000"));
            }
        }
    }

    private void showIconForAttachmentContent(Content content, ItemViewHolder holder) {
        if (content.getAttachmentId() != null) {
            holder.playIcon.setImageResource(R.drawable.ic_attachment);
            holder.playIcon.setVisibility(View.VISIBLE);
        }
    }

    private void showReadTimeForHtmlContent(Content content, ItemViewHolder holder) {
        if (content.getHtmlId() != null) {
            HtmlContent htmlContent = response.getHtmlContentHashMap().get(content.getHtmlId());
            holder.infoLayout.setVisibility(View.VISIBLE);
            if (htmlContent != null) {
                holder.numberOfQuestions.setText(htmlContent.getReadTime());
            }
            holder.infoSubtitle.setVisibility(View.GONE);
        }
    }
    
    private void showProgressBarForResumeVideos(int position, ItemViewHolder holder) {
        holder.videoProgressLayout.setVisibility(View.GONE);
        if (section.getSlug().equals("resume")) {
            Long attemptId = Long.valueOf(section.getItems().get(position));
            CourseAttempt attempt = this.response.getContentAttemptHashMap().get(attemptId);

            if (attempt != null && attempt.getUserVideoId() != null) {
                VideoAttempt userVideo = response.getUserVideoHashMap().get(attempt.getUserVideoId());

                if (userVideo != null && userVideo.getRawVideoContent() != null && userVideo.getRawVideoContent().getDuration() != null) {
                    float lastPosition = Float.parseFloat(userVideo.getLastPosition());
                    float totalDuration = Float.parseFloat(userVideo.getRawVideoContent().getDuration());
                    float watchPercentage = (lastPosition / totalDuration) * 100;
                    holder.videoProgress.setProgress((int) watchPercentage);
                    holder.videoProgressLayout.setVisibility(View.VISIBLE);
                }
            }
        } else {
            holder.videoProgressLayout.setVisibility(View.GONE);
        }
    }

    private void setIconAndChapterTitle(Content content, ItemViewHolder holder) {
        if (response.getChapterHashMap().get(content.getChapterId()) != null) {
            Chapter chapter = response.getChapterHashMap().get(content.getChapterId());
            if (chapter != null) {
                holder.subtitle.setText(chapter.getName());
            }
        }

        switch (content.getContentType().toLowerCase()) {
            case "video":
                holder.contentTypeIcon.setImageResource(R.drawable.ic_video_white);
                break;
            case "exam":
                holder.contentTypeIcon.setImageResource(R.drawable.ic_exam_white);
                break;
            case "notes":
                holder.contentTypeIcon.setImageResource(R.drawable.ic_news_white);
                break;
            case "attachment":
                holder.contentTypeIcon.setImageResource(R.drawable.ic_attachment);
                break;
        }
    }

    private void showOrHideVideoAccessories(Content content, ItemViewHolder holder) {
        if (content.getVideoId() != null) {
            holder.playIcon.setVisibility(View.VISIBLE);
            holder.playIcon.setImageResource(R.drawable.play);
        } else {
            holder.playIcon.setVisibility(View.GONE);
        }
    }

    private void showOrHideExamAccessories(Content content, ItemViewHolder holder) {
        if (content.getExamId() != null) {
            Exam exam = response.getExamHashMap().get(content.getExamId());
            holder.infoLayout.setVisibility(View.VISIBLE);
            holder.infoSubtitle.setVisibility(View.VISIBLE);
            if (exam != null) {
                holder.numberOfQuestions.setText(exam.getNumberOfQuestions().toString());
            }
        } else {
            holder.infoLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView image, playIcon, contentTypeIcon;
        TextView title, numberOfQuestions, subtitle, infoSubtitle;
        LinearLayout infoLayout, videoProgressLayout;
        ProgressBar videoProgress;

        public ItemViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_view);
            playIcon = (ImageView) itemView.findViewById(R.id.play_icon);
            contentTypeIcon = (ImageView) itemView.findViewById(R.id.content_type_icon);
            infoLayout = (LinearLayout) itemView.findViewById(R.id.info_layout);
            videoProgressLayout = (LinearLayout) itemView.findViewById(R.id.video_progress_layout);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            numberOfQuestions = (TextView) itemView.findViewById(R.id.number_of_questions);
            numberOfQuestions.setTypeface(TestpressSdk.getRubikMediumFont(context));
            infoSubtitle = (TextView) itemView.findViewById(R.id.info_subtitle);
            videoProgress = (ProgressBar) itemView.findViewById(R.id.video_progress);

            title.setTypeface(UIUtils.getLatoSemiBoldFont(context));
        }
    }
}

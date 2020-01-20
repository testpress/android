package in.testpress.testpress.ui.view_holders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import in.testpress.core.TestpressSDKDatabase;
import in.testpress.models.greendao.Content;
import in.testpress.models.greendao.ContentDao;
import in.testpress.models.greendao.CourseAttempt;
import in.testpress.models.greendao.CourseAttemptDao;
import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.ui.adapters.ContentsCarouselAdapter;

public class ContentsCarouselViewHolder extends BaseCarouselViewHolder {
    public ContentsCarouselViewHolder(View itemView, Context context) {
        super(itemView, context);
    }

    public void display(List<DashboardSection> sections, Context context) {
        DashboardSection section = sections.get(getAdapterPosition());
        List<Content> contents = getContents(section, context);
        ContentsCarouselAdapter adapter = new ContentsCarouselAdapter(contents, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        title.setText(sections.get(getAdapterPosition()).getDisplayName());
    }

    private List<Content> getContents(DashboardSection section, Context context) {
        List<Integer> contentIds = new ArrayList<>();

        if (section.getContentType().equals("chapter_content_attempt")) {
            List<CourseAttempt> contentAttempts = TestpressSDKDatabase.getCourseAttemptDao(context)
                    .queryBuilder().where(CourseAttemptDao.Properties.Id.in(section.getItems())).list();

            for (CourseAttempt contentAttempt : contentAttempts) {
                contentIds.add(contentAttempt.getChapterContentId().intValue());
            }
        } else if (section.getContentType().equals("chapter_content")) {
            contentIds.addAll(section.getItems());
        }

        return TestpressSDKDatabase.getContentDao(context).queryBuilder()
                .where(ContentDao.Properties.Id.in(contentIds)).list();
    }

}

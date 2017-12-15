package in.testpress.testpress.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import in.testpress.course.ui.ContentActivity;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.models.ForumDao;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.Ln;
import retrofit.RetrofitError;


public class ForumsPager extends ResourcePager<Forum> {

    TestpressApiResponse<Forum> response;
    private String latestModifiedDate;
    private SimpleDateFormat simpleDateFormat;
    private ForumDao forumDao;

    public ForumsPager(TestpressService service, ForumDao forumDao) {
        super(service);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        this.forumDao = forumDao;
    }

    @Override
    public ResourcePager<Forum> clear() {
        response = null;
        super.clear();
        return this;
    }

    @Override
    protected Object getId(Forum resource) {
        return resource.getId();
    }

    @Override
    public List<Forum> getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_FORUMS_FRAG;
//            url = Constants.Http.URL_POSTS_FRAG;
        } else {
            try {
                URL full = new URL(response.getNext());
                url = full.getFile().substring(1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                url = null;
            }
        }
        if (url != null) {
            response = service.getForums(url, queryParams, latestModifiedDate);
            return response.getResults();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean next() throws IOException {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        boolean emptyPage = false;
        try {
            for (int i = 0; i < count && hasNext(); i++) {
                List<Forum> resourcePage = getItems(page, -1);
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                for (Forum resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null) {
                        continue;
                    }
                    if(resource.category != null) {
                        resource.setCategory(resource.category);
                    }
                    resource.setPublished(simpleDateFormat.parse(resource.getPublishedDate()).getTime());
                    resource.setModifiedDate(simpleDateFormat.parse(resource.getModified()).getTime());
                    resources.put(getId(resource), resource);
                }
            }
            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (ParseException e) {
            Ln.d("ParseException " + e);
        } catch (Exception e) {
            hasMore = false;
            throw e;
        }
        hasMore = hasNext() && !emptyPage;
        return hasMore;
    }

    @Override
    protected Forum register(Forum forum){
        try {
            // Omit the post if its published date less then the lowest published date in db
            if ((forumDao != null) && (forumDao.count() != 0) && (simpleDateFormat.parse(
                    forum.getPublishedDate()).getTime() < forumDao.queryBuilder().orderDesc(
                    ForumDao.Properties.Published).list().get((int) forumDao.count() - 1)
                    .getPublished())) {

                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return forum;
    }

    @Override
    public boolean hasNext() {
        if (response == null || response.getNext() != null) {
            return true;
        }
        return false;
    }

    public int getTotalCount() {
        return response.getCount();
    }

    public void setLatestModifiedDate(String latestModifiedDate) {
        this.latestModifiedDate = latestModifiedDate;
    }
}

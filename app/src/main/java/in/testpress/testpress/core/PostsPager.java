package in.testpress.testpress.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.Ln;
import retrofit.RetrofitError;


public class PostsPager extends ResourcePager<Post> {

    TestpressApiResponse<Post> response;
    String latestModifiedDate;
    SimpleDateFormat simpleDateFormat;
    PostDao postDao;

    public PostsPager(TestpressService service, PostDao postDao) {
        super(service);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        this.postDao = postDao;
    }

    @Override
    public ResourcePager<Post> clear() {
        response = null;
        super.clear();
        return this;
    }

    @Override
    protected Object getId(Post resource) {
        return resource.getId();
    }

    @Override
    public List<Post> getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_POSTS_FRAG;
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
            response = service.getPosts(url, queryParams, latestModifiedDate);
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
                Ln.d("PostsPager Getting Items");
                List<Post> resourcePage = getItems(page, -1);
                Ln.d("PostsPager Items Received");
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                Ln.d("Looping through resources");
                for (Post resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    if(resource.getCategory() != null) {
                        resource.setCategory(resource.getCategory());
                    }
                    Ln.d("Category ID " + resource.getCategoryId());
                    resource.setPublished(simpleDateFormat.parse(resource.getPublishedDate()).getTime());
                    resource.setModifiedDate(simpleDateFormat.parse(resource.getModified()).getTime());
                    resources.put(getId(resource), resource);
                }
                Ln.d("Looping resources over");
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
    protected Post register(Post post){
        try {
            // Omit the post if its published date less then the lowest published date in db
            if ((postDao != null) && (postDao.count() != 0) && (simpleDateFormat.parse(
                    post.getPublishedDate()).getTime() < postDao.queryBuilder().orderDesc(
                    PostDao.Properties.Published).list().get((int) postDao.count() - 1)
                    .getPublished())) {

                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return post;
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

package in.testpress.testpress.core;

import android.content.Context;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.Ln;
import retrofit.RetrofitError;


public class PostsPager extends ResourcePager<Post> {

    TestpressApiResponse<Post> response;
    DaoSession daoSession;
    PostDao postDao;
    CategoryDao categoryDao;

    public PostsPager(TestpressService service, Context context) {
        super(service);
        daoSession = ((TestpressApplication) context.getApplicationContext()).getDaoSession();
        postDao = daoSession.getPostDao();
        categoryDao = daoSession.getCategoryDao();
    }

    @Override
    public ResourcePager<Post> clear() {
        response = null;
        super.clear();
        hasMore=false;
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
            response = service.getPosts(url, queryParams);
            return response.getResults();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean next() throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        boolean emptyPage = false;
        networkFail = false;
        try {
            for (int i = 0; i < count && hasNext(); i++) {
                List<Post> resourcePage = getItems(page, -1);
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                for (Post resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    if(resource.category != null) {
                        categoryDao.insertOrReplace(resource.category);
                        resource.setCategory(resource.category);
                    }
                    resource.setCreatedDate(simpleDateFormat.parse(resource.getCreated()).getTime());
                    resources.put(getId(resource), resource);
                    postDao.insertOrReplace(resource);
                }
            }
            // Set page to count value if first call after call to reset()
            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (ParseException e) {
            Ln.e("ParseException " + e);
        } catch (Exception e) {
            hasMore = false;
            networkFail = true;
            throw e;
        }
        hasMore = hasNext() && !emptyPage;
        return hasMore;
    }

    @Override
    public boolean hasNext() {
        if (response == null) {
            return true;
        }
        if (response.getNext() != null) {
            if(queryParams != null)
            queryParams.clear();
            return true;
        }
        return false;
    }
}

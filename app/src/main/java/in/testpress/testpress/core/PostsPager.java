package in.testpress.testpress.core;

import android.content.Context;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.CategoryDao;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Post;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.util.Ln;
import retrofit.RetrofitError;


public class PostsPager extends ResourcePager<Post> {

    TestpressApiResponse<Post> response;

    public PostsPager(TestpressService service, Context context) {
        super(service);
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
                Ln.e("PostsPager Getting Items");
                List<Post> resourcePage = getItems(page, -1);
                Ln.e("PostsPager Items Received");
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;
                Ln.e("Looping through resources");
                for (Post resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    if(resource.category != null) {
                        resource.setCategory(resource.category);
                    }
                    resource.setCreatedDate(simpleDateFormat.parse(resource.getCreated()).getTime());
                    resources.put(getId(resource), resource);
                }

                Ln.e("Looping resources over");
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
        if (response == null || response.getNext() != null) {
            return true;
        }

        return false;
    }

    public int getTotalItemsWantTOLoad() {
        return response.getCount();
    }
}

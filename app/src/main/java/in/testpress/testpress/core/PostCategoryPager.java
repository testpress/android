package in.testpress.testpress.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.RetrofitError;

public class PostCategoryPager extends ResourcePager<Category> {

    private TestpressApiResponse<Category> response;

    public PostCategoryPager(TestpressService service) {
        super(service);
    }

    @Override
    public ResourcePager<Category> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Category resource) {
        return resource.getId();
    }

    @Override
    public List<Category> getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_CATEGORIES_FRAG;
        } else {
            queryParams.clear();
            try {
                URL full = new URL(response.getNext());
                url = full.getFile().substring(1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                url = null;
            }
        }
        if (url != null) {
            response = service.getCategories(url, queryParams);
            return response.getResults();
        }
        return Collections.emptyList();
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

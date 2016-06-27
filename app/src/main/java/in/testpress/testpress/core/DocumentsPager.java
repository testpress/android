package in.testpress.testpress.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Notes;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.RetrofitError;


public class DocumentsPager extends ResourcePager<Notes> {

    TestpressApiResponse<Notes> response;

    public DocumentsPager(TestpressService service) {
        super(service);
    }

    @Override
    public ResourcePager<Notes> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Notes resource) {
        return resource.getId();
    }

    @Override
    public List<Notes> getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_DOCUMENTS_FRAG;
            queryParams.put("type", "attachment");
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
            try {
                response = service.getDocumentsList(url, queryParams);
                return response.getResults();
            }
            catch (Exception e) {
                if((e.getMessage()).equals("403 FORBIDDEN")) {
                    throw e;
                } else {
                    return null;
                }
            }
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

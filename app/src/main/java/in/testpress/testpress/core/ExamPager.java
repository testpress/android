package in.testpress.testpress.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.RetrofitError;


public class ExamPager extends ResourcePager<Exam> {
    String subclass;
    TestpressApiResponse<Exam> response;

    public ExamPager(String subclass, TestpressService service) {
        super(service);
        this.subclass = subclass;
    }

    @Override
    public ResourcePager<Exam> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Exam resource) {
        return resource.getId();
    }

    @Override
    public List<Exam> getItems(int page, int size) throws RetrofitError {
        String url = null;
        if (response == null) {
            url = Constants.Http.URL_EXAMS_FRAG;
            queryParams.put(Constants.Http.STATE, subclass);
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
            try {
                response = service.getExams(url, queryParams);
                return response.getResults();
            }
            catch (Exception e) {
                throw e;
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

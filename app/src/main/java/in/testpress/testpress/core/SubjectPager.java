package in.testpress.testpress.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Subject;
import in.testpress.testpress.models.TestpressApiResponse;
import retrofit.RetrofitError;

public class SubjectPager extends ResourcePager<Subject> {

    TestpressApiResponse<Subject> response;

    public SubjectPager(TestpressService service) {
        super(service);
    }

    @Override
    public ResourcePager<Subject> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Subject resource) {
        return resource.getId();
    }

    @Override
    public List<Subject> getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_SUBJECT_ANALYTICS_FRAG;
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
            response = service.getSubjects(url, queryParams);
            return response.getResults();
        }
        return Collections.emptyList();
    }

    @Override
    protected Subject register(Subject subject){
        // Discard the Subject if its Total answered count is zero
        if ((subject != null) && (subject.getTotal() != 0)) {
            subject.setCorrectPercentage(getPercentage(subject.getCorrect(), subject.getTotal()));
            subject.setIncorrectPercentage(getPercentage(subject.getIncorrect(), subject.getTotal()));
            subject.setUnansweredPercentage(getPercentage(subject.getUnanswered(), subject.getTotal()));
            return subject;
        }
        return null;
    }

    private float getPercentage(int value, int total) {
        return ((float) value) / total * 100f;
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

package in.testpress.testpress.core;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;


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
    public List<Exam> getItems(int page, int size) {
        String url = null;
        if (response == null) {
            if (subclass.equals("available")) {
                url = Constants.Http.URL_AVAILABLE_EXAMS_FRAG;
            } else if (subclass.equals("upcoming")) {
                url = Constants.Http.URL_UPCOMING_EXAMS_FRAG;
            } else if (subclass.equals("history")) {
                url = Constants.Http.URL_HISTORY_EXAMS_FRAG;
            } else {
                url = Constants.Http.URL_AVAILABLE_EXAMS_FRAG;
            }
        } else {
            try {
                URL full = new URL(response.getNext());
                url = full.getFile();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                url = null;
            }

        }
        if (url != null) {
            response = service.getExams(url);
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
            return true;
        }

        return false;
    }
}

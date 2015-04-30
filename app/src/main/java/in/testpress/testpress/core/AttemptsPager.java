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

import in.testpress.testpress.models.Attempt;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;


public class AttemptsPager extends ResourcePager<Attempt> {
    Exam exam;
    TestpressApiResponse<Attempt> response;

    public AttemptsPager(Exam exam, TestpressService service) {
        super(service);
        this.exam = exam;
    }

    @Override
    public ResourcePager<Attempt> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(Attempt resource) {
        return resource.getId();
    }

    @Override
    public List<Attempt> getItems(int page, int size) {
        String url = null;
        if (response == null) {
            url = exam.getAttemptsFrag();
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
            response = service.getAttempts(url);
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

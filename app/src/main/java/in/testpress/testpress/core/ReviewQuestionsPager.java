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
import in.testpress.testpress.models.ReviewItem;
import in.testpress.testpress.models.TestpressApiResponse;


public class ReviewQuestionsPager extends ResourcePager<ReviewItem> {
    Attempt attempt;
    String filter;
    TestpressApiResponse<ReviewItem> response;

    public ReviewQuestionsPager(Attempt attempt, String filter, TestpressService service) {
        super(service);
        this.attempt = attempt;
        this.filter = filter;
    }

    @Override
    public ResourcePager<ReviewItem> clear() {
        response = null;
        return super.clear();
    }

    @Override
    protected Object getId(ReviewItem resource) {
        return resource.getId();
    }

    @Override
    public List<ReviewItem> getItems(int page, int size) {
        String url = null;
        if (response == null) {
            url = attempt.getReviewFrag();
            if (filter.equals("all") == false) {
                url = url + "?state=" + filter;
            }
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
            response = service.getReviewItems(url);
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

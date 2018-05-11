package in.testpress.testpress.core;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.testpress.testpress.models.ActivityFeedResponse;
import in.testpress.testpress.models.TestpressDataApiResponse;
import in.testpress.testpress.util.Ln;
import retrofit.RetrofitError;

public class ActivityFeedPager extends DataResourcePager<ActivityFeedResponse> {

    TestpressDataApiResponse<ActivityFeedResponse> response;

    public ActivityFeedPager(TestpressService service) {
        super(service);
    }

    @Override
    protected Object getId(ActivityFeedResponse resource) {
        return page;
    }

    @Override
    public ActivityFeedResponse getItems(int page, int size) throws RetrofitError {
        String url;
        if (response == null) {
            url = Constants.Http.URL_ACTIVITY_FEED_FRAG;
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
            response = service.getActivityFeed(url);
            Log.e("Inside", "getItems");
            Log.e("Response", (response.getResults()==null)+"");
            return response.getResults();
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (response == null || response.getNext() != null) {
            return true;
        }
        return false;
    }
}

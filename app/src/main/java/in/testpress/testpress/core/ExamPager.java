package in.testpress.testpress.core;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.models.Exam;
import in.testpress.testpress.models.TestpressApiResponse;
import in.testpress.testpress.ui.MainActivity;
import retrofit.RetrofitError;


public class ExamPager extends ResourcePager<Exam> {
    String subclass;
    TestpressApiResponse<Exam> response;
    Activity activity;

    @Inject protected TestpressServiceProvider serviceProvider;
    @Inject protected LogoutService logoutService;

    public ExamPager(String subclass, TestpressService service, Activity activity) {
        super(service);
        this.subclass = subclass;
        this.activity = activity;
        Injector.inject(this);
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
                if((e.getMessage()).equals("403 FORBIDDEN")) {
                   serviceProvider.invalidateAuthToken();
                   logoutService.logout(new Runnable() {
                       @Override
                       public void run() {
                           Intent intent = activity.getIntent();
                           activity.finish();
                           activity.startActivity(intent);
                       }
                   });
                }
               else return null;
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

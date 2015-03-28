package in.testpress.testpress.core;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


import javax.inject.Inject;

import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.ui.MainActivity;
import retrofit.RestAdapter;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static in.testpress.testpress.core.Constants.Auth.AUTHTOKEN_TYPE;
import static in.testpress.testpress.core.Constants.Auth.TESTPRESS_ACCOUNT_TYPE;

public class TestpressService {
    private RestAdapter restAdapter;
    private String authToken;

    public TestpressService() {
    }

    /**
     * Create testpress service
     *
     * @param restAdapter The RestAdapter that allows HTTP Communication.
     */
    public TestpressService(RestAdapter restAdapter) {
        this.restAdapter = restAdapter;
    }

    public TestpressService(RestAdapter restAdapter, String authToken) {
        this.restAdapter = restAdapter;
        this.authToken = authToken;
    }

    private RestAdapter getRestAdapter() {
        return restAdapter;
    }

    private AuthenticationService getAuthenticationService() {
        return getRestAdapter().create(AuthenticationService.class);
    }

    private ExamService getExamsService() { return getRestAdapter().create(ExamService.class); }

    private String getAuthToken() {
        return "JWT " + authToken;
    }

    public List<Exam> getAvailableExams() {
        return getExamsService().getAvailableExams(getAuthToken()).getResults();
    }

    public List<Exam> getUpcomingExams() {
        return getExamsService().getUpcomingExams(getAuthToken()).getResults();
    }

    public List<Exam> getHistoryExams() {
        return getExamsService().getHistoryExams(getAuthToken()).getResults();
    }

    public String authenticate(String username, String password) {
        HashMap<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", username);
        credentials.put("password", password);
        authToken = getAuthenticationService().authenticate(credentials).getToken();
        return authToken;
    }
}

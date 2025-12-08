package in.testpress.testpress.ui;

import android.os.Bundle;

import javax.inject.Inject;

import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.util.CommonUtils;

public class BaseAuthenticatedActivity extends BaseToolBarActivity {

    @Inject
    protected TestpressService testpressService;
    @Inject
    protected TestpressServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestpressApplication.getAppComponent().inject(this);
        if (!CommonUtils.isUserAuthenticated(this)) {
            serviceProvider.logout(this, testpressService, serviceProvider, logoutService);
        }
    }

}

package in.testpress.testpress.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

import javax.inject.Inject;


/**
 * Class used for logging a user out.
 */
public class LogoutService {

    protected final Context context;
    protected final AccountManager accountManager;

    @Inject
    public LogoutService(final Context context, final AccountManager accountManager) {
        this.context = context;
        this.accountManager = accountManager;
    }

    public void logout(final Runnable onSuccess) {
        new LogoutTask(context, onSuccess).execute();
    }

    private static class LogoutTask extends SafeAsyncTask<Boolean> {

        private final Context taskContext;
        private final Runnable onSuccess;

        protected LogoutTask(final Context context, final Runnable onSuccess) {
            this.taskContext = context;
            this.onSuccess = onSuccess;
        }

        @Override
        public Boolean call() throws Exception {

            final AccountManager accountManagerWithContext = AccountManager.get(taskContext);
            if (accountManagerWithContext != null) {
                final Account[] accounts = accountManagerWithContext
                        .getAccountsByType(Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                if (accounts.length > 0) {
                    final AccountManagerFuture<Boolean> removeAccountFuture
                            = accountManagerWithContext.removeAccount(accounts[0], null, null);

                    return removeAccountFuture.getResult();
                }
            } else {
                Ln.w("accountManagerWithContext is null");
            }

            return false;
        }

        @Override
        protected void onSuccess(final Boolean accountWasRemoved) throws Exception {
            super.onSuccess(accountWasRemoved);

            Ln.d("Logout succeeded: %s", accountWasRemoved);
            onSuccess.run();

        }

        @Override
        protected void onException(final Exception e) throws RuntimeException {
            super.onException(e);

            Ln.e(e.getCause(), "Logout failed.");
        }
    }

    public void logout(final Activity activity, TestpressServiceProvider serviceProvider, LogoutService logoutService) {
        final MaterialDialog materialDialog = new MaterialDialog.Builder(activity)
                .title(R.string.label_logging_out)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .show();
        serviceProvider.invalidateAuthToken();
        //set the user is unAuthenticated, to check the auth in MainActivity
        ((TestpressApplication) activity.getApplicationContext()).setUserAuthenticated(false);
        DaoSession daoSession = ((TestpressApplication) activity.getApplicationContext()).getDaoSession();
        PostDao postDao = daoSession.getPostDao();
        postDao.deleteAll();
        daoSession.clear();
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling a checkAuth in MainActivity will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                Intent intent;
                if(activity.getClass() == MainActivity.class) {
                    intent = activity.getIntent();
                } else {
                    intent = new Intent(activity, MainActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                materialDialog.dismiss();
                activity.finish();
                activity.startActivity(intent);
            }
        });
    }
}

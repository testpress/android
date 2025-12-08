package in.testpress.testpress.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;

import javax.inject.Inject;

import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;


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
                        .getAccountsByType(APPLICATION_ID);
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
}

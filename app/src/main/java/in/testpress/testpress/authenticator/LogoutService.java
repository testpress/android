package in.testpress.testpress.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import in.testpress.testpress.util.SafeAsyncTask;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;


/**
 * Class used for logging a user out.
 */
public class LogoutService {

    private static final String TAG = "LogoutService";

    protected final Context context;
    protected final AccountManager accountManager;

    @Inject
    public LogoutService(@NonNull final Context context, @NonNull final AccountManager accountManager) {
        this.context = context;
        this.accountManager = accountManager;
    }

    public void logout(@Nullable final Runnable onSuccess) {
        new LogoutTask(context, onSuccess).execute();
    }

    private static class LogoutTask extends SafeAsyncTask<Boolean> {

        private final Context taskContext;
        private final Runnable onSuccess;
        private final AtomicBoolean hasRunCallback = new AtomicBoolean(false);

        protected LogoutTask(@NonNull final Context context, @Nullable final Runnable onSuccess) {
            this.taskContext = context;
            this.onSuccess = onSuccess;
        }

        private void runCallbackOnce() {
            if (onSuccess != null && hasRunCallback.compareAndSet(false, true)) {
                onSuccess.run();
            }
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

                    Boolean result = removeAccountFuture.getResult();
                    if (!Boolean.TRUE.equals(result)) {
                        Log.w(TAG, "AccountManager.removeAccount returned false");
                    }
                    return result;
                }
            }

            return false;
        }

        @Override
        protected void onSuccess(final Boolean accountWasRemoved) throws Exception {
            super.onSuccess(accountWasRemoved);
            runCallbackOnce();
        }

        @Override
        protected void onException(final Exception e) throws RuntimeException {
            super.onException(e);
            Log.w(TAG, "AccountManager.removeAccount failed with exception", e);
            // Even if account removal throws an exception (e.g. AuthenticatorException,
            // OperationCanceledException, IOException), we must invoke onSuccess to allow
            // the caller to dismiss the progress dialog and complete navigation.
            // Leaving the dialog displayed indefinitely traps the user in an unresponsive UI.
            // Local credentials were already wiped before account removal was attempted,
            // so navigation proceeds cleanly and anonymous device registration is triggered.
            // Guarded by runCallbackOnce (AtomicBoolean) so if onSuccess.run() throws, SafeAsyncTask's
            // exception routing into onException does not trigger the callback a second time.
            runCallbackOnce();
        }
    }
}

package in.testpress.testpress.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import in.testpress.testpress.core.Constants;
import in.testpress.testpress.util.Ln;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.accounts.AccountManager.KEY_INTENT;
import static in.testpress.testpress.authenticator.TestpressAuthenticatorActivity.PARAM_AUTHTOKEN_TYPE;


public class TestpressAccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context context;

    public TestpressAccountAuthenticator(final Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response,
                               final Account account, final String authTokenType,
                               final Bundle options) throws NetworkErrorException {
        Ln.d("Attempting to get authToken");

        final String authToken = AccountManager.get(context).peekAuthToken(account, authTokenType);

        final Bundle bundle = new Bundle();
        bundle.putString(KEY_ACCOUNT_NAME, account.name);
        bundle.putString(KEY_ACCOUNT_TYPE, Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        bundle.putString(KEY_AUTHTOKEN, authToken);

        return bundle;
    }

    @Override
    public String getAuthTokenLabel(final String authTokenType) {
        return authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE) ? authTokenType : null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    /*
     * The user has requested to add a new account to the system. We return an intent that will
     * launch our login screen if the user has not logged in yet, otherwise our activity will
     * just pass the user's credentials on to the account manager.
     */
    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType,
                             final String authTokenType, final String[] requiredFeatures,
                             final Bundle options) throws NetworkErrorException {
        //TODO Restrict to one account
        final Intent intent = new Intent(context, TestpressAuthenticatorActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account,
                              final String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }
}

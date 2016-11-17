package in.testpress.testpress.authenticator;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.R.layout;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.UnAuthorizedErrorEvent;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.OrderConfirmActivity;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import retrofit.RetrofitError;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class LoginActivity extends ActionBarAccountAuthenticatorActivity {
    /**
     * PARAM_CONFIRM_CREDENTIALS
     */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /**
     * PARAM_PASSWORD
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * PARAM_USERNAME
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * PARAM_AUTHTOKEN_TYPE
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private AccountManager accountManager;

    @Inject TestpressService testpressService;

    @Inject Bus bus;

    @InjectView(id.et_username) EditText usernameText;
    @InjectView(id.et_password) protected EditText passwordText;
    @InjectView(id.b_signin) protected Button signInButton;

    private final TextWatcher watcher = validationTextWatcher();

    private SafeAsyncTask<Boolean> authenticationTask;
    private String authToken;
    private String authTokenType;
    private MaterialDialog progressDialog;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;

    private String username;

    private String password;

    private String token;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Injector.inject(this);

        accountManager = AccountManager.get(this);

        final Intent intent = getIntent();
        username = intent.getStringExtra(PARAM_USERNAME);
        authTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        confirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);

        requestNewAccount = username == null;

        setContentView(layout.login_activity);

        ButterKnife.inject(this);

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && signInButton.isEnabled()) {
                    signIn();
                    return true;
                }
                return false;
            }
        });

        usernameText.addTextChangedListener(watcher);
        passwordText.addTextChangedListener(watcher);
        passwordText.setTypeface(Typeface.DEFAULT);
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable gitDirEditText) {
                updateUIWithValidation();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        updateUIWithValidation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(usernameText) && populated(passwordText);
        signInButton.setEnabled(populated);
    }

    private boolean populated(final EditText editText) {
        return editText.length() > 0;
    }

    @Subscribe
    public void onUnAuthorizedErrorEvent(UnAuthorizedErrorEvent unAuthorizedErrorEvent) {
        // Could not authorize for some reason.
        Toaster.showLong(LoginActivity.this, R.string.message_bad_credentials);
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * <p/>
     * Specified by android:onClick="handleLogin" in the layout xml
     *
     * @param view
     */
    public void handleLogin(final View view) {
        if (authenticationTask != null) {
            return;
        }

        if (requestNewAccount) {
            username = usernameText.getText().toString();
        }

        password = passwordText.getText().toString();
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.message_signing_in)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .show();

        authenticationTask = new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {

                token = testpressService.authenticate(username, password);

                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                showAlert("Invalid username/password");
                // Retrofit Errors are handled inside of the {
                if(!(e instanceof RetrofitError)) {
                    final Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if(cause != null) {
                        Toaster.showLong(LoginActivity.this, cause.getMessage());
                    }
                }
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                onAuthenticationResult(authSuccess);
            }

            @Override
            protected void onFinally() throws RuntimeException {
                progressDialog.dismiss();
                authenticationTask = null;
            }
        };
        authenticationTask.execute();
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(final boolean result) {
        final Account account = new Account(username, Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
        accountManager.setPassword(account, password);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     */

    protected void finishLogin() {
        updateDevice();
        final Account account = new Account(username, Constants.Auth.TESTPRESS_ACCOUNT_TYPE);

        authToken = token;

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
            accountManager.setAuthToken(account, Constants.Auth.TESTPRESS_ACCOUNT_TYPE, authToken);
        } else {
            accountManager.setPassword(account, password);
        }
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        PostDao postDao = daoSession.getPostDao();
        postDao.deleteAll();
        daoSession.clear();
        if (getIntent().getStringExtra("deeplinkTo") != null) {
            Intent intent;
            switch (getIntent().getStringExtra("deeplinkTo")) {
                case "payment":
                    intent = new Intent(this, OrderConfirmActivity.class);
                    intent.putExtra("isDeepLink", true);
                    intent.putExtras(getIntent().getExtras());
                    break;
                default:
                    intent = new Intent(this, MainActivity.class);
                    break;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            final Intent intent = new Intent();
            intent.putExtra(KEY_ACCOUNT_NAME, username);
            intent.putExtra(KEY_ACCOUNT_TYPE, Constants.Auth.TESTPRESS_ACCOUNT_TYPE);

            if (authTokenType != null
                    && authTokenType.equals(Constants.Auth.AUTHTOKEN_TYPE)) {
                intent.putExtra(KEY_AUTHTOKEN, authToken);
            }
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void updateDevice() {
        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
        new SafeAsyncTask<Device>() {
            @Override
            public Device call() throws Exception {
                String token = GCMPreference.getRegistrationId(getApplicationContext());
                return testpressService.register(token, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
            }

            @Override
            protected void onSuccess(final Device device) throws Exception {
                sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, true).apply();
            }
        }.execute();
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param result
     */
    public void onAuthenticationResult(final boolean result) {
        if (result) {
            if (!confirmCredentials) {
                finishLogin();
            } else {
                finishConfirmCredentials(true);
            }
        } else {
            Ln.d("onAuthenticationResult: failed to authenticate");
            if (requestNewAccount) {
                Toaster.showLong(LoginActivity.this,
                        R.string.message_auth_failed_new_account);
            } else {
                Toaster.showLong(LoginActivity.this,
                        R.string.message_auth_failed);
            }
        }
    }

    public void showAlert(String alertMessage) {
        new MaterialDialog.Builder(LoginActivity.this)
                .content(alertMessage)
                .neutralText(R.string.ok)
                .neutralColorRes(R.color.primary)
                .buttonsGravity(GravityEnum.CENTER)
                .show();
    }

    @OnClick(id.b_signin) public void signIn() {
        if(internetConnectivityChecker.isConnected()) {
            handleLogin(signInButton);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @OnClick(id.link_signup) public void signUp() {
        if(internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        } else {
            internetConnectivityChecker.showAlert();
        }

    }

    @OnClick(id.reset_password) public void verify() {
        if(internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }
}

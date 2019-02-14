package in.testpress.testpress.authenticator;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.R.layout;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.UnAuthorizedErrorEvent;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.Ln;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;

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

    @InjectView(id.login_layout) LinearLayout loginLayout;
    @InjectView(id.et_username) EditText usernameText;
    @InjectView(id.et_password) protected EditText passwordText;
    @InjectView(id.b_signin) protected Button signInButton;
    @InjectView(id.or) protected TextView orLabel;
    @InjectView(id.fb_login_button) protected LoginButton fbLoginButton;
    @InjectView(id.google_sign_in_button) protected Button googleLoginButton;
    @InjectView(id.social_sign_in_buttons) protected LinearLayout socialLoginLayout;
    @InjectView(id.signup) protected TextView signUpButton;

    @InjectView(id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;

    private final TextWatcher watcher = validationTextWatcher();

    private String authToken;
    private String authTokenType;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean confirmCredentials = false;

    private String username;

    private String password;

    /**
     * Was the original caller asking for an entirely new account?
     */
    protected boolean requestNewAccount = false;

    public static final int REQUEST_CODE_REGISTER_USER = 1111;
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 2222;
    private CallbackManager callbackManager;
    private GoogleApiClient googleApiClient;
    private InstituteSettingsDao instituteSettingsDao;
    private InstituteSettings instituteSettings;

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

        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }

        setContentView(layout.login_activity);

        ButterKnife.inject(this);
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);
        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_SEND && signInButton.isEnabled()) {
                    signIn();
                    return true;
                }
                return false;
            }
        });

        usernameText.addTextChangedListener(watcher);
        usernameText.setSingleLine();
        passwordText.addTextChangedListener(watcher);
        passwordText.setTypeface(Typeface.DEFAULT);
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.invalidate();
        fbLoginButton.setReadPermissions("email");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginLayout.setVisibility(View.GONE);
                username = loginResult.getAccessToken().getUserId();
                authenticate(loginResult.getAccessToken().getUserId(),
                        loginResult.getAccessToken().getToken(), TestpressSdk.Provider.FACEBOOK);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                if (error.getMessage().contains("CONNECTION_FAILURE")) {
                    showAlert(getString(R.string.no_internet_try_again));
                } else {
                    Log.e("Facebook sign in error", "check hashes");
                    showAlert(getString(R.string.something_went_wrong_please_try_after));
                }
            }
        });
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (connectionResult.getErrorMessage() != null) {
                            showAlert(connectionResult.getErrorMessage());
                        } else {
                            showAlert(connectionResult.toString());
                        }
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        orLabel.setTypeface(TestpressSdk.getRubikMediumFont(this));
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        instituteSettingsDao = daoSession.getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list();

        if (instituteSettingsList.size() == 0) {
            getInstituteSettings();
        } else {
            instituteSettings = instituteSettingsList.get(0);
            updateInstituteSpecificFields();
        }
    }

    private void getInstituteSettings() {
        progressBar.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
        new SafeAsyncTask<InstituteSettings>() {
            @Override
            public InstituteSettings call() throws Exception {
                return testpressService.getInstituteSettings();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
                progressBar.setVisibility(View.GONE);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        getInstituteSettings();
                    }
                });
            }

            @Override
            protected void onSuccess(InstituteSettings instituteSettings) throws Exception {
                instituteSettings.setBaseUrl(BASE_URL);
                instituteSettingsDao.insertOrReplace(instituteSettings);
                LoginActivity.this.instituteSettings = instituteSettings;
                updateInstituteSpecificFields();
                loginLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void authenticate(final String userId, String accessToken,
                              final TestpressSdk.Provider provider) {

        in.testpress.models.InstituteSettings settings =
                new in.testpress.models.InstituteSettings(instituteSettings.getBaseUrl())
                        .setBookmarksEnabled(instituteSettings.getBookmarksEnabled())
                        .setCoursesFrontend(instituteSettings.getShowGameFrontend())
                        .setCoursesGamificationEnabled(instituteSettings.getCoursesEnableGamification())
                        .setCommentsVotingEnabled(instituteSettings.getCommentsVotingEnabled())
                        .setAccessCodeEnabled(false);

        TestpressSdk.initialize(this, settings, userId, accessToken, provider,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
                        if (provider == TestpressSdk.Provider.FACEBOOK &&
                                Profile.getCurrentProfile() != null) {
                            username = Profile.getCurrentProfile().getName();
                        }
                        authToken = response.getToken();
                        testpressService.setAuthToken(authToken);
                        onAuthenticationResult(true);
                    }

                    @Override
                    public void onException(TestpressException e) {
                        loginLayout.setVisibility(View.VISIBLE);
                        if (e.isNetworkError()) {
                            showAlert(getString(R.string.no_internet_try_again));
                        } else if (e.isClientError()) {
                            showAlert(getString(R.string.invalid_username_or_password));
                        } else {
                            showAlert(getString(R.string.testpress_some_thing_went_wrong_try_again));
                        }
                    }
                });
    }

    private void updateInstituteSpecificFields() {
        ViewUtils.setGone(fbLoginButton, !instituteSettings.getFacebookLoginEnabled());
        ViewUtils.setGone(googleLoginButton, !instituteSettings.getGoogleLoginEnabled());
        ViewUtils.setGone(socialLoginLayout, !instituteSettings.getFacebookLoginEnabled() &&
                !instituteSettings.getGoogleLoginEnabled());
        ViewUtils.setGone(signUpButton, !instituteSettings.getAllowSignup());
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

        if (requestNewAccount) {
            username = usernameText.getText().toString();
        }

        password = passwordText.getText().toString();

        authenticate(username, password, TestpressSdk.Provider.TESTPRESS);
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     * @param result
     */
    protected void finishConfirmCredentials(final boolean result) {
        final Account account = new Account(username, APPLICATION_ID);
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
        SharedPreferences sharedPreferences =
                getSharedPreferences(Constants.GCM_PREFERENCE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(GCMPreference.SENT_TOKEN_TO_SERVER, false).apply();
        CommonUtils.registerDevice(this, testpressService);
        final Account account = new Account(username, APPLICATION_ID);

        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
            accountManager.setAuthToken(account, APPLICATION_ID, authToken);
        } else {
            accountManager.setPassword(account, password);
        }
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        PostDao postDao = daoSession.getPostDao();
        postDao.deleteAll();
        daoSession.clear();
        if (authTokenType != null && authTokenType.equals(APPLICATION_ID)) {
            final Intent intent = new Intent();
            intent.putExtra(KEY_ACCOUNT_NAME, username);
            intent.putExtra(KEY_ACCOUNT_TYPE, APPLICATION_ID);
            intent.putExtra(KEY_AUTHTOKEN, authToken);
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            if (getIntent().getStringExtra(Constants.DEEP_LINK_TO) != null) {
                switch (getIntent().getStringExtra(Constants.DEEP_LINK_TO)) {
                    case Constants.DEEP_LINK_TO_POST:
                        intent = new Intent(this, PostActivity.class);
                        intent.putExtra(Constants.IS_DEEP_LINK, true);
                        intent.putExtras(getIntent().getExtras());
                        break;
                    default:
                        intent = new Intent(this, MainActivity.class);
                        break;
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
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
                .buttonsGravity(GravityEnum.END)
                .show();
    }

    @OnClick(id.b_signin) public void signIn() {
        if(internetConnectivityChecker.isConnected()) {
            handleLogin(signInButton);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @OnClick(id.signup) public void signUp() {
        if(internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            if(getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivityForResult(intent, REQUEST_CODE_REGISTER_USER);
        } else {
            internetConnectivityChecker.showAlert();
        }

    }

    @OnClick(id.forgot_password) public void verify() {
        if(internetConnectivityChecker.isConnected()) {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    @OnClick(id.google_sign_in_button) public void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        } else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                //noinspection ConstantConditions
                if (result.getSignInAccount().getGivenName() == null ||
                        result.getSignInAccount().getGivenName().isEmpty()) {
                    username = result.getSignInAccount().getGivenName();
                } else {
                    username = result.getSignInAccount().getEmail();
                }
                authenticate(result.getSignInAccount().getId(), result.getSignInAccount().getIdToken(),
                        TestpressSdk.Provider.GOOGLE);
            } else if (result.getStatus().getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
                showAlert(getString(R.string.no_internet_try_again));
            } else if (result.getStatus().getStatusCode() == CommonStatusCodes.DEVELOPER_ERROR) {
                showAlert(getString(R.string.google_sign_in_wrong_hash));
            } else if (result.getStatus().getStatusCode() == 12501) {
                Log.e("Google sign in error", "Might be wrong app certificate SHA1");
                showAlert(getString(R.string.something_went_wrong_please_try_after));
            } else {
                Log.e("Google sign in error", result.getStatus().toString());
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

}

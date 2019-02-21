package in.testpress.testpress.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.phone.SmsRetriever;


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
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.SmsReceivingEvent;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.Device;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.PostDao;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.RegistrationErrorDetails;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.GCMPreference;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.SafeAsyncTask;
import retrofit.RetrofitError;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;

public class CodeVerificationActivity extends AppCompatActivity {
    @Inject TestpressService testpressService;
    @InjectView(R.id.welcome) TextView welcomeText;
    @InjectView(R.id.et_username) EditText usernameText;
    @InjectView(R.id.et_verificationCode) EditText verificationCodeText;
    @InjectView(R.id.b_verify) Button verifyButton;
    @InjectView(R.id.progressbar) ProgressBar progressBar;
    @InjectView(R.id.count) TextView countText;
    @InjectView(R.id.sms_receiving_layout) LinearLayout smsReceivingLayout;

    private String username;
    private String password;
    private String authToken;
    private AccountManager accountManager;
    private RegistrationSuccessResponse codeResponse;
    private final TextWatcher watcher = validationTextWatcher();
    private MaterialDialog progressDialog;
    private Context context=this;
    private SmsReceivingEvent smsReceivingEvent;
    private Timer timer;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
    private PackageManager packageManager;
    private InstituteSettingsDao instituteSettingsDao;
    private InstituteSettings instituteSettings;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.code_verify_activity);
        ButterKnife.inject(this);
        final Intent intent = getIntent();
        fetchInstituteSettingLocalDB();
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        if(username == null){
            usernameText.setVisibility(View.VISIBLE);
            verificationCodeText.setVisibility(View.VISIBLE);
            verifyButton.setVisibility(View.VISIBLE);
            smsReceivingLayout.setVisibility(View.GONE);
            usernameText.addTextChangedListener(watcher);
        } else {
            welcomeText.setText("Waiting to automatically detect an sms sent to " + phoneNumber + "\nIf you get the verification code press Manually Verify");
            timer = new Timer();
            smsReceivingEvent = new SmsReceivingEvent(timer);
            IntentFilter filter = new IntentFilter();
            filter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            registerReceiver(smsReceivingEvent, filter); //Register SMS broadcast receiver
            timer.start(); // Start timer
        }
        verificationCodeText.addTextChangedListener(watcher);
        verificationCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && verifyButton.isEnabled()) {
                    verify();
                    return true;
                }
                return false;
            }
        });
        accountManager = AccountManager.get(this);
    }

    @OnClick(R.id.b_verify) public void verify() {
        if(internetConnectivityChecker.isConnected()) {
            if (username == null) {
                username = usernameText.getText().toString().trim();
            }
            handleCodeVerification();
        } else {
            internetConnectivityChecker.showAlert();
        }
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated;
        if(username == null) {
            populated = populated(verificationCodeText) && populated(usernameText);
        } else {
            populated = populated(verificationCodeText);
        }
        verifyButton.setEnabled(populated);
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    // CountDownTimer class
    public class Timer extends CountDownTimer {
        public Timer() {
            super(30000, 1000); //super(startTime, interval);
        }

        @Override
        public void onFinish() {
            countText.setText("30s");
            progressBar.setProgress(30);
            unregisterReceiver(smsReceivingEvent); //end receiver

            if (smsReceivingEvent.code  != null) { //checking smsReceivingEvent get the code or not
                verificationCodeText.setText(smsReceivingEvent.code);
                handleCodeVerification(); //verify code
            } else {
                verificationCodeText.setVisibility(View.VISIBLE); //user have to enter code
                verifyButton.setVisibility(View.VISIBLE);
                smsReceivingLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            countText.setText((int)(millisUntilFinished / 1000) + "s");
            progressBar.setProgress(30 - (int)(millisUntilFinished / 1000));
        }
    }

    @OnClick(R.id.b_manually_verify) public void manuallyVerify() { //user have to enter code
        timer.cancel();
        timer.onFinish();
    }

    // verify the verification code
    private void handleCodeVerification(){
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.message_verifying)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0).show();
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                codeResponse = testpressService.verifyCode(username,verificationCodeText.getText().toString().trim());
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                progressDialog.dismiss();
                // Retrofit Errors are handled
                if((e instanceof RetrofitError)) {
                    RegistrationErrorDetails registrationErrorDetails = (RegistrationErrorDetails)((RetrofitError) e).getBodyAs(RegistrationErrorDetails.class);
                    if(!registrationErrorDetails.getNonFieldErrors().isEmpty()) {
                        verificationCodeText.setError(registrationErrorDetails.getNonFieldErrors().get(0));
                        verificationCodeText.requestFocus();
                    }
                }
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {  //Successfully Verified
                setResult(RESULT_OK);
                if(password == null){
                    gotoLoginScreen();
                } else {
                    autoLogin();
                }
            }
        }.execute();
    }

    // check password & get authKey
    private void autoLogin() {
        in.testpress.models.InstituteSettings settings =
                new in.testpress.models.InstituteSettings(instituteSettings.getBaseUrl())
                        .setBookmarksEnabled(instituteSettings.getBookmarksEnabled())
                        .setCoursesFrontend(instituteSettings.getShowGameFrontend())
                        .setCoursesGamificationEnabled(instituteSettings.getCoursesEnableGamification())
                        .setCommentsVotingEnabled(instituteSettings.getCommentsVotingEnabled()).setAccessCodeEnabled(false);

        TestpressSdk.initialize(this, settings, username, password, TestpressSdk.Provider.TESTPRESS,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
                        //add account in mobile
                        authToken = response.getToken();
                        testpressService.setAuthToken(authToken);
                        final Account account = new Account(username, APPLICATION_ID);
                        accountManager.addAccountExplicitly(account, password, null);
                        accountManager.setAuthToken(account, APPLICATION_ID, authToken);
                        updateDevice();
                        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
                        PostDao postDao = daoSession.getPostDao();
                        postDao.deleteAll();
                        daoSession.clear();
                        Intent intent;
                        switch (getIntent().getExtras().getString(Constants.DEEP_LINK_TO, "")) {
                            case Constants.DEEP_LINK_TO_POST:
                                intent = new Intent(CodeVerificationActivity.this, PostActivity.class);
                                intent.putExtra(Constants.IS_DEEP_LINK, true);
                                intent.putExtras(getIntent().getExtras());
                                break;
                            default:
                                intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                                break;
                        }
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onException(TestpressException e) {
                        gotoLoginScreen();
                    }
                });
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

    private void gotoLoginScreen(){
        new MaterialDialog.Builder(context)
                .title("Code successfully verified")
                .content("Please login to continue")
                .neutralText(R.string.ok)
                .neutralColorRes(R.color.primary)
                .buttonsGravity(GravityEnum.CENTER)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class); //call main activity, it will show login screen
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if(username == null) { //onBackPressed go to login screen only if username is null
            Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void fetchInstituteSettingLocalDB() {
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        instituteSettingsDao = daoSession.getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list();
        if (instituteSettingsList.size() == 0) {
            getInstituteSettings();
        } else {
            instituteSettings = instituteSettingsList.get(0);
        }
    }

    private void getInstituteSettings() {
        progressBar.setVisibility(View.VISIBLE);
        new SafeAsyncTask<InstituteSettings>() {
            @Override
            public InstituteSettings call() throws Exception {
                return testpressService.getInstituteSettings();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                if (exception.getCause() instanceof IOException) {
                    internetConnectivityChecker.showAlert();
                } else {
                    internetConnectivityChecker.showAlert();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            protected void onSuccess(InstituteSettings instituteSettings) throws Exception {
                instituteSettings.setBaseUrl(BASE_URL);
                instituteSettingsDao.insertOrReplace(instituteSettings);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

}

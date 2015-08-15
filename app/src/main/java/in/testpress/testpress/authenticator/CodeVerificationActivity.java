package in.testpress.testpress.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.events.SmsReceivingEvent;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.RegistrationErrorDetails;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.SafeAsyncTask;
import retrofit.RetrofitError;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class CodeVerificationActivity extends Activity {
    @Inject TestpressService testpressService;
    @InjectView(R.id.welcome) TextView welcomeText;
    @InjectView(R.id.et_username) EditText usernameText;
    @InjectView(R.id.et_password) EditText passwordText;
    @InjectView(R.id.et_verificationCode) EditText verificationCodeText;
    @InjectView(R.id.b_verify) Button verifyButton;

    private String username;
    private String password;
    private String authToken;
    private AccountManager accountManager;
    private RegistrationSuccessResponse codeResponse;
    private final TextWatcher watcher = validationTextWatcher();
    private SmsReceivingEvent smsReceivingEvent;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.code_verify_activity);
        ButterKnife.inject(this);
        final Intent intent = getIntent();
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        if(username == null){
            usernameText.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.GONE);
            usernameText.addTextChangedListener(watcher);
            passwordText.addTextChangedListener(watcher);
        } else {
            showProgress(0);
            welcomeText.setText("Welcome " + username);
            Timer timer = new Timer();
            smsReceivingEvent = new SmsReceivingEvent(timer);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsReceivingEvent, filter); //start receiver
            timer.start(); //start timer
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
        if(username == null){
            username = usernameText.getText().toString().trim();
            password = passwordText.getText().toString().trim();
        }
        handleCodeVerification();
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
            populated = populated(verificationCodeText) && populated(usernameText) && populated(passwordText);
        } else {
            populated = populated(verificationCodeText);
        }
        verifyButton.setEnabled(populated);
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    /**
     * Hide progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void hideProgress(int id) {
        dismissDialog(id);
    }

    /**
     * Show progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void showProgress(int id) {
        showDialog(id);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        if (id == 0) {
            dialog.setTitle(getText(R.string.message_receiving_code)); //Progress for timer
        } else {
            dialog.setTitle(getText(R.string.message_verifying)); //Progress title for handleCodeVerification
        }
        dialog.setMessage("Please Wait..");
        dialog.setIndeterminate(true);
        return dialog;
    }

    public void showAlert(String alertMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CodeVerificationActivity.this);
        builder.setMessage(alertMessage);
        builder.setCancelable(false);
        builder.setNeutralButton("ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                        startActivity(intent);
                        dialog.cancel();
                        dialog.dismiss();
                        finish();
                    }
                });
        AlertDialog alert = builder.show();
        TextView messageView = (TextView)alert.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }

    // CountDownTimer class
    public class Timer extends CountDownTimer
    {

        public Timer()
        {
            super(20000, 1000); //super(startTime, interval);
        }

        @Override
        public void onFinish()
        {
            //end receiver
            unregisterReceiver(smsReceivingEvent);
            if (smsReceivingEvent.code  != null) { //checking smsReceivingEvent get the code or not
                verificationCodeText.setText(smsReceivingEvent.code);
                handleCodeVerification(); //verify code
            } else {
                hideProgress(0); //user have to enter code
            }
        }

        @Override
        public void onTick(long millisUntilFinished)
        {
            //empty implementation because CountDownTimer is abstract
        }
    }

    // verify the verification code
    private void handleCodeVerification(){
        showProgress(1);
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                codeResponse = testpressService.verifyCode(username,verificationCodeText.getText().toString().trim());
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                hideProgress(1);
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
                login();
            }
        }.execute();
    }

    // check password & get authKey
    private void login(){
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                authToken = testpressService.authenticate(username, password);
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                showAlert("Code successfully verified\n*Invalid password login again");//call main activity, it will show login screen
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                //add account in mobile
                final Account account = new Account(username, Constants.Auth.TESTPRESS_ACCOUNT_TYPE);
                accountManager.addAccountExplicitly(account, password, null);
                accountManager.setAuthToken(account, Constants.Auth.TESTPRESS_ACCOUNT_TYPE, authToken);
                //call main activity, it will simply go to available exams
                Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if(username == null) { //onBackPressed go to login screen only if username is null
            Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

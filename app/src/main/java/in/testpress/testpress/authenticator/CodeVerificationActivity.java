package in.testpress.testpress.authenticator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import in.testpress.testpress.core.TestpressService;
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
    @InjectView(R.id.et_verificationCode) EditText verificationCodeText;
    @InjectView(R.id.b_verify) Button verifyButton;

    private String username;
    private String password;
    private boolean unknownUser;
    private RegistrationSuccessResponse codeResponse;
    private final TextWatcher watcher = validationTextWatcher();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.code_verify_activity);
        ButterKnife.inject(this);
        final Intent intent = getIntent();
        username=intent.getStringExtra("username");
        password=intent.getStringExtra("password");
        unknownUser=username==null;
        if(unknownUser) {
            usernameText.setVisibility(View.VISIBLE);
        }
        else
            welcomeText.setText("Welcome "+username);

        usernameText.addTextChangedListener(watcher);
        verificationCodeText.addTextChangedListener(watcher);
        verificationCodeText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && verifyButton.isEnabled()) {
                    handleCodeVerification();
                    return true;
                }
                return false;
            }
        });
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable gitDirEditText) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated;
        if(unknownUser)
            populated = populated(verificationCodeText) && populated(usernameText);
        else
            populated = populated(verificationCodeText);
            verifyButton.setEnabled(populated);;

    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    /**
     * Hide progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void hideProgress() {
        dismissDialog(0);
    }

    /**
     * Show progress dialog
     */
    @SuppressWarnings("deprecation")
    protected void showProgress() {
        showDialog(0);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.message_verifying));
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
                        finish();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.show();
        TextView messageView = (TextView)alert.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }

    private void handleCodeVerification(){
        SafeAsyncTask<Boolean> authenticationTask;
        showProgress();
        authenticationTask = new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                if(unknownUser)
                    codeResponse = testpressService.verifyCode(usernameText.getText().toString().trim(),verificationCodeText.getText().toString().trim());//get username from editText
                else
                    codeResponse = testpressService.verifyCode(username,verificationCodeText.getText().toString().trim());//get username from previous activity
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                // Retrofit Errors are handled inside of the {
                if((e instanceof RetrofitError)) {
                    RegistrationErrorDetails registrationErrorDetails=(RegistrationErrorDetails)((RetrofitError) e).getBodyAs(RegistrationErrorDetails.class);
                    if(!registrationErrorDetails.getNonFieldErrors().isEmpty()) {
                        verificationCodeText.setError(registrationErrorDetails.getNonFieldErrors().get(0));
                        verificationCodeText.requestFocus();
                    }
                }
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {  //Successfully Verified
                hideProgress();
                if(!unknownUser) { //Automatic signIn
                    TestpressAuthenticatorActivity.username = username;
                    TestpressAuthenticatorActivity.password = password;
                    TestpressAuthenticatorActivity.requestNewAccount = true;
                    Intent intent = new Intent(CodeVerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else
                    showAlert("Successfully Verified Please Sign In"); //Need manual signIn
            }

            @Override
            protected void onFinally() throws RuntimeException {
                hideProgress();
            }
        };
        authenticationTask.execute();
    }

    @OnClick(R.id.b_verify) public void verify() {
        handleCodeVerification();
    }
}

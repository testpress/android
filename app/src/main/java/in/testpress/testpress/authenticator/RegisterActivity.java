package in.testpress.testpress.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.RegistrationErrorDetails;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.SafeAsyncTask;
import retrofit.RetrofitError;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class RegisterActivity extends AppCompatActivity {
    @Inject TestpressService testpressService;
    @InjectView(id.et_username) EditText usernameText;
    @InjectView(id.et_password) EditText passwordText;
    @InjectView(id.et_password_confirm) EditText confirmPasswordText;
    @InjectView(id.et_email) EditText emailText;
    @InjectView(id.et_phone) EditText phoneText;
    @InjectView(id.tv_fill_all_details) TextView fillAllDetailsText;
    @InjectView(id.b_register) Button registerButton;
    private final TextWatcher watcher = validationTextWatcher();
    private RegistrationSuccessResponse registrationSuccessResponse;
    private MaterialDialog progressDialog;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.register_activity);
        ButterKnife.inject(this);
        confirmPasswordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(final TextView v, final int actionId,
                                          final KeyEvent event) {
                if (actionId == IME_ACTION_DONE && registerButton.isEnabled()) {
                    register();
                    return true;
                }
                return false;
            }
        });
        usernameText.addTextChangedListener(watcher);
        passwordText.addTextChangedListener(watcher);
        emailText.addTextChangedListener(watcher);
        phoneText.addTextChangedListener(watcher);
        confirmPasswordText.addTextChangedListener(watcher);
    }

    void postDetails(){
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .show();
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                registrationSuccessResponse = testpressService.register(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString(), phoneText.getText().toString());
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                // Retrofit Errors are handled
                if((e instanceof RetrofitError)) {
                    RegistrationErrorDetails registrationErrorDetails = (RegistrationErrorDetails)((RetrofitError) e).getBodyAs(RegistrationErrorDetails.class);
                    if(!registrationErrorDetails.getUsername().isEmpty()) {
                        usernameText.setError(registrationErrorDetails.getUsername().get(0));
                        usernameText.requestFocus();
                    }
                    if(!registrationErrorDetails.getEmail().isEmpty()) {
                        emailText.setError(registrationErrorDetails.getEmail().get(0));
                        emailText.requestFocus();
                    }
                    if(!registrationErrorDetails.getPassword().isEmpty()) {
                        passwordText.setError(registrationErrorDetails.getPassword().get(0));
                        passwordText.requestFocus();
                    }
                    if(!registrationErrorDetails.getPhone().isEmpty()) {
                        phoneText.setError(registrationErrorDetails.getPhone().get(0));
                        phoneText.requestFocus();
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                progressDialog.dismiss();
                Intent intent = new Intent(RegisterActivity.this, CodeVerificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Bundle bundle = new Bundle();
                bundle.putString("username", registrationSuccessResponse.getUsername());
                bundle.putString("password", registrationSuccessResponse.getPassword());
                bundle.putString("phoneNumber", registrationSuccessResponse.getPhone());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        }.execute();
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                updateUIWithValidation();
            }
        };
    }

    private void updateUIWithValidation() {
        final boolean populated = populated(usernameText) && populated(passwordText)&&populated(emailText)&&populated(phoneText)&&populated(confirmPasswordText);
        if(populated) {
            registerButton.setEnabled(true);
            fillAllDetailsText.setVisibility(View.GONE);
        } else {
            registerButton.setEnabled(false);
            fillAllDetailsText.setVisibility(View.VISIBLE);
        }
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    private boolean validate(){
           //Username verification
           Pattern userNamePattern = Pattern.compile("[a-z0-9]*");
           Matcher userNameMatcher = userNamePattern.matcher(usernameText.getText().toString().trim());
           if(!userNameMatcher.matches()) {
               usernameText.setError("This field can contain only lowercase alphabets and numbers.");
               usernameText.requestFocus();
               return false;
           }
           //Email verification
           if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString().trim()).matches()) {
               emailText.setError("Please enter a valid email address");
               emailText.requestFocus();
               return false;
           }
           //Phone number verification
           Pattern phoneNumberPattern = Pattern.compile("[789]\\d{9}");
           Matcher phoneNumberMatcher = phoneNumberPattern.matcher(phoneText.getText().toString().trim());
           if(!phoneNumberMatcher.matches()) {
               phoneText.setError("Please enter 10 digit valid mobile number");
               phoneText.requestFocus();
               return false;
           }
           //Password verification
           if(passwordText.getText().toString().trim().length()<6){
               passwordText.setError("Password should contain at least 6 digits");
               passwordText.requestFocus();
               return false;
           }
           //ConfirmPassword verification
           if(!passwordText.getText().toString().equals(confirmPasswordText.getText().toString().trim())){
               confirmPasswordText.setError("Passwords not matching");
               confirmPasswordText.requestFocus();
               return false;
           }
           return true;
    }

    @OnClick(id.b_register) public void register() {
        if(validate()) {
        new MaterialDialog.Builder(this)
                .title("Verify phone")
                .content("We will verify the phone number\n\n" + phoneText.getText().toString() + "\n\nIs this OK, or would you like to edit the number?")
                .positiveText(R.string.ok)
                .negativeText(R.string.edit)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .buttonsGravity(GravityEnum.CENTER)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if(internetConnectivityChecker.isConnected()) {
                            postDetails();
                        } else {
                            internetConnectivityChecker.showAlert();
                        }
                    }
                })
                .show();
        }
    }
}

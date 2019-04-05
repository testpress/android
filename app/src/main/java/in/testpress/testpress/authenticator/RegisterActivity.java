package in.testpress.testpress.authenticator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hbb20.CountryCodePicker;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.R.id;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import in.testpress.testpress.models.RegistrationSuccessResponse;
import in.testpress.testpress.models.RegistrationErrorDetails;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.PhoneNumberValidator;
import retrofit.RetrofitError;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.authenticator.LoginActivity.REQUEST_CODE_REGISTER_USER;
import static in.testpress.testpress.authenticator.RegisterActivity.VerificationMethod.EMAIL;
import static in.testpress.testpress.authenticator.RegisterActivity.VerificationMethod.MOBILE;

public class RegisterActivity extends AppCompatActivity {

    @Inject TestpressService testpressService;
    @InjectView(id.et_username) EditText usernameText;
    @InjectView(id.et_password) EditText passwordText;
    @InjectView(id.et_password_confirm) EditText confirmPasswordText;
    @InjectView(id.et_email) EditText emailText;
    @InjectView(id.et_phone) EditText phoneText;
    @InjectView(id.ccp) CountryCodePicker countryCodePicker;
    @InjectView(id.phone_layout) TextInputLayout phoneLayout;
    @InjectView(id.tv_fill_all_details) TextView fillAllDetailsText;
    @InjectView(id.b_register) Button registerButton;
    @InjectView(id.register_layout) LinearLayout registerLayout;
    @InjectView(R.id.success_complete) LinearLayout successContainer;
    @InjectView(R.id.success_description) TextView successDescription;

    private final TextWatcher watcher = validationTextWatcher();
    private RegistrationSuccessResponse registrationSuccessResponse;
    private MaterialDialog progressDialog;
    private InternetConnectivityChecker internetConnectivityChecker = new InternetConnectivityChecker(this);
    private boolean isTwilioEnabled;
    private VerificationMethod verificationMethod;
    enum VerificationMethod { MOBILE, EMAIL }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Injector.inject(this);
        setContentView(R.layout.register_activity);
        ButterKnife.inject(this);
        DaoSession daoSession = ((TestpressApplication) getApplicationContext()).getDaoSession();
        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingsList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list();

        if (instituteSettingsList.size() != 0) {
            InstituteSettings instituteSettings = instituteSettingsList.get(0);
            verificationMethod =
                    instituteSettings.getVerificationMethod().equals("M") ? MOBILE : EMAIL;
            isTwilioEnabled = instituteSettings.getTwilioEnabled();
        } else {
            // Never happen, just for a safety.
            finish();
        }

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
        if (verificationMethod.equals(MOBILE)) {
            phoneText.addTextChangedListener(watcher);
            phoneLayout.setVisibility(View.VISIBLE);

            if (!isTwilioEnabled) {
                countryCodePicker.setVisibility(View.GONE);
            }
        } else {
            phoneLayout.setVisibility(View.GONE);
            countryCodePicker.setVisibility(View.GONE);
            isTwilioEnabled=false;
        }
        confirmPasswordText.addTextChangedListener(watcher);

        if(isTwilioEnabled) {
            countryCodePicker.registerCarrierNumberEditText(phoneText);
            countryCodePicker.setNumberAutoFormattingEnabled(false);
        }
    }

    void postDetails(){
        registerButton.setEnabled(false);
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .content(R.string.please_wait)
                .widgetColorRes(R.color.primary)
                .progress(true, 0)
                .cancelable(false)
                .show();
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {

                if(isTwilioEnabled){
                    registrationSuccessResponse = testpressService.register(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString(), phoneText.getText().toString(), countryCodePicker.getSelectedCountryNameCode());
                } else {
                    registrationSuccessResponse = testpressService.register(usernameText.getText().toString(), emailText.getText().toString(), passwordText.getText().toString(), phoneText.getText().toString(), "");
                }
                return true;
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                registerButton.setEnabled(true);
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
                if (verificationMethod.equals(MOBILE)) {
                    Intent intent = new Intent(RegisterActivity.this, CodeVerificationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("username", registrationSuccessResponse.getUsername());
                    intent.putExtra("password", registrationSuccessResponse.getPassword());
                    intent.putExtra("phoneNumber", registrationSuccessResponse.getPhone());
                    intent.putExtras(getIntent().getExtras());
                    startActivityForResult(intent, REQUEST_CODE_REGISTER_USER);
                } else {
                    registerLayout.setVisibility(View.GONE);
                    successDescription.setText(R.string.activation_email_sent_message);
                    successContainer.setVisibility(View.VISIBLE);
                }
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
        final boolean populated = populated(usernameText) && populated(passwordText) &&
                populated(emailText) && populated(confirmPasswordText) &&
                (populated(phoneText) || verificationMethod.equals(EMAIL));
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
            if (verificationMethod.equals(MOBILE)) {
                //Phone number verification
                boolean isPhoneNumberValid;

                if (isTwilioEnabled) {
                    isPhoneNumberValid = PhoneNumberValidator.validateInternationalPhoneNumber(countryCodePicker);
                } else {
                    isPhoneNumberValid = PhoneNumberValidator.validatePhoneNumber(phoneText.getText().toString().trim());
                }

                if (!isPhoneNumberValid) {
                    phoneText.setError("Please enter a valid mobile number");
                    phoneText.requestFocus();
                    return false;
                }
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
            if (verificationMethod.equals(MOBILE)) {
                new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                        .setTitle("Verify phone")
                        .setMessage("We will verify the phone number\n\n" + phoneText.getText().toString() +
                                "\n\nIs this OK, or would you like to edit the number?")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                if (internetConnectivityChecker.isConnected()) {
                                    initiateSmsRetrieverClient();
                                } else {
                                    internetConnectivityChecker.showAlert();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.edit, null)
                        .show();
            } else {
                postDetails();
            }
        }
    }
    public void initiateSmsRetrieverClient() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        Task<Void> task = client.startSmsRetriever();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // successfully started an SMS Retriever for one SMS message
                postDetails();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                // user have to manually enter the code
                postDetails();
            }
        });
    }

    @OnClick(R.id.success_ok) public void verificationMailSent() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_REGISTER_USER) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

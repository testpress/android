package in.testpress.testpress.authenticator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;
import java.util.HashMap;
import java.util.Hashtable;
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
import in.testpress.testpress.util.InternetConnectivityChecker;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.testpress.util.Validator;
import in.testpress.util.EventsTrackerFacade;
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
    @InjectView(id.phone_layout)
    TextInputLayout phoneLayout;
    @InjectView(id.b_register) Button registerButton;
    @InjectView(id.register_layout) LinearLayout registerLayout;
    @InjectView(R.id.success_complete) LinearLayout successContainer;
    @InjectView(R.id.success_description) TextView successDescription;
    @InjectView(id.username_error) TextView usernameError;
    @InjectView(id.email_error) TextView emailError;
    @InjectView(id.password_error) TextView passwordError;
    @InjectView(id.confirm_password_error) TextView confirmPasswordError;
    @InjectView(id.phone_error) TextView phoneError;

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
        if (verificationMethod.equals(MOBILE)) {
            phoneLayout.setVisibility(View.VISIBLE);

            if (!isTwilioEnabled) {
                countryCodePicker.setVisibility(View.GONE);
            }
        } else {
            phoneLayout.setVisibility(View.GONE);
            countryCodePicker.setVisibility(View.GONE);
            isTwilioEnabled=false;
        }

        if(isTwilioEnabled) {
            countryCodePicker.registerCarrierNumberEditText(phoneText);
            countryCodePicker.setNumberAutoFormattingEnabled(false);
        }
        setTextWatchers();
        
    }
    
    void setTextWatchers() {
        Hashtable<EditText, TextView> editTextMap = new Hashtable<>();
        editTextMap.put(usernameText, usernameError);
        editTextMap.put(passwordText, passwordError);
        editTextMap.put(confirmPasswordText, confirmPasswordError);
        editTextMap.put(emailText, emailError);
        editTextMap.put(phoneText, phoneError);

        for(EditText editText: editTextMap.keySet()) {
            hideErrorMessageOnTextChange(editText, editTextMap.get(editText));
        }
    }

    void hideErrorMessageOnTextChange(EditText editText, final TextView errorText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                errorText.setVisibility(View.GONE);
            }
        });
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
                        usernameError.setVisibility(View.VISIBLE);
                        usernameError.setText(registrationErrorDetails.getUsername().get(0));
                        usernameText.requestFocus();
                    }
                    if(!registrationErrorDetails.getEmail().isEmpty()) {
                        emailError.setVisibility(View.VISIBLE);
                        emailError.setText(registrationErrorDetails.getEmail().get(0));
                        emailText.requestFocus();
                    }
                    if(!registrationErrorDetails.getPassword().isEmpty()) {
                        passwordError.setVisibility(View.VISIBLE);
                        passwordError.setText(registrationErrorDetails.getPassword().get(0));
                        passwordText.requestFocus();
                    }
                    if(!registrationErrorDetails.getPhone().isEmpty()) {
                        phoneError.setVisibility(View.VISIBLE);
                        phoneError.setText(registrationErrorDetails.getPhone().get(0));
                        phoneText.requestFocus();
                    }
                }
                progressDialog.dismiss();
            }

            private void logEvent() {
                EventsTrackerFacade eventsTrackerFacade = new EventsTrackerFacade(getApplicationContext());
                HashMap<String, Object> params = new HashMap<>();
                params.put("username", registrationSuccessResponse.getUsername());
                eventsTrackerFacade.logEvent(EventsTrackerFacade.ACCOUNT_REGISTERED, params);
            }

            @Override
            public void onSuccess(final Boolean authSuccess) {
                progressDialog.dismiss();
                logEvent();
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


    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    private boolean validate(){
        boolean isValid = true;

        if (!populated(passwordText)) {
            passwordError.setVisibility(View.VISIBLE);
            passwordError.setText(getString(R.string.empty_input_error));
            isValid = false;
        }
        if (!populated(confirmPasswordText)) {
            confirmPasswordError.setVisibility(View.VISIBLE);
            confirmPasswordError.setText(getString(R.string.empty_input_error));
            isValid = false;
        }
        if (!populated(emailText)) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText(getString(R.string.empty_input_error));
            isValid = false;
        }
        if (!populated(usernameText)) {
            usernameError.setVisibility(View.VISIBLE);
            usernameError.setText(getString(R.string.empty_input_error));
            isValid = false;
        }

        if ((!populated(phoneText) && !verificationMethod.equals(EMAIL))) {
            phoneError.setVisibility(View.VISIBLE);
            phoneError.setText(getString(R.string.empty_input_error));
            isValid = false;
        }

           //Username verification
           Pattern userNamePattern = Pattern.compile("[a-z0-9]*");
           Matcher userNameMatcher = userNamePattern.matcher(usernameText.getText().toString().trim());
           if(populated(usernameText) && !userNameMatcher.matches()) {
               usernameError.setVisibility(View.VISIBLE);
               usernameError.setText(getString(R.string.username_error));
               usernameText.requestFocus();
               isValid = false;
           }
           //Email verification
           if(populated(emailText) && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString().trim()).matches()) {
               emailError.setVisibility(View.VISIBLE);
               emailError.setText(getString(R.string.email_error));
               emailText.requestFocus();
               isValid = false;
           }
            if (verificationMethod.equals(MOBILE)) {
                //Phone number verification
                boolean isPhoneNumberValid;

                if (isTwilioEnabled) {
                    isPhoneNumberValid = Validator.validateInternationalPhoneNumber(countryCodePicker);
                } else {
                    isPhoneNumberValid = Validator.validatePhoneNumber(phoneText.getText().toString().trim());
                }

                if (populated(phoneText) && !isPhoneNumberValid) {
                    phoneError.setVisibility(View.VISIBLE);
                    phoneError.setText(getString(R.string.phone_number_error));
                    phoneText.requestFocus();
                    isValid = false;
                }
            }
           //Password verification
           if(populated(passwordText) && passwordText.getText().toString().trim().length()<6){
               passwordError.setVisibility(View.VISIBLE);
               passwordError.setText(getString(R.string.password_error));
               passwordText.requestFocus();
               isValid = false;
           }
           //ConfirmPassword verification
           if(populated(confirmPasswordText) && !passwordText.getText().toString().equals(confirmPasswordText.getText().toString().trim())){
               confirmPasswordError.setVisibility(View.VISIBLE);
               confirmPasswordError.setText(getString(R.string.password_mismatch_error));
               confirmPasswordText.requestFocus();
               isValid = false;
           }
           return isValid;
    }

    @OnClick(id.b_register) public void register() {

        final boolean populated = populated(usernameText) && populated(passwordText) &&
                populated(emailText) && populated(confirmPasswordText) &&
                (populated(phoneText) || verificationMethod.equals(EMAIL));

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

package in.testpress.testpress.authenticator;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.R;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.ResetPassword;
import in.testpress.testpress.ui.TextWatcherAdapter;
import in.testpress.testpress.util.SafeAsyncTask;

public class ResetPasswordActivity extends FragmentActivity {

    @Inject TestpressService testpressService;
    private EditText email;
    private TextView emailError;
    private Button resetButton;
    public int resetErrorMessage;
    private Button okButton;
    private LinearLayout formContainer;
    private LinearLayout successContainer;
    private final TextWatcher watcher = validationTextWatcher();
    private RelativeLayout forgotPasswordContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        TestpressApplication.getAppComponent().inject(this);
        bindViews();
        resetErrorMessage = R.string.reset_error_message;
        email.addTextChangedListener(watcher);
        forgotPasswordContainer = findViewById(R.id.forgot_password_container);
    }

    private void bindViews() {
        email = findViewById(R.id.et_useremail);
        emailError = findViewById(R.id.email_error);
        resetButton = findViewById(R.id.b_reset_password);
        okButton = findViewById(R.id.success_ok);
        formContainer = findViewById(R.id.form);
        successContainer = findViewById(R.id.success_complete);

        resetButton.setOnClickListener(v -> reset());
        okButton.setOnClickListener(v -> passwordResetDone());
    }

    private void reset(){
        if(validate()) {
            final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .widgetColorRes(R.color.primary)
                    .progress(true, 0)
                    .show();
            new SafeAsyncTask<ResetPassword>() {

                @Override
                public ResetPassword call() throws Exception {

                    return testpressService.resetPassword(email.getText().toString());
                }

                @Override
                protected void onException(final Exception e) throws RuntimeException {
                    progressDialog.dismiss();
                    new MaterialDialog.Builder(ResetPasswordActivity.this)
                            .title("Error")
                            .content(R.string.reset_error_message)
                            .neutralText(R.string.ok)
                            .neutralColorRes(R.color.primary)
                            .buttonsGravity(GravityEnum.END)
                            .show();
                }

                @Override
                protected void onSuccess(final ResetPassword resetPassword) throws Exception {
                    progressDialog.dismiss();
                    formContainer.setVisibility(View.GONE);
                    successContainer.setVisibility(View.VISIBLE);
                    forgotPasswordContainer.setVisibility(View.GONE);
                }
            }.execute();
        }
    }

    private void passwordResetDone() {
        finish();
    }

    private boolean validate() {
        if (!populated(email)) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText(getString(R.string.email_error));
            return false;
        }
        //Email verification
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText(getString(R.string.email_error));
            email.requestFocus();
            return false;
        }
        return true;
    }

    private TextWatcher validationTextWatcher() {
        return new TextWatcherAdapter() {
            public void afterTextChanged(final Editable EditTextBox) {
                emailError.setVisibility(View.GONE);
            }
        };
    }

    private boolean populated(final EditText editText) {
        return editText.getText().toString().trim().length() > 0;
    }

    public void showAlert(String alertMessage) {
    }
}

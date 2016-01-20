package in.testpress.testpress.authenticator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.ResetPassword;
import in.testpress.testpress.util.SafeAsyncTask;

public class ResetPasswordVerificationActivity extends AppCompatActivity {

    @Inject TestpressService testpressService;
    @InjectView(R.id.et_useremail) EditText useremailText;
    @InjectView(R.id.b_reset_password) Button resetButton;
    public int resetErrorMessage;
    public String ok;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_verification);
        Injector.inject(this);
        ButterKnife.inject(this);
        resetErrorMessage = R.string.reset_error_message;
    }

    @OnClick(R.id.b_reset_password) public void reset(){

        new SafeAsyncTask<ResetPassword>() {

            @Override
            public ResetPassword call() throws Exception {

                return testpressService.getStatus(useremailText.getText().toString());
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                showAlert(getResources().getString(R.string.reset_error_message));
            }

            @Override
            protected void onSuccess(final ResetPassword resetPassword) throws Exception {

                showAlert(getResources().getString(R.string.reset_success_message));
            }
        }.execute();
    }

    public void showAlert(String alertMessage) {
        new MaterialDialog.Builder(ResetPasswordVerificationActivity.this)
                .content(alertMessage)
                .neutralText(R.string.ok)
                .neutralColorRes(R.color.primary)
                .buttonsGravity(GravityEnum.CENTER)
                .show();
    }
}

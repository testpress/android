package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.R;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.Assert;
import in.testpress.util.UIUtils;
import retrofit.client.Response;

public class AccountActivateActivity extends AppCompatActivity {

    public static final String ACTIVATE_URL_FRAG = "activateUrlFrag";

    @Inject TestpressService testpressService;

    ProgressBar progressBar;
    LinearLayout emptyView;
    TextView emptyTitleView;
    TextView emptyDescView;
    Button retryButton;
    LinearLayout successContainer;
    ImageView successImage;
    TextView successTitle;
    TextView successDescription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestpressApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_account_activate);
        bindViews();
        UIUtils.setIndeterminateDrawable(this, progressBar, 4);

        String urlFrag = getIntent().getStringExtra(ACTIVATE_URL_FRAG);
        Assert.assertNotNull("ACTIVATE_URL_FRAG must not be null.", urlFrag);
        Log.e("sssss",urlFrag);
        activateAccount(urlFrag.substring(1));
    }

    private void bindViews() {
        progressBar = findViewById(R.id.pb_loading);
        emptyView = findViewById(R.id.empty_container);
        emptyTitleView = findViewById(R.id.empty_title);
        emptyDescView = findViewById(R.id.empty_description);
        retryButton = findViewById(R.id.retry_button);
        successContainer = findViewById(R.id.success_complete);
        successImage = findViewById(R.id.success_image);
        successTitle = findViewById(R.id.success_title);
        successDescription = findViewById(R.id.success_description);

        findViewById(R.id.success_ok).setOnClickListener(v -> login());
    }

    private void activateAccount(final String urlFrag) {
        progressBar.setVisibility(View.VISIBLE);
        new SafeAsyncTask<Response>() {
            @Override
            public Response call() throws Exception {
                return testpressService.activateAccount(urlFrag);
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
                        activateAccount(urlFrag);
                    }
                });
            }

            @Override
            protected void onSuccess(Response response) throws Exception {
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(response.getBody().in()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String htmlResponse = stringBuilder.toString();
                if (htmlResponse.contains(getString(R.string.activate_completed_title))) {
                    successTitle.setText(R.string.activate_completed_title);
                    successDescription.setText(R.string.activate_completed_message);
                } else {
                    successImage.setImageResource(R.drawable.testpress_alert_warning);
                    successTitle.setText(R.string.activate_incomplete_title);
                    successDescription.setText(R.string.activate_incomplete_message);
                }
                successContainer.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void login() {
        if (CommonUtils.isUserAuthenticated(this)) {
            finish();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }
}

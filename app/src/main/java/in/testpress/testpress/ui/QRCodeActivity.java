package in.testpress.testpress.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ImageUtils;
import in.testpress.util.UIUtils;

import static com.nostra13.universalimageloader.core.assist.FailReason.FailType.IO_ERROR;

public class QRCodeActivity extends BaseAuthenticatedActivity {

    @Inject TestpressServiceProvider serviceProvider;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;
    @InjectView(R.id.qr_code_layout) LinearLayout qrCodeLayout;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Injector.inject(this);
        ButterKnife.inject(this);
        progressBar.setVisibility(View.VISIBLE);
        ProfileDetails profileDetails = ProfileDetails.getProfileDetailsFromPreferences(this);
        if (profileDetails == null) {
            UIUtils.setIndeterminateDrawable(this, progressBar, 4);
            fetchProfileDetails();
        } else {
            displayQRCode(profileDetails);
        }
    }

    private void fetchProfileDetails() {
        new SafeAsyncTask<ProfileDetails>() {
            @Override
            public ProfileDetails call() throws Exception {
                return serviceProvider.getService(QRCodeActivity.this).getProfileDetails();
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                int errorDescription;
                if (exception.getCause() instanceof IOException) {
                    errorDescription = R.string.no_internet_try_again;
                } else {
                    errorDescription = R.string.try_after_sometime;
                }
                setEmptyText(R.string.network_error, errorDescription);
                progressBar.setVisibility(View.GONE);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        fetchProfileDetails();
                    }
                });
            }

            @Override
            protected void onSuccess(ProfileDetails profileDetails) throws Exception {
                ProfileDetails.saveProfileDetailsInPreferences(QRCodeActivity.this, profileDetails);
                displayQRCode(profileDetails);
            }
        }.execute();
    }

    void displayQRCode(final ProfileDetails profileDetails) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoader imageLoader = ImageUtils.initImageLoader(this);
        final ImageView imageView = (ImageView) findViewById(R.id.qr_code);
        final String url = "https://simbatech.in/bill/viewimageTestpress.ashx?enrollmentid=" +
                profileDetails.getUsername();

        final TextView textView = (TextView) findViewById(R.id.qr_code_message);
        textView.setTypeface(TestpressSdk.getRubikMediumFont(this));
        imageLoader.loadImage(url, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (failReason.getType() == IO_ERROR) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again);
                    retryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emptyView.setVisibility(View.GONE);
                            displayQRCode(profileDetails);
                        }
                    });
                } else {
                    setEmptyText(R.string.qr_code_generation_failed, R.string.not_race_student);
                    retryButton.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setImageBitmap(loadedImage);
                textView.setText(R.string.my_race_enrollment_id);
                qrCodeLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    protected void setEmptyText(final int title, final int description) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
    }

}

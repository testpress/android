package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.models.FileDetails;
import in.testpress.network.TestpressApiClient;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.ImagePickerUtil;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ViewUtils;
import in.testpress.util.WebViewUtils;
import retrofit.RetrofitError;

import static in.testpress.testpress.ui.ForumActivity.URL;

public class CreateForumActivity extends TestpressFragmentActivity{

    @Inject protected TestpressService testpressService;
    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.title_heading) TextView titleHeading;
    @InjectView(R.id.topic_heading) TextView topicHeading;
    @InjectView(R.id.content_heading) TextView contentHeading;
    @InjectView(R.id.publish_button) Button publishButton;
    @InjectView(R.id.post_title) EditText postTitle;
    @InjectView(R.id.post_details) EditText postDetails;
    @InjectView(R.id.post_layout) LinearLayout postLayout;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;
    @InjectView(R.id.content_layout) LinearLayout contentLayout;
    @InjectView(R.id.title_layout) LinearLayout titleLayout;
    private ProgressDialog progressDialog;
    private static List<Category> categoryList;
    private ExploreSpinnerAdapter categoriesSpinnerAdapter;
    private Spinner categoriesSpinner;
    protected int selectedItemPosition = -1;
    ImagePickerUtil imagePickerUtil;
    Uri selectedCommentImageUri;
    String url;
    String imageHtml = "";
    @InjectView(android.R.id.content) View activityRootLayout;
    @InjectView(R.id.uploaded_image) ImageView imageView;
    @InjectView(R.id.image_clear_button) ImageButton imageButton;
    Activity activity;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public static Intent createIntent(Activity activity, List<Category> categories) {
        Intent intent = new Intent(activity, CreateForumActivity.class);
        categoryList = new ArrayList<>();
        categoryList.addAll(categories);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_forum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Post");
        activity = this;
        categoriesSpinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);
        categoriesSpinner = (Spinner) findViewById(R.id.categories_spinner) ;
        categoriesSpinner.setAdapter(categoriesSpinnerAdapter);
        addCategoriesItemsInSpinner();

        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageOnFail(R.drawable.profile_image_place_holder)
                .showImageForEmptyUri(R.drawable.profile_image_place_holder)
                .showImageOnLoading(R.drawable.profile_image_place_holder).build();

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItemPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postTitle.length() < 3) {
                    Snackbar.make(v, "The title must be at least 3 characters.",
                            Snackbar.LENGTH_SHORT).show();
                } else if (postTitle.length() > 200) {
                    Snackbar.make(v, "The title must be less than 200 characters.",
                            Snackbar.LENGTH_SHORT).show();
                } else if (postDetails.getText().toString().equals("")) {
                    Snackbar.make(v, "The content field is required.", Snackbar.LENGTH_SHORT).show();
                } else {
                    postForum(postTitle.getText().toString(),
                            Html.toHtml(new SpannableString(postDetails.getText().toString().trim()))
                                    + (!imageHtml.equals("")?"<br><br><br>":"") + imageHtml,
                            categoryList.get(selectedItemPosition).getSlug());
                }
            }
        });

        titleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postTitle.requestFocus();
            }
        });

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDetails.requestFocus();
            }
        });

        ViewUtils.setTypeface(new TextView[] {titleHeading, topicHeading, contentHeading},
                TestpressSdk.getRubikMediumFont(this));
        postTitle.setTypeface(TestpressSdk.getRubikMediumFont(this));
        publishButton.setTypeface(TestpressSdk.getRubikMediumFont(this));

        progressDialog = new ProgressDialog(this);

        imagePickerUtil = new ImagePickerUtil(activityRootLayout, this);
    }

    private void postForum(final String title, final String content, final String category) {
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        new SafeAsyncTask<Forum>() {
            @Override
            public Forum call() throws Exception {
                return getService().postForum(title, content, category);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else if (exception instanceof RetrofitError) {
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Posted successfully", Toast.LENGTH_SHORT).show();
                    setResult(2);
                    finish();
                    return;
                } else {
                    setEmptyText(R.string.network_error, R.string.try_after_sometime,
                            R.drawable.ic_error_outline_black_18dp);
                }
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        emptyView.setVisibility(View.GONE);
                        postLayout.setVisibility(View.VISIBLE);
                        postForum(title, content, category);
                    }
                });
                progressDialog.dismiss();
            }

            @Override
            protected void onSuccess(Forum forum) throws Exception {
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), "Posted successfully!", Toast.LENGTH_SHORT).show();
                setResult(2);
                finish();
            }
        }.execute();
    }

    @OnClick(R.id.image_upload_button) void pickImage() {
        CropImage.startPickImageActivity(this);
    }

    @OnClick(R.id.image_clear_button) void clearImage() {
        Snackbar.make(activityRootLayout, "Image cleared.",
                Snackbar.LENGTH_SHORT).show();
        imageHtml = "";
        imageView.setVisibility(View.GONE);
        imageButton.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePickerUtil.onActivityResult(requestCode, resultCode, data,
                new ImagePickerUtil.ImagePickerActivityResultHandler() {
                    @Override
                    public void onStoragePermissionsRequired(Uri selectedImageUri) {
                        selectedCommentImageUri = selectedImageUri;
                    }

                    @Override
                    public void onSuccessfullyImageCropped(String imagePath) {
                        uploadImage(imagePath);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        imagePickerUtil.onRequestPermissionsResult(requestCode, grantResults, selectedCommentImageUri);
    }

    void uploadImage(final String imagePath) {
//        if (!progressDialog.isShowing()) {
//            progressDialog.setMessage(getResources().getString(R.string.please_wait));
//            progressDialog.show();
//            progressDialog.setCancelable(true);
//        }
        if (!CommonUtils.isUserAuthenticated(this)) {
            showLoginScreen();
            return;
        }
        Snackbar.make(activityRootLayout, "Uploading image..", Snackbar.LENGTH_LONG).show();
        //noinspection ConstantConditions
        new TestpressApiClient(this, TestpressSdk.getTestpressSession(this))
                .upload(imagePath).enqueue(new TestpressCallback<FileDetails>() {
            @Override
            public void onSuccess(FileDetails fileDetails) {
//                progressDialog.dismiss();
                imageLoader.displayImage(fileDetails.getUrl(), imageView);
                imageView.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                Snackbar.make(activityRootLayout, "Image uploaded successfully! Click publish to publish the post.",
                        Snackbar.LENGTH_SHORT).show();
                imageHtml = WebViewUtils.appendImageTags(fileDetails.getUrl());
            }

            @Override
            public void onException(TestpressException exception) {
                handleExceptionOnSendComment(exception);
            }
        });
    }

    void showLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(Constants.DEEP_LINK_TO, Constants.DEEP_LINK_TO_POST);
        intent.putExtra(URL, url);
        startActivity(intent);
    }

    void handleExceptionOnSendComment(Exception exception) {
//        progressDialog.dismiss();
        if (exception.getCause() instanceof IOException) {
            Snackbar.make(activityRootLayout, R.string.testpress_no_internet_connection,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(activityRootLayout, R.string.testpress_network_error,
                    Snackbar.LENGTH_SHORT).show();
        }
    }
    
    private void addCategoriesItemsInSpinner() {
        categoriesSpinnerAdapter.clear();

        for (Category category : categoryList) {
            categoriesSpinnerAdapter.addItem(category.getSlug(), category.getName(), false, 0);
        }

        if (selectedItemPosition == -1) {
            selectedItemPosition = 0;
            categoriesSpinnerAdapter.notifyDataSetChanged();
        } else {
            categoriesSpinnerAdapter.notifyDataSetChanged();
            categoriesSpinner.setSelection(selectedItemPosition);
        }
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        postLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    /**
     * Call this method only from async task
     *
     * @return TestpressService
     */
    TestpressService getService() {
        if (CommonUtils.isUserAuthenticated(this)) {
            try {
                testpressService = serviceProvider.getService(CreateForumActivity.this);
            } catch (IOException | AccountsException e) {
                e.printStackTrace();
            }
        }
        return testpressService;
    }
}

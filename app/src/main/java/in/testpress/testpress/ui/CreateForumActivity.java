package in.testpress.testpress.ui;

import android.accounts.AccountsException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Category;
import in.testpress.testpress.models.Forum;
import in.testpress.testpress.util.CommonUtils;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ViewUtils;
import retrofit.RetrofitError;

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
    private ProgressDialog progressDialog;
    private static List<Category> categoryList;
    private ExploreSpinnerAdapter categoriesSpinnerAdapter;
    private Spinner categoriesSpinner;
    protected int selectedItemPosition = -1;

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

        categoriesSpinnerAdapter = new ExploreSpinnerAdapter(getLayoutInflater(), getResources(), false);
        categoriesSpinner = (Spinner) findViewById(R.id.categories_spinner) ;
        categoriesSpinner.setAdapter(categoriesSpinnerAdapter);
        addCategoriesItemsInSpinner();

        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("selected position", ""+position);
                Log.e("selected category", ""+categoryList.get(position).getSlug());
                selectedItemPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postTitle.getText().toString().equals("") || postDetails.getText().toString().equals("")) {
                    Snackbar.make(v, "You can't leave title or content blank", Snackbar.LENGTH_SHORT).show();
                } else if (postTitle.length() > 200) {
                    Snackbar.make(v, "Title is too long", Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.e("Inside", "publish button on click");
                    postForum(postTitle.getText().toString(), postDetails.getText().toString(),
                            categoryList.get(selectedItemPosition).getSlug());
                }
            }
        });

        ViewUtils.setTypeface(new TextView[] {titleHeading, topicHeading, contentHeading},
                TestpressSdk.getRubikMediumFont(this));
        postTitle.setTypeface(TestpressSdk.getRubikMediumFont(this));
        publishButton.setTypeface(TestpressSdk.getRubikMediumFont(this));

        progressDialog = new ProgressDialog(this);
    }

    private void postForum(final String title, final String content, final String category) {
//        postLayout.setVisibility(View.GONE);
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();
        new SafeAsyncTask<Forum>() {
            @Override
            public Forum call() throws Exception {
                Log.e("Inside", "call()");
                return getService().postForum(title, content, category);
            }

            @Override
            protected void onException(Exception exception) throws RuntimeException {
                Log.e("Inside", "onException()");
                Log.e("Exception", exception.toString());
                if (exception.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else if (exception instanceof RetrofitError) {
                    Log.e("Excptn msg",exception.getMessage());
                    Log.e("Excptn status",((RetrofitError) exception).getResponse().getStatus()+"");
                    Log.e("Excptn body",((RetrofitError) exception).getResponse().getBody()+"");
                    Log.e("Excptn reason",((RetrofitError) exception).getResponse().getReason()+"");
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
                Toast.makeText(getBaseContext(), "Posted successfully", Toast.LENGTH_SHORT).show();
                setResult(2);
                finish();
            }
        }.execute();
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

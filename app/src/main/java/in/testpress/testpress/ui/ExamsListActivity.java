package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.ExamCategory;
import in.testpress.testpress.util.SafeAsyncTask;

public class ExamsListActivity extends TestpressFragmentActivity {

    @Inject protected TestpressServiceProvider serviceProvider;
    @InjectView(R.id.fragment_container) FrameLayout fragmentContainer;
    @InjectView(R.id.pb_loading) RelativeLayout progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        Injector.inject(this);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadExamCategories();
    }

    void loadExamCategories() {
        emptyView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new SafeAsyncTask<List<ExamCategory>>() {
            public List<ExamCategory> call() throws Exception {
                return serviceProvider.getService(ExamsListActivity.this).getExamsCourses().getResults();
            }

            @Override
            protected void onException(final Exception exception) throws RuntimeException {
                exception.printStackTrace();
                if (exception.getCause() instanceof UnknownHostException) {
                    setEmptyText(R.string.network_error, R.string.no_internet, R.drawable.ic_error_outline_black_18dp);
                    Toaster.showShort(ExamsListActivity.this, getResources().getString(R.string.no_internet));
                } else {
                    setEmptyText(R.string.error_loading_exams, R.string.try_after_sometime, R.drawable.ic_error_outline_black_18dp);
                    Toaster.showShort(ExamsListActivity.this, exception.getMessage());
                }
            }

            @Override
            public void onSuccess(final List<ExamCategory> categoryList) {
                fragmentContainer.setVisibility(View.VISIBLE);
                CarouselFragment fragment = new CarouselFragment();
                Bundle bundle;
                if (getIntent().getExtras() == null) {
                    bundle = new Bundle();
                } else {
                    bundle = getIntent().getExtras();
                }
                bundle.putParcelableArrayList("categoryList",new ArrayList<Parcelable>(categoryList));
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
            }

            @Override
            protected void onFinally() throws RuntimeException {
                super.onFinally();
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
    }

    @OnClick(R.id.retry_button)
    protected void refreshWithProgress() {
        loadExamCategories();
    }

    @Override
    public void onBackPressed() {
        if(getIntent().getBooleanExtra("isDeepLink", false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
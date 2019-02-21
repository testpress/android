package in.testpress.testpress.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.testpress.core.TestpressSdk;
import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.ProfileDetailsDao;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.ViewUtils;

import static in.testpress.testpress.ui.InAppBrowserActivity.TITLE;
import static in.testpress.testpress.ui.InAppBrowserActivity.URL;

public class ProgressCardFragment extends Fragment {

    @Inject protected TestpressServiceProvider serviceProvider;

    @InjectView(R.id.pb_loading) ProgressBar progressBar;
    @InjectView(R.id.empty_container) LinearLayout emptyView;
    @InjectView(R.id.empty_title) TextView emptyTitleView;
    @InjectView(R.id.empty_description) TextView emptyDescView;
    @InjectView(R.id.retry_button) Button retryButton;
    @InjectView(R.id.items_layout) ScrollView itemsLayout;
    @InjectView(R.id.forms_label) TextView forumsLabel;
    @InjectView(R.id.schedule_label) TextView scheduleLabel;
    @InjectView(R.id.attendance_label) TextView attendanceLabel;
    @InjectView(R.id.marks_label) TextView marksLabel;
    @InjectView(R.id.review_label) TextView reviewLabel;

    ProfileDetailsDao profileDetailsDao;
    Long userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_progress_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Injector.inject(this);
        ButterKnife.inject(this, view);
        ViewUtils.setTypeface(
                new TextView[] { forumsLabel, scheduleLabel, attendanceLabel, marksLabel,
                        reviewLabel },

                TestpressSdk.getRubikMediumFont(getActivity())
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profileDetailsDao = ((TestpressApplication) getActivity().getApplicationContext())
                .getDaoSession().getProfileDetailsDao();

        if (profileDetailsDao.queryBuilder().count() > 0) {
            userId = profileDetailsDao.queryBuilder().list().get(0).getId();
            displayItems();
        } else {
            fetchProfileDetails();
        }
    }

    private void fetchProfileDetails() {
        progressBar.setVisibility(View.VISIBLE);
        new SafeAsyncTask<ProfileDetails>() {
            @Override
            public ProfileDetails call() throws Exception {
                return serviceProvider.getService(getActivity()).getProfileDetails();
            }

            @Override
            protected void onException(final Exception e) throws RuntimeException {
                super.onException(e);
                progressBar.setVisibility(View.GONE);
                if (e.getCause() instanceof IOException) {
                    setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                            R.drawable.ic_error_outline_black_18dp);
                } else {
                    setEmptyText(R.string.network_error, R.string.error_loading_content,
                            R.drawable.ic_error_outline_black_18dp);
                }
            }

            @Override
            protected void onSuccess(ProfileDetails profileDetails) throws Exception {
                profileDetailsDao.insertOrReplace(profileDetails);
                userId = profileDetails.getId();
                displayItems();
            }
        }.execute();
    }

    void displayItems() {
        progressBar.setVisibility(View.GONE);
        itemsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.form_layout) void showForms() {
        Intent intent = new Intent(getActivity(), FormListActivity.class);
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.schedule_layout) void showSchedule() {
        Intent intent = new Intent(getActivity(), InAppBrowserActivity.class);
        intent.putExtra(TITLE, getString(R.string.my_schedule));
        intent.putExtra(URL, getUrl() + "my_schedule");
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.attendance_layout) void showAttendance() {
        Intent intent = new Intent(getActivity(), InAppBrowserActivity.class);
        intent.putExtra(TITLE, getString(R.string.my_attendance));
        intent.putExtra(URL, getUrl() + "my_attendance");
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.test_mark_layout) void showTestMark() {
        Intent intent = new Intent(getActivity(), InAppBrowserActivity.class);
        intent.putExtra(TITLE, getString(R.string.my_test_mark));
        intent.putExtra(URL, getUrl() + "my_test_mark");
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.review_layout) void showReview() {
        Intent intent = new Intent(getActivity(), InAppBrowserActivity.class);
        intent.putExtra(TITLE, getString(R.string.my_review));
        intent.putExtra(URL, getUrl() + "my_review");
        getActivity().startActivity(intent);
    }

    String getUrl() {
        return "http://extelacademy.com/students1617/cgi_api_testpress.php?testpress_id=" + userId
                + "&report_type=";
    }

    protected void setEmptyText(final int title, final int description, final int left) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyTitleView.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
        emptyDescView.setText(description);
        retryButton.setVisibility(View.GONE);
    }

}

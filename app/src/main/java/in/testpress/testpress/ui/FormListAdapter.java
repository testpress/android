package in.testpress.testpress.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Form;
import in.testpress.testpress.models.ProfileDetails;
import in.testpress.testpress.models.ProfileDetailsDao;
import in.testpress.testpress.util.SafeAsyncTask;
import in.testpress.util.UIUtils;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

class FormListAdapter extends SingleTypeAdapter<Form> {

    private Activity activity;
    private TestpressService testpressService;
    private ProfileDetails profileDetails;

    FormListAdapter(Activity activity, TestpressService testpressService, List<Form> items) {
        super(activity.getLayoutInflater(), R.layout.form_list_item);

        this.activity = activity;
        this.testpressService = testpressService;
        ProfileDetailsDao profileDetailsDao = ((TestpressApplication) activity.getApplicationContext())
                .getDaoSession().getProfileDetailsDao();

        if (profileDetailsDao.queryBuilder().count() > 0) {
            profileDetails = profileDetailsDao.queryBuilder().list().get(0);
        }
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.form_name, R.id.status, R.id.request_form, R.id.description_layout,
                R.id.description };
    }

    @Override
    protected void update(final int position, final Form form) {
        setText(0, form.getFormName());
        setText(4, Html.fromHtml(form.getDesc()));
        if (Integer.parseInt(form.getStatus()) == 1) {
            setGone(2, false);
        } else {
            setGone(2, true);
        }
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(activity));
        textView(4).setTypeface(TestpressSdk.getRubikRegularFont(activity));
        textView(4).setTextColor(Color.parseColor(form.getDesccolor()));
        view(3).setBackgroundColor(Color.parseColor(form.getDescbgcolor()));

        if (form.getStatusmsg().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            setText(1, form.getStatusmsg());
            GradientDrawable shape = (GradientDrawable) textView(1).getBackground().getCurrent();
            shape.setColor(Color.parseColor(form.getStatusbgcolor()));
        }
        view(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(activity);
                progressDialog.setMessage(activity.getString(R.string.please_wait));
                progressDialog.setCancelable(false);
                UIUtils.setIndeterminateDrawable(activity, progressDialog, 4);
                progressDialog.show();
                new SafeAsyncTask<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return testpressService.requestForm(form.getFormId(), profileDetails);
                    }

                    @Override
                    protected void onException(Exception e) throws RuntimeException {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        View rootView = activity.findViewById(android.R.id.content);
                        Snackbar.make(rootView, R.string.no_internet, LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onSuccess(Integer data) throws Exception {
                        progressDialog.dismiss();
                        View rootView = activity.findViewById(android.R.id.content);
                        Snackbar.make(rootView, "Request sent", LENGTH_SHORT).show();
                    }
                }.execute();
            }
        });
    }
}
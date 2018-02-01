package in.testpress.testpress.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import java.util.List;

import in.testpress.core.TestpressSdk;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Notes;
import in.testpress.testpress.util.SafeAsyncTask;

class DocumentsListAdapter extends SingleTypeAdapter<Notes> {

    Activity activity;
    TestpressServiceProvider serviceProvider;

    DocumentsListAdapter(Activity activity, TestpressServiceProvider serviceProvider,
                         List<Notes> items, int layout) {

        super(activity.getLayoutInflater(), layout);
        this.activity = activity;
        this.serviceProvider = serviceProvider;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.title, R.id.description};
    }

    @Override
    protected void update(final int position, final Notes notes) {
        setText(0, notes.getTitle());
        if (notes.getDescription() == null || notes.getDescription().isEmpty()) {
            setGone(1, true);
        } else {
            setGone(1, false);
            setText(1, notes.getDescription());
        }
        textView(0).setTypeface(TestpressSdk.getRubikMediumFont(activity));
        textView(1).setTypeface(TestpressSdk.getRubikRegularFont(activity));
        ImageView image = ((ImageView) updater.view.findViewById(R.id.image));
        updater.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog =
                        new ProgressDialog(activity);
                progressDialog.setMessage(activity.getResources().getString(R.string.please_wait));
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setProgressNumberFormat(null);
                progressDialog.setProgressPercentFormat(null);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
                new SafeAsyncTask<Notes>() {
                    @Override
                    public Notes call() throws Exception {
                        return serviceProvider.getService(activity).getDownloadUrl(notes.getSlug());
                    }

                    @Override
                    protected void onException(Exception e) throws RuntimeException {
                        progressDialog.dismiss();
                        Toaster.showShort(activity, R.string.no_internet);
                    }

                    @Override
                    protected void onSuccess(Notes notes) throws Exception {
                        progressDialog.dismiss();
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(notes.getUrl())));
                    }
                }.execute();
            }
        });
        switch (position % 5) {
            case 0:
                image.setColorFilter(Color.parseColor("#52c6bb"));
                break;
            case 1:
                image.setColorFilter(Color.parseColor("#80aaef"));
                break;
            case 2:
                image.setColorFilter(Color.parseColor("#ac78c3"));
                break;
            case 3:
                image.setColorFilter(Color.parseColor("#f99b91"));
                break;
            case 4:
                image.setColorFilter(Color.parseColor("#fed358"));
                break;
        }
    }
}
package in.testpress.testpress.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.models.Notes;
import in.testpress.testpress.util.SafeAsyncTask;

public class DocumentsListAdapter extends SingleTypeAdapter<Notes> {

    Activity activity;
    TestpressServiceProvider serviceProvider;

    public DocumentsListAdapter(Activity activity, TestpressServiceProvider serviceProvider, final List<Notes> items, int layout) {
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
        setText(1, notes.getDescription());
        ImageView image = ((ImageView) updater.view.findViewById(R.id.image));
        updater.view.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialDialog progressDialog = new MaterialDialog.Builder(activity)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .show();
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
package in.testpress.testpress.ui;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import in.testpress.testpress.R;

public class ProfilePhotoActivity extends TestpressFragmentActivity {
    ProgressBar progressBar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_image_view_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        final TextView emptyView = (TextView)findViewById(R.id.empty);
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .build();
        ImageLoader.getInstance().loadImage(getIntent().getStringExtra("profilePhoto"), options,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBar.setVisibility(View.GONE);
                        if(failReason.getType().equals(FailReason.FailType.IO_ERROR)) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(R.string.no_internet);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(failReason.getCause().getMessage());
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        progressBar.setVisibility(View.GONE);
                        ((TouchImageView) findViewById(R.id.image)).setImageBitmap(loadedImage);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else if(item.getItemId() == R.id.edit) {
            //handled on onActivityResult of ProfileDetailsActivity
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return false;
    }
}

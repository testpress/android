package in.testpress.testpress.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import in.testpress.testpress.R;

public class ZoomableImageActivity extends Activity {
    ProgressBar progressBar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_zoomable_image);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        final TextView emptyView = (TextView)findViewById(R.id.empty);
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();
        ImageLoader.getInstance().loadImage(getIntent().getStringExtra("url"), options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBar.setVisibility(View.GONE);
                        if(failReason.getType().equals(FailReason.FailType.IO_ERROR)) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(R.string.no_internet_try_again);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText(getString(R.string.something_went_wrong_please_try_after));
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
}

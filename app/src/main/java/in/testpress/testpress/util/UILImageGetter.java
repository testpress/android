package in.testpress.testpress.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.InputStream;


public class UILImageGetter implements Html.ImageGetter {
    Activity activity;
    TextView container;
    private int finalContainerHeight;

    /***
     * Construct the UILImageGetter which will execute AsyncTask and refresh the container
     * @param t
     * @param activity
     */
    public UILImageGetter(View t, Activity activity) {
        this.activity = activity;
        this.container = (TextView)t;
    }

    @Override
    public Drawable getDrawable(String source) {
        UrlImageDownloader urlDrawable = new UrlImageDownloader(activity.getApplicationContext().getResources(), source);
        ImageLoader.getInstance().loadImage(source, new SimpleListener(urlDrawable));
        return urlDrawable;
    }

    private class SimpleListener extends SimpleImageLoadingListener
    {
        UrlImageDownloader urlImageDownloader;

        public SimpleListener(UrlImageDownloader downloader) {
            super();
            urlImageDownloader = downloader;
        }

        @Override
        public void onLoadingComplete(final String imageUri, View view, final Bitmap loadedImage) {
            // The images load asynchronously. But the textview doesn't get invalidated easily
            // We keep adding the downloaded image height and add it to the existing container
            // height and finally set the container (textview) height to the final value.
            // The first time image loads from the network. Subsequent times
            // the image comes from the file cache and loading of images completes before the
            // text view is rendered. Hence container.getHeight() will return zero resulting in
            // calculated height lesser than the actual height. Hence this is now made to run in the
            // UI Runnable thread which will ensure that the container is actually rendered first
            // and it has a valid layout height value.
            container.post(new Runnable() {
                @Override
                public void run() {
                    if (null != loadedImage) {

                        int width = loadedImage.getWidth();
                        int height = loadedImage.getHeight();

                        Log.e("UILImageGetter", imageUri);
                        Log.e("UILImageGetter", "Original Width " + width);
                        Log.e("UILImageGetter", "Original Height " + height);
                        int newWidth = width;
                        int newHeight = height;

                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int screenWidth = displaymetrics.widthPixels;
                        if( width > screenWidth ) {
                            newWidth = screenWidth;
                            newHeight = (newWidth * height) / width;
                        }

                        Log.e("UILImageGetter", "New Width " + newWidth);
                        Log.e("UILImageGetter", "New Height " + newHeight);
                        Drawable result = new BitmapDrawable(activity.getApplicationContext().getResources(), loadedImage);
                        result.setBounds(0, 0, newWidth, newHeight);
                        urlImageDownloader.setBounds(0, 0, newWidth, newHeight);
                        urlImageDownloader.drawable = result;

                        container.invalidate();

                        Log.e("UILImageGetter", "Container Height " + container.getHeight());
                        Log.e("UILImageGetter", "B4 finalContainer Height " + finalContainerHeight);
                        if (0 != finalContainerHeight) {
                            finalContainerHeight += newHeight;
                        } else {
                            finalContainerHeight = container.getHeight() + newHeight;
                        }
                        Log.e("UILImageGetter", "finalContainer Height " + finalContainerHeight);

                        // http://stackoverflow.com/questions/7870312/android-imagegetter-images-overlapping-text
                        // For ICS
                        //container.setHeight(finalContainerHeight);

                        container.setText(container.getText());

                        container.invalidate();

                        // Pre ICS
                        container.setEllipsize(null);
                    }
                }
            });
        }
    }
}

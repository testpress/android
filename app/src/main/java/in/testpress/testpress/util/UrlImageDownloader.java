package in.testpress.testpress.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;

public class UrlImageDownloader extends BitmapDrawable
{
    public Drawable drawable;

    /**
     * Create a drawable by decoding a bitmap from the given input stream.
     *
     * @param res
     * @param is
     */
    public UrlImageDownloader(Resources res, InputStream is) {
        super(res, is);
    }

    /**
     * Create a drawable by opening a given file path and decoding the bitmap.
     *
     * @param res
     * @param filepath
     */
    public UrlImageDownloader(Resources res, String filepath) {
        super(res, filepath);
        drawable = new BitmapDrawable(res, filepath);
    }

    /**
     * Create drawable from a bitmap, setting initial target density based on
     * the display metrics of the resources.
     *
     * @param res
     * @param bitmap
     */
    public UrlImageDownloader(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    @Override
    public void draw(Canvas canvas) {
        // override the draw to facilitate refresh function later
        if(drawable != null) {
            drawable.draw(canvas);
        }
    }
}

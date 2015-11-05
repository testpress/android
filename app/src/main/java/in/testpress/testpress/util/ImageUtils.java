package in.testpress.testpress.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {

    //if Bitmap is too large, OutOfMemory error will come. so, to scale down the image this method will use
    public static Bitmap decodeImage(String pathName, final int width, final int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        //Find the correct scale value. It should be the power of 2.
        int scale=1;
        while(options.outWidth / scale / 2 >= width && options.outHeight / scale / 2 >= height)
            scale *= 2;
        //Decode with inSampleSize
        BitmapFactory.Options scalableOptions = new BitmapFactory.Options();
        scalableOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(pathName, scalableOptions);
    }
}

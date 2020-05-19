package in.testpress.testpress.util;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import in.testpress.testpress.R;

public class ImageUtils {
    public static DisplayImageOptions getPlaceholdersOption() {
        return new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageForEmptyUri(R.drawable.image_placeholder)
                .showImageOnFail(R.drawable.image_placeholder)
                .showImageOnLoading(R.drawable.image_placeholder).build();
    }
}

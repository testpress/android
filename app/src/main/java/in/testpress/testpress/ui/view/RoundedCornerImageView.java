package in.testpress.testpress.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import in.testpress.testpress.R;

public class RoundedCornerImageView extends AppCompatImageView {

    private float cornerRadius = 0;

    private void initCornerRadius(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundedCornerImageView,
                0, 0);
        try {
            cornerRadius = a.getFloat(R.styleable.RoundedCornerImageView_imageCornerRadius, 0);
        } finally {
            a.recycle();
        }
    }

    public RoundedCornerImageView(Context context) {
        super(context);
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCornerRadius(context, attrs);
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCornerRadius(context, attrs);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        RoundedBitmapDrawable rid = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        rid.setCornerRadius(bitmap.getWidth() * cornerRadius);
        super.setImageDrawable(rid);
    }
}
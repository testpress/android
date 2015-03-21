package in.testpress.testpress.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import in.testpress.testpress.util.Strings;
import android.util.Log;
import java.util.Hashtable;
import java.util.Locale;

/**
 * A button who's text is always uppercase which uses the roboto font.
 * Inspired by <code>com.actionbarsherlock.internal.widget.CapitalizingTextView</code>
 */
public class CapitalizedTextView extends Button {

    private static final boolean IS_GINGERBREAD
            = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    private static final String TAG = "Typefaces";
    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public CapitalizedTextView(Context context) {
        super(context);

        setTF(context);
    }

    public CapitalizedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTF(context);
    }

    public CapitalizedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setTF(context);

    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (IS_GINGERBREAD) {
            try {
                super.setText(text.toString().toUpperCase(Locale.ROOT), type);
            } catch (NoSuchFieldError e) {
                //Some manufacturer broke Locale.ROOT. See #572.
                super.setText(text.toString().toUpperCase(), type);
            }
        } else {
            super.setText(text.toString().toUpperCase(), type);
        }
    }

    public static Typeface getTypeFace(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e(TAG, "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }

    private void setTF(Context context) {
        Typeface tf = getTypeFace(context, "fonts/Roboto-Regular.ttf");
        setTypeface(tf);
    }
}

package in.testpress.testpress;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import in.testpress.testpress.models.DaoMaster;
import in.testpress.testpress.models.DaoSession;

public class TestpressApplication extends Application {
    private static TestpressApplication instance;
    public DaoSession daoSession;

    /**
     * Create main application
     */
    public TestpressApplication() {
    }

    /**
     * Create main application
     *
     * @param context
     */
    public TestpressApplication(final Context context) {
        this();
        attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // Perform injection
        Injector.init(getRootModule(), this);
        initImageLoader(getApplicationContext());
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "testpress-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        Stetho.initializeWithDefaults(this);

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(500 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
    }

    private Object getRootModule() {
        return new RootModule();
    }
    /**
     * Create main application
     *
     * @param instrumentation
     */
    public TestpressApplication(final Instrumentation instrumentation) {
        this();
        attachBaseContext(instrumentation.getTargetContext());
    }

    public static TestpressApplication getInstance() {
        return instance;
    }
}

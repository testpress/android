package in.testpress.testpress;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import in.testpress.testpress.models.DaoMaster;
import in.testpress.testpress.models.DaoSession;
import in.testpress.testpress.models.InstituteSettings;
import in.testpress.testpress.models.InstituteSettingsDao;
import static in.testpress.testpress.BuildConfig.BASE_URL;
import in.testpress.testpress.util.NotificationHelper;

public class TestpressApplication extends Application {
    private static TestpressApplication instance;
    public static DaoSession daoSession;
    private static SQLiteDatabase database;
    private static AppComponent appComponent;

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
        appComponent = DaggerAppComponent.builder()
                .androidModule(new AndroidModule())
                .testpressModule(new TestpressModule())
                .build();

        initImageLoader(getApplicationContext());
        SQLiteDatabase db = getDatabase(this);
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        NotificationHelper.createChannels(this);
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static SQLiteDatabase getDatabase(@NonNull Context context) {
        if (database == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                    context.getApplicationContext(), "testpress-db", null);

            database = helper.getWritableDatabase();
        }
        return database;
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    public static void clearDatabase(@NonNull Context context) {
        SQLiteDatabase database = getDatabase(context);
        DaoMaster.dropAllTables(database, true);
        DaoMaster.createAllTables(database, true);
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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

    public static InstituteSettings getInstituteSettings(){
        InstituteSettingsDao instituteSettingsDao = daoSession.getInstituteSettingsDao();
        return instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BASE_URL))
                .list().get(0);

    }
}

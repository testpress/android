package in.testpress.testpress;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.activeandroid.ActiveAndroid;

public class TestpressApplication extends Application {
    private static TestpressApplication instance;

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
        ActiveAndroid.initialize(this);

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

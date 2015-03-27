package in.testpress.testpress;

import dagger.Module;

/**
 * Add all the other modules to this one.
 */
@Module(
        includes = {
                AndroidModule.class,
                TestpressModule.class
        }
)
public class RootModule {
}

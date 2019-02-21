package in.testpress.testpress;


import android.accounts.AccountManager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.authenticator.RegisterActivity;
import in.testpress.testpress.authenticator.ResetPasswordActivity;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.CodeVerificationActivity;
import in.testpress.testpress.core.PostFromAnyThreadBus;
import in.testpress.testpress.core.RestAdapterRequestInterceptor;
import in.testpress.testpress.core.RestErrorHandler;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.core.UserAgentProvider;
import in.testpress.testpress.ui.AccountActivateActivity;
import in.testpress.testpress.ui.CreateForumActivity;
import in.testpress.testpress.ui.DocumentsListActivity;
import in.testpress.testpress.ui.DocumentsListFragment;
import in.testpress.testpress.ui.DrupalRssListActivity;
import in.testpress.testpress.ui.DrupalRssListFragment;
import in.testpress.testpress.ui.ForumActivity;
import in.testpress.testpress.ui.ForumListActivity;
import in.testpress.testpress.ui.ForumListFragment;
import in.testpress.testpress.ui.RssFeedDetailActivity;
import in.testpress.testpress.ui.SplashScreenActivity;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.MainMenuFragment;
import in.testpress.testpress.ui.OrdersListActivity;
import in.testpress.testpress.ui.OrdersListFragment;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.PostsListActivity;
import in.testpress.testpress.ui.PostsListFragment;
import in.testpress.testpress.ui.ProfileDetailsActivity;
import in.testpress.testpress.ui.ProfilePhotoActivity;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import static in.testpress.testpress.BuildConfig.BASE_URL;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */

@Module(
        complete = false,

        injects = {
                TestpressApplication.class,
                LoginActivity.class,
                MainActivity.class,
                RegisterActivity.class,
                CodeVerificationActivity.class,
                OrdersListActivity.class,
                OrdersListFragment.class,
                PostActivity.class,
                PostsListActivity.class,
                PostsListFragment.class,
                ProfileDetailsActivity.class,
                ProfilePhotoActivity.class,
                MainMenuFragment.class,
                ResetPasswordActivity.class,
                DocumentsListActivity.class,
                DocumentsListFragment.class,
                SplashScreenActivity.class,
                DrupalRssListActivity.class,
                DrupalRssListFragment.class,
                RssFeedDetailActivity.class,
                AccountActivateActivity.class,
                ForumListActivity.class,
                ForumListFragment.class,
                ForumActivity.class,
                CreateForumActivity.class
        }
)
public class TestpressModule {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new PostFromAnyThreadBus();
    }

//    @Provides
//    @Singleton
//    LogoutService provideLogoutService(final Context context, final AccountManager accountManager) {
//        return new LogoutService(context, accountManager);
//    }

    @Provides
    TestpressService provideTestpressService(RestAdapter.Builder restAdapter) {
        return new TestpressService(restAdapter);
    }

    @Provides
    TestpressServiceProvider provideTestpressServiceProvider(RestAdapter.Builder restAdapter, ApiKeyProvider apiKeyProvider) {
        return new TestpressServiceProvider(restAdapter, apiKeyProvider);
    }

    @Provides
    ApiKeyProvider provideApiKeyProvider(AccountManager accountManager) {
        return new ApiKeyProvider(accountManager);
    }

    @Provides
    Gson provideGson() {
        /**
         * GSON instance to use for all request  with date format set up for proper parsing.
         * <p/>
         * You can also configure GSON with different naming policies for your API.
         * Maybe your API is Rails API and all json values are lower case with an underscore,
         * like this "first_name" instead of "firstName".
         * You can configure GSON as such below.
         * <p/>
         *
         * public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd")
         *         .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();
         */
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    RestErrorHandler provideRestErrorHandler(Bus bus) {
        return new RestErrorHandler(bus);
    }

    @Provides
    RestAdapterRequestInterceptor provideRestAdapterRequestInterceptor(UserAgentProvider userAgentProvider) {
        return new RestAdapterRequestInterceptor(userAgentProvider);
    }

    @Provides
    RestAdapter.Builder provideRestAdapter(RestErrorHandler restErrorHandler, RestAdapterRequestInterceptor restRequestInterceptor, Gson gson) {

        return new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .setErrorHandler(restErrorHandler)
                .setRequestInterceptor(restRequestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson));
    }
}

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
import in.testpress.testpress.authenticator.RegistrationIntentService;
import in.testpress.testpress.authenticator.ResetPasswordActivity;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.CodeVerificationActivity;
import in.testpress.testpress.core.Constants;
import in.testpress.testpress.core.PostFromAnyThreadBus;
import in.testpress.testpress.core.RestAdapterRequestInterceptor;
import in.testpress.testpress.core.RestErrorHandler;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.core.UserAgentProvider;
import in.testpress.testpress.ui.AttemptFragment;
import in.testpress.testpress.ui.AttemptsListActivity;
import in.testpress.testpress.ui.AttemptsListAdapter;
import in.testpress.testpress.ui.AttemptsListFragment;
import in.testpress.testpress.ui.AvailableExamsListAdapter;
import in.testpress.testpress.ui.CropImageActivity;
import in.testpress.testpress.ui.ExamActivity;
import in.testpress.testpress.ui.ExamsListActivity;
import in.testpress.testpress.ui.ExamsListFragment;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.MainMenuFragment;
import in.testpress.testpress.ui.NavigationDrawerFragment;
import in.testpress.testpress.ui.OrderConfirmActivity;
import in.testpress.testpress.ui.OrdersListActivity;
import in.testpress.testpress.ui.OrdersListFragment;
import in.testpress.testpress.ui.PaymentSuccessActivity;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.PostsListActivity;
import in.testpress.testpress.ui.PostsListFragment;
import in.testpress.testpress.ui.ProductDetailsActivity;
import in.testpress.testpress.ui.ProductListFragment;
import in.testpress.testpress.ui.ProductsListActivity;
import in.testpress.testpress.ui.ProfileDetailsActivity;
import in.testpress.testpress.ui.ProfilePhotoActivity;
import in.testpress.testpress.ui.ReviewActivity;
import in.testpress.testpress.ui.ReviewFragment;
import in.testpress.testpress.ui.ReviewQuestionsFragment;
import in.testpress.testpress.ui.ReviewStatsFragment;
import in.testpress.testpress.ui.paymentGateway.CreditDebitCardActivity;
import in.testpress.testpress.ui.paymentGateway.NetBankingActivity;
import in.testpress.testpress.ui.paymentGateway.PaymentModeActivity;
import in.testpress.testpress.ui.paymentGateway.PaymentsActivity;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

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
                ExamActivity.class,
                NavigationDrawerFragment.class,
                ExamsListFragment.class,
                AttemptFragment.class,
                ReviewFragment.class,
                AttemptsListFragment.class,
                ReviewQuestionsFragment.class,
                ReviewActivity.class,
                AttemptsListActivity.class,
                RegisterActivity.class,
                CodeVerificationActivity.class,
                ExamsListActivity.class,
                ProductsListActivity.class,
                ProductListFragment.class,
                ProductDetailsActivity.class,
                OrdersListActivity.class,
                OrdersListFragment.class,
                PaymentSuccessActivity.class,
                OrderConfirmActivity.class,
                PaymentModeActivity.class,
                PaymentsActivity.class,
                CreditDebitCardActivity.class,
                NetBankingActivity.class,
                RegistrationIntentService.class,
                PostActivity.class,
                PostsListActivity.class,
                PostsListFragment.class,
                AttemptsListAdapter.class,
                AvailableExamsListAdapter.class,
                ReviewStatsFragment.class,
                ProfileDetailsActivity.class,
                ProfilePhotoActivity.class,
                CropImageActivity.class,
                MainMenuFragment.class,
                ResetPasswordActivity.class
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
    TestpressService provideTestpressService(RestAdapter restAdapter) {
        return new TestpressService(restAdapter);
    }

    @Singleton
    @Provides
    TestpressServiceProvider provideTestpressServiceProvider(RestAdapter restAdapter, ApiKeyProvider apiKeyProvider) {
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
    RestAdapter provideRestAdapter(RestErrorHandler restErrorHandler, RestAdapterRequestInterceptor restRequestInterceptor, Gson gson) {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.Http.URL_BASE)
                .setErrorHandler(restErrorHandler)
                .setRequestInterceptor(restRequestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
    }
}

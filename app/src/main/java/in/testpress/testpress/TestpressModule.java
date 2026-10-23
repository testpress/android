package in.testpress.testpress;


import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import in.testpress.testpress.authenticator.ApiKeyProvider;
import in.testpress.testpress.core.PostFromAnyThreadBus;
import in.testpress.testpress.core.RestAdapterRequestInterceptor;
import in.testpress.testpress.core.RestErrorHandler;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.core.UserAgentProvider;
import in.testpress.testpress.models.pojo.PerformanceValue;
import in.testpress.testpress.models.pojo.PerformanceValueDeserializer;
import in.testpress.testpress.network.PerformanceAnalysisApiService;
import in.testpress.testpress.repository.PerformanceAnalysisRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static in.testpress.testpress.BuildConfig.BASE_URL;
import static in.testpress.testpress.network.PerformanceAnalysisApiServiceKt.PERFORMANCE_ANALYSIS_API_KEY;
import static in.testpress.testpress.network.PerformanceAnalysisApiServiceKt.PERFORMANCE_ANALYSIS_API_KEY_HEADER;
import static in.testpress.testpress.network.PerformanceAnalysisApiServiceKt.PERFORMANCE_ANALYSIS_BASE_URL;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */

@Module
public class TestpressModule {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new PostFromAnyThreadBus();
    }

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
    public Gson provideGson() {
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
                .registerTypeAdapter(PerformanceValue.class, new PerformanceValueDeserializer())
                .create();
    }

    @Provides
    RestErrorHandler provideRestErrorHandler(Bus bus) {
        return new RestErrorHandler(bus);
    }

    @Provides
    RestAdapterRequestInterceptor provideRestAdapterRequestInterceptor(UserAgentProvider userAgentProvider, Context context) {
        return new RestAdapterRequestInterceptor(userAgentProvider, context);
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

    @Provides
    @Named("performanceAnalysisApiKey")
    String providePerformanceAnalysisApiKey() {
        return PERFORMANCE_ANALYSIS_API_KEY;
    }

    @Singleton
    @Provides
    PerformanceAnalysisApiService providePerformanceAnalysisApiService(
            @Named("performanceAnalysisApiKey") String apiKey,
            Gson gson
    ) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header(PERFORMANCE_ANALYSIS_API_KEY_HEADER, apiKey)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(PERFORMANCE_ANALYSIS_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(PerformanceAnalysisApiService.class);
    }

    @Provides
    PerformanceAnalysisRepository providePerformanceAnalysisRepository(
            PerformanceAnalysisApiService service,
            @Named("performanceAnalysisApiKey") String apiKey,
            Gson gson
    ) {
        return new PerformanceAnalysisRepository(service, apiKey, gson);
    }
}

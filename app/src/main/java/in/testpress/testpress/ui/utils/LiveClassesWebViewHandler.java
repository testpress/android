package in.testpress.testpress.ui.utils;

import android.os.Bundle;
import android.net.Uri;
import android.text.TextUtils;
import java.util.List;

import in.testpress.fragments.WebViewFragment;
import in.testpress.core.TestpressSession;
import in.testpress.core.TestpressSdk;
import in.testpress.course.TestpressCourse;
import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.R;
import in.testpress.testpress.TestpressServiceProvider;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.authenticator.LogoutService;
import in.testpress.testpress.models.SsoUrl;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.util.SafeAsyncTask;

public class LiveClassesWebViewHandler {

    private static final String LIVE_CLASSES_PATH = "/events/live-classes-list/?testpress_app=android";

    public static void addLiveClassesWebViewFragment(
            final MainActivity activity,
            final TestpressServiceProvider serviceProvider,
            final TestpressService testpressService,
            final LogoutService logoutService
    ) {
        final String initialUrl = BuildConfig.BASE_URL + LIVE_CLASSES_PATH;
        final WebViewFragment webViewFragment = new WebViewFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(WebViewFragment.URL_TO_OPEN, initialUrl);
        bundle.putBoolean(WebViewFragment.SHOW_LOADING_BETWEEN_PAGES, true);
        bundle.putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, true);
        bundle.putBoolean(WebViewFragment.ENABLE_SWIPE_REFRESH, true);
        bundle.putBoolean(WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, true);
        webViewFragment.setArguments(bundle);
        webViewFragment.setListener(new WebViewFragment.Listener() {
            @Override
            public void onWebViewInitializationSuccess() {}

            @Override
            public boolean shouldOverrideUrlLoading(String url) {
                if (url == null) return false;

                Uri uri = Uri.parse(url);
                String path = uri.getPath();

                // Intercept native content detail pages (live class item detail)
                if (path != null && (path.contains("/live-classes/") || path.contains("/events/live-classes") || path.contains("/contents/"))) {
                    List<String> segments = uri.getPathSegments();
                    if (segments != null && !segments.isEmpty()) {
                        String lastSegment = segments.get(segments.size() - 1);
                        if (TextUtils.isDigitsOnly(lastSegment) && !lastSegment.isEmpty()) {
                            TestpressSession session = TestpressSdk.getTestpressSession(activity);
                            if (session != null) {
                                TestpressCourse.showContentDetail(activity, lastSegment, session);
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public void onPageStarted(String url) {}

            @Override
            public void onPageFinished(String url) {}
        });

        fetchSsoUrlAndLoad(activity, webViewFragment, serviceProvider, false);

        activity.addMenuItem(R.string.live_classes, R.drawable.ic_video_white, webViewFragment);
    }

    public static void reload(
            final MainActivity activity,
            final WebViewFragment webViewFragment,
            final TestpressServiceProvider serviceProvider
    ) {
        if (webViewFragment.isAdded() && webViewFragment.getWebView() != null) {
            String currentUrl = webViewFragment.getWebView().getUrl();
            if (currentUrl != null && !currentUrl.isEmpty()) {
                Uri uri = Uri.parse(currentUrl);
                String host = uri.getHost();
                String path = uri.getPath();

                String baseHost = Uri.parse(BuildConfig.BASE_URL).getHost();
                String whiteLabelHost = Uri.parse(BuildConfig.WHITE_LABELED_HOST_URL).getHost();

                if (host != null && (host.equals(baseHost) || host.equals(whiteLabelHost))) {
                    if (path != null && !path.contains("/login") && !path.contains("/logout")) {
                        return;
                    }
                }
            }
        }

        if (webViewFragment.isAdded()) {
            webViewFragment.showLoading();
        }

        fetchSsoUrlAndLoad(activity, webViewFragment, serviceProvider, true);
    }

    private static void fetchSsoUrlAndLoad(
            final MainActivity activity,
            final WebViewFragment webViewFragment,
            final TestpressServiceProvider serviceProvider,
            final boolean showLoaderOnError
    ) {
        new SafeAsyncTask<SsoUrl>() {
            @Override
            public SsoUrl call() throws Exception {
                return serviceProvider.getService(activity).getSsoUrl();
            }

            @Override
            protected void onSuccess(SsoUrl ssoUrl) throws Exception {
                super.onSuccess(ssoUrl);
                if (ssoUrl != null && ssoUrl.getSsoUrl() != null) {
                    String fullSsoUrl = BuildConfig.WHITE_LABELED_HOST_URL + ssoUrl.getSsoUrl() + "&next=" + LIVE_CLASSES_PATH;
                    if (webViewFragment.getArguments() != null) {
                        webViewFragment.getArguments().putString(WebViewFragment.URL_TO_OPEN, fullSsoUrl);
                    }
                    if (webViewFragment.isAdded() && webViewFragment.getWebView() != null) {
                        webViewFragment.getWebView().loadUrl(fullSsoUrl);
                    }
                } else {
                    if (showLoaderOnError && webViewFragment.isAdded()) {
                        webViewFragment.hideLoading();
                    }
                }
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                if (showLoaderOnError && webViewFragment.isAdded()) {
                    webViewFragment.hideLoading();
                }
            }
        }.execute();
    }
}

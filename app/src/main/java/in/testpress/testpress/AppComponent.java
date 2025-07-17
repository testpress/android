package in.testpress.testpress;

import javax.inject.Singleton;

import dagger.Component;
import in.testpress.testpress.authenticator.CodeVerificationActivity;
import in.testpress.testpress.authenticator.LoginActivity;
import in.testpress.testpress.authenticator.LoginActivityV2;
import in.testpress.testpress.authenticator.RegisterActivity;
import in.testpress.testpress.authenticator.ResetPasswordActivity;
import in.testpress.testpress.ui.AccountActivateActivity;
import in.testpress.testpress.ui.AccountDeleteActivity;
import in.testpress.testpress.ui.BaseAuthenticatedActivity;
import in.testpress.testpress.ui.DocumentsListActivity;
import in.testpress.testpress.ui.DocumentsListFragment;
import in.testpress.testpress.ui.DoubtsActivity;
import in.testpress.testpress.ui.DrupalRssListActivity;
import in.testpress.testpress.ui.DrupalRssListFragment;
import in.testpress.testpress.ui.EnforceDataActivity;
import in.testpress.testpress.ui.MainActivity;
import in.testpress.testpress.ui.MainMenuFragment;
import in.testpress.testpress.ui.OrdersListActivity;
import in.testpress.testpress.ui.OrdersListFragment;
import in.testpress.testpress.ui.PostActivity;
import in.testpress.testpress.ui.PostsListFragment;
import in.testpress.testpress.ui.ProfileDetailsActivity;
import in.testpress.testpress.ui.ProfilePhotoActivity;
import in.testpress.testpress.ui.RssFeedDetailActivity;
import in.testpress.testpress.ui.SplashScreenActivity;
import in.testpress.testpress.ui.TermsAndConditionActivity;
import in.testpress.testpress.ui.TestpressActivity;
import in.testpress.testpress.ui.TestpressFragmentActivity;
import in.testpress.testpress.ui.WebViewActivity;
import in.testpress.testpress.ui.fragments.DashboardFragment;
import in.testpress.testpress.ui.fragments.OTPVerificationFragment;
import in.testpress.testpress.ui.fragments.PhoneAuthenticationFragment;
import in.testpress.testpress.ui.fragments.UsernameAuthentication;

@Singleton
@Component(modules = {AndroidModule.class, TestpressModule.class})
public interface AppComponent {
    void inject(TestpressApplication testpressApplication);
    void inject(LoginActivity loginActivity);
    void inject(MainActivity mainActivity);
    void inject(RegisterActivity registerActivity);
    void inject(CodeVerificationActivity codeVerificationActivity);
    void inject(OrdersListActivity ordersListActivity);
    void inject(OrdersListFragment ordersListFragment);
    void inject(PostActivity postActivity);
    void inject(PostsListFragment postsListFragment);
    void inject(ProfileDetailsActivity profileDetailsActivity);
    void inject(ProfilePhotoActivity profilePhotoActivity);
    void inject(MainMenuFragment mainMenuFragment);
    void inject(ResetPasswordActivity resetPasswordActivity);
    void inject(DocumentsListActivity documentsListActivity);
    void inject(DocumentsListFragment documentsListFragment);
    void inject(SplashScreenActivity splashScreenActivity);
    void inject(DrupalRssListActivity drupalRssListActivity);
    void inject(DrupalRssListFragment drupalRssListFragment);
    void inject(RssFeedDetailActivity rssFeedDetailActivity);
    void inject(AccountActivateActivity accountActivateActivity);
    void inject(WebViewActivity webViewActivity);
    void inject(DashboardFragment dashboardFragment);
    void inject(DoubtsActivity doubtsActivity);
    void inject(LoginActivityV2 loginActivityV2);
    void inject(UsernameAuthentication usernameAuthentication);
    void inject(PhoneAuthenticationFragment phoneAuthenticationFragment);
    void inject(OTPVerificationFragment otpVerificationFragment);
    void inject(TermsAndConditionActivity termsAndConditionActivity);
    void inject(AccountDeleteActivity accountDeleteActivity);
    void inject(EnforceDataActivity enforceDataActivity);
    void inject(TestpressFragmentActivity testpressFragmentActivity);
    void inject(BaseAuthenticatedActivity baseAuthenticatedActivity);
    void inject(TestpressActivity testpressActivity);
}
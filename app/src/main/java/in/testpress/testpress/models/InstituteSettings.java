package in.testpress.testpress.models;

import in.testpress.util.IntegerList;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import in.testpress.testpress.BuildConfig;
import in.testpress.testpress.TestpressApplication;
import in.testpress.testpress.enums.VerificationMethod;
// KEEP INCLUDES END
/**
 * Entity mapped to table "INSTITUTE_SETTINGS".
 */
public class InstituteSettings {

    private String baseUrl;
    private String verificationMethod;
    private Boolean allowSignup;
    private Boolean forceStudentData;
    private Boolean removeTpBranding;
    private String url;
    private Boolean showGameFrontend;
    private Boolean coursesEnabled;
    private Boolean coursesEnableGamification;
    private String coursesLabel;
    private Boolean postsEnabled;
    private String postsLabel;
    private Boolean storeEnabled;
    private String storeLabel;
    private Boolean documentsEnabled;
    private String documentsLabel;
    private Boolean resultsEnabled;
    private Boolean dashboardEnabled;
    private Boolean facebookLoginEnabled;
    private Boolean googleLoginEnabled;
    private boolean commentsVotingEnabled;
    private Boolean bookmarksEnabled;
    private Boolean forumEnabled;
    private String forumLabel;
    private Boolean twilioEnabled;
    private Boolean allow_profile_edit;
    private String learnLabel;
    private String leaderboardLabel;
    private String dashboardLabel;
    private String bookmarksLabel;
    private String loginLabel;
    private String loginPasswordLabel;
    private String aboutUs;
    private Boolean disableStudentAnalytics;
    private Boolean customRegistrationEnabled;
    private Boolean enableParallelLoginRestriction;
    private Integer maxParallelLogins;
    private Integer lockoutLimit;
    private String cooloffTime;
    private String appToolbarLogo;
    private String appShareLink;
    private String serverTime;
    private Boolean allowScreenshotInApp;
    private String androidSentryDns;
    private Boolean leaderboardEnabled;
    private String threatsAndTargetsLabel;
    private Boolean isVideoDownloadEnabled;
    private Boolean isHelpdeskEnabled;
    private IntegerList allowedLoginMethods;
    private Boolean showShareButton;
    private String facebookAppId;
    private Integer maxAllowedDownloadedVideos;
    private Boolean disableForgotPassword;
    private Boolean disableStudentReport;
    private Boolean enableCustomTest;
    private String currentPaymentApp;
    private String customRegistrationUrl;
    private Boolean disableStoreInApp;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public InstituteSettings() {
    }

    public InstituteSettings(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public InstituteSettings(String baseUrl, String verificationMethod, Boolean allowSignup, Boolean forceStudentData, Boolean removeTpBranding, String url, Boolean showGameFrontend, Boolean coursesEnabled, Boolean coursesEnableGamification, String coursesLabel, Boolean postsEnabled, String postsLabel, Boolean storeEnabled, String storeLabel, Boolean documentsEnabled, String documentsLabel, Boolean resultsEnabled, Boolean dashboardEnabled, Boolean facebookLoginEnabled, Boolean googleLoginEnabled, boolean commentsVotingEnabled, Boolean bookmarksEnabled, Boolean forumEnabled, String forumLabel, Boolean twilioEnabled, Boolean allow_profile_edit, String learnLabel, String leaderboardLabel, String dashboardLabel, String bookmarksLabel, String loginLabel, String loginPasswordLabel, String aboutUs, Boolean disableStudentAnalytics, Boolean customRegistrationEnabled, Boolean enableParallelLoginRestriction, Integer maxParallelLogins, Integer lockoutLimit, String cooloffTime, String appToolbarLogo, String appShareLink, String serverTime, Boolean allowScreenshotInApp, String androidSentryDns, Boolean leaderboardEnabled, String threatsAndTargetsLabel, Boolean isVideoDownloadEnabled, Boolean isHelpdeskEnabled, IntegerList allowedLoginMethods, Boolean showShareButton, String facebookAppId, Integer maxAllowedDownloadedVideos, Boolean disableForgotPassword, Boolean disableStudentReport, Boolean enableCustomTest, String currentPaymentApp, String customRegistrationUrl, Boolean disableStoreInApp) {
        this.baseUrl = baseUrl;
        this.verificationMethod = verificationMethod;
        this.allowSignup = allowSignup;
        this.forceStudentData = forceStudentData;
        this.removeTpBranding = removeTpBranding;
        this.url = url;
        this.showGameFrontend = showGameFrontend;
        this.coursesEnabled = coursesEnabled;
        this.coursesEnableGamification = coursesEnableGamification;
        this.coursesLabel = coursesLabel;
        this.postsEnabled = postsEnabled;
        this.postsLabel = postsLabel;
        this.storeEnabled = storeEnabled;
        this.storeLabel = storeLabel;
        this.documentsEnabled = documentsEnabled;
        this.documentsLabel = documentsLabel;
        this.resultsEnabled = resultsEnabled;
        this.dashboardEnabled = dashboardEnabled;
        this.facebookLoginEnabled = facebookLoginEnabled;
        this.googleLoginEnabled = googleLoginEnabled;
        this.commentsVotingEnabled = commentsVotingEnabled;
        this.bookmarksEnabled = bookmarksEnabled;
        this.forumEnabled = forumEnabled;
        this.forumLabel = forumLabel;
        this.twilioEnabled = twilioEnabled;
        this.allow_profile_edit = allow_profile_edit;
        this.learnLabel = learnLabel;
        this.leaderboardLabel = leaderboardLabel;
        this.dashboardLabel = dashboardLabel;
        this.bookmarksLabel = bookmarksLabel;
        this.loginLabel = loginLabel;
        this.loginPasswordLabel = loginPasswordLabel;
        this.aboutUs = aboutUs;
        this.disableStudentAnalytics = disableStudentAnalytics;
        this.customRegistrationEnabled = customRegistrationEnabled;
        this.enableParallelLoginRestriction = enableParallelLoginRestriction;
        this.maxParallelLogins = maxParallelLogins;
        this.lockoutLimit = lockoutLimit;
        this.cooloffTime = cooloffTime;
        this.appToolbarLogo = appToolbarLogo;
        this.appShareLink = appShareLink;
        this.serverTime = serverTime;
        this.allowScreenshotInApp = allowScreenshotInApp;
        this.androidSentryDns = androidSentryDns;
        this.leaderboardEnabled = leaderboardEnabled;
        this.threatsAndTargetsLabel = threatsAndTargetsLabel;
        this.isVideoDownloadEnabled = isVideoDownloadEnabled;
        this.isHelpdeskEnabled = isHelpdeskEnabled;
        this.allowedLoginMethods = allowedLoginMethods;
        this.showShareButton = showShareButton;
        this.facebookAppId = facebookAppId;
        this.maxAllowedDownloadedVideos = maxAllowedDownloadedVideos;
        this.disableForgotPassword = disableForgotPassword;
        this.disableStudentReport = disableStudentReport;
        this.enableCustomTest = enableCustomTest;
        this.currentPaymentApp = currentPaymentApp;
        this.customRegistrationUrl = customRegistrationUrl;
        this.disableStoreInApp = disableStoreInApp;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public Boolean getAllowSignup() {
        return allowSignup;
    }

    public void setAllowSignup(Boolean allowSignup) {
        this.allowSignup = allowSignup;
    }

    public Boolean getForceStudentData() {
        return forceStudentData;
    }

    public void setForceStudentData(Boolean forceStudentData) {
        this.forceStudentData = forceStudentData;
    }

    public Boolean getRemoveTpBranding() {
        return removeTpBranding;
    }

    public void setRemoveTpBranding(Boolean removeTpBranding) {
        this.removeTpBranding = removeTpBranding;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getShowGameFrontend() {
        return showGameFrontend;
    }

    public void setShowGameFrontend(Boolean showGameFrontend) {
        this.showGameFrontend = showGameFrontend;
    }

    public Boolean getCoursesEnabled() {
        return coursesEnabled;
    }

    public void setCoursesEnabled(Boolean coursesEnabled) {
        this.coursesEnabled = coursesEnabled;
    }

    public Boolean getCoursesEnableGamification() {
        return coursesEnableGamification;
    }

    public void setCoursesEnableGamification(Boolean coursesEnableGamification) {
        this.coursesEnableGamification = coursesEnableGamification;
    }

    public String getCoursesLabel() {
        return coursesLabel;
    }

    public void setCoursesLabel(String coursesLabel) {
        this.coursesLabel = coursesLabel;
    }

    public Boolean getPostsEnabled() {
        return postsEnabled;
    }

    public void setPostsEnabled(Boolean postsEnabled) {
        this.postsEnabled = postsEnabled;
    }

    public String getPostsLabel() {
        return postsLabel;
    }

    public void setPostsLabel(String postsLabel) {
        this.postsLabel = postsLabel;
    }

    public Boolean getStoreEnabled() {
        return storeEnabled;
    }

    public void setStoreEnabled(Boolean storeEnabled) {
        this.storeEnabled = storeEnabled;
    }

    public String getStoreLabel() {
        return storeLabel;
    }

    public void setStoreLabel(String storeLabel) {
        this.storeLabel = storeLabel;
    }

    public Boolean getDocumentsEnabled() {
        return documentsEnabled;
    }

    public void setDocumentsEnabled(Boolean documentsEnabled) {
        this.documentsEnabled = documentsEnabled;
    }

    public String getDocumentsLabel() {
        return documentsLabel;
    }

    public void setDocumentsLabel(String documentsLabel) {
        this.documentsLabel = documentsLabel;
    }

    public Boolean getResultsEnabled() {
        return resultsEnabled;
    }

    public void setResultsEnabled(Boolean resultsEnabled) {
        this.resultsEnabled = resultsEnabled;
    }

    public Boolean getDashboardEnabled() {
        return dashboardEnabled;
    }

    public void setDashboardEnabled(Boolean dashboardEnabled) {
        this.dashboardEnabled = dashboardEnabled;
    }

    public Boolean getFacebookLoginEnabled() {
        return facebookLoginEnabled;
    }

    public void setFacebookLoginEnabled(Boolean facebookLoginEnabled) {
        this.facebookLoginEnabled = facebookLoginEnabled;
    }

    public Boolean getGoogleLoginEnabled() {
        return googleLoginEnabled;
    }

    public void setGoogleLoginEnabled(Boolean googleLoginEnabled) {
        this.googleLoginEnabled = googleLoginEnabled;
    }

    public boolean getCommentsVotingEnabled() {
        return commentsVotingEnabled;
    }

    public void setCommentsVotingEnabled(boolean commentsVotingEnabled) {
        this.commentsVotingEnabled = commentsVotingEnabled;
    }

    public Boolean getBookmarksEnabled() {
        return bookmarksEnabled;
    }

    public void setBookmarksEnabled(Boolean bookmarksEnabled) {
        this.bookmarksEnabled = bookmarksEnabled;
    }

    public Boolean getForumEnabled() {
        return forumEnabled;
    }

    public void setForumEnabled(Boolean forumEnabled) {
        this.forumEnabled = forumEnabled;
    }

    public String getForumLabel() {
        return forumLabel;
    }

    public void setForumLabel(String forumLabel) {
        this.forumLabel = forumLabel;
    }

    public Boolean getTwilioEnabled() {
        return twilioEnabled;
    }

    public void setTwilioEnabled(Boolean twilioEnabled) {
        this.twilioEnabled = twilioEnabled;
    }

    public Boolean getAllow_profile_edit() {
        return allow_profile_edit;
    }

    public void setAllow_profile_edit(Boolean allow_profile_edit) {
        this.allow_profile_edit = allow_profile_edit;
    }

    public String getLearnLabel() {
        return learnLabel;
    }

    public void setLearnLabel(String learnLabel) {
        this.learnLabel = learnLabel;
    }

    public String getLeaderboardLabel() {
        return leaderboardLabel;
    }

    public void setLeaderboardLabel(String leaderboardLabel) {
        this.leaderboardLabel = leaderboardLabel;
    }

    public String getDashboardLabel() {
        return dashboardLabel;
    }

    public void setDashboardLabel(String dashboardLabel) {
        this.dashboardLabel = dashboardLabel;
    }

    public String getBookmarksLabel() {
        return bookmarksLabel;
    }

    public void setBookmarksLabel(String bookmarksLabel) {
        this.bookmarksLabel = bookmarksLabel;
    }

    public String getLoginLabel() {
        return loginLabel;
    }

    public void setLoginLabel(String loginLabel) {
        this.loginLabel = loginLabel;
    }

    public String getLoginPasswordLabel() {
        return loginPasswordLabel;
    }

    public void setLoginPasswordLabel(String loginPasswordLabel) {
        this.loginPasswordLabel = loginPasswordLabel;
    }

    public String getAboutUs() {
        return aboutUs;
    }

    public void setAboutUs(String aboutUs) {
        this.aboutUs = aboutUs;
    }

    public Boolean getDisableStudentAnalytics() {
        return disableStudentAnalytics;
    }

    public void setDisableStudentAnalytics(Boolean disableStudentAnalytics) {
        this.disableStudentAnalytics = disableStudentAnalytics;
    }

    public Boolean getCustomRegistrationEnabled() {
        return customRegistrationEnabled;
    }

    public void setCustomRegistrationEnabled(Boolean customRegistrationEnabled) {
        this.customRegistrationEnabled = customRegistrationEnabled;
    }

    public Boolean getEnableParallelLoginRestriction() {
        return enableParallelLoginRestriction;
    }

    public void setEnableParallelLoginRestriction(Boolean enableParallelLoginRestriction) {
        this.enableParallelLoginRestriction = enableParallelLoginRestriction;
    }

    public Integer getMaxParallelLogins() {
        return maxParallelLogins;
    }

    public void setMaxParallelLogins(Integer maxParallelLogins) {
        this.maxParallelLogins = maxParallelLogins;
    }

    public Integer getLockoutLimit() {
        return lockoutLimit;
    }

    public void setLockoutLimit(Integer lockoutLimit) {
        this.lockoutLimit = lockoutLimit;
    }

    public String getCooloffTime() {
        return cooloffTime;
    }

    public void setCooloffTime(String cooloffTime) {
        this.cooloffTime = cooloffTime;
    }

    public String getAppToolbarLogo() {
        return appToolbarLogo;
    }

    public void setAppToolbarLogo(String appToolbarLogo) {
        this.appToolbarLogo = appToolbarLogo;
    }

    public String getAppShareLink() {
        return appShareLink;
    }

    public void setAppShareLink(String appShareLink) {
        this.appShareLink = appShareLink;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public Boolean getAllowScreenshotInApp() {
        return allowScreenshotInApp;
    }

    public void setAllowScreenshotInApp(Boolean allowScreenshotInApp) {
        this.allowScreenshotInApp = allowScreenshotInApp;
    }

    public String getAndroidSentryDns() {
        return androidSentryDns;
    }

    public void setAndroidSentryDns(String androidSentryDns) {
        this.androidSentryDns = androidSentryDns;
    }

    public Boolean getLeaderboardEnabled() {
        return leaderboardEnabled;
    }

    public void setLeaderboardEnabled(Boolean leaderboardEnabled) {
        this.leaderboardEnabled = leaderboardEnabled;
    }

    public String getThreatsAndTargetsLabel() {
        return threatsAndTargetsLabel;
    }

    public void setThreatsAndTargetsLabel(String threatsAndTargetsLabel) {
        this.threatsAndTargetsLabel = threatsAndTargetsLabel;
    }

    public Boolean getIsVideoDownloadEnabled() {
        return isVideoDownloadEnabled;
    }

    public void setIsVideoDownloadEnabled(Boolean isVideoDownloadEnabled) {
        this.isVideoDownloadEnabled = isVideoDownloadEnabled;
    }

    public Boolean getIsHelpdeskEnabled() {
        return isHelpdeskEnabled;
    }

    public void setIsHelpdeskEnabled(Boolean isHelpdeskEnabled) {
        this.isHelpdeskEnabled = isHelpdeskEnabled;
    }

    public IntegerList getAllowedLoginMethods() {
        return allowedLoginMethods;
    }

    public void setAllowedLoginMethods(IntegerList allowedLoginMethods) {
        this.allowedLoginMethods = allowedLoginMethods;
    }

    public Boolean getShowShareButton() {
        return showShareButton;
    }

    public void setShowShareButton(Boolean showShareButton) {
        this.showShareButton = showShareButton;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public Integer getMaxAllowedDownloadedVideos() {
        return maxAllowedDownloadedVideos;
    }

    public void setMaxAllowedDownloadedVideos(Integer maxAllowedDownloadedVideos) {
        this.maxAllowedDownloadedVideos = maxAllowedDownloadedVideos;
    }

    public Boolean getDisableForgotPassword() {
        return disableForgotPassword;
    }

    public void setDisableForgotPassword(Boolean disableForgotPassword) {
        this.disableForgotPassword = disableForgotPassword;
    }

    public Boolean getDisableStudentReport() {
        return disableStudentReport;
    }

    public void setDisableStudentReport(Boolean disableStudentReport) {
        this.disableStudentReport = disableStudentReport;
    }

    public Boolean getEnableCustomTest() {
        return enableCustomTest;
    }

    public void setEnableCustomTest(Boolean enableCustomTest) {
        this.enableCustomTest = enableCustomTest;
    }

    public String getCurrentPaymentApp() {
        return currentPaymentApp;
    }

    public void setCurrentPaymentApp(String currentPaymentApp) {
        this.currentPaymentApp = currentPaymentApp;
    }

    public String getCustomRegistrationUrl() {
        return customRegistrationUrl;
    }

    public void setCustomRegistrationUrl(String customRegistrationUrl) {
        this.customRegistrationUrl = customRegistrationUrl;
    }

    public Boolean getDisableStoreInApp() {
        return disableStoreInApp;
    }

    public void setDisableStoreInApp(Boolean disableStoreInApp) {
        this.disableStoreInApp = disableStoreInApp;
    }

    // KEEP METHODS - put your custom methods here
    public long serverTime() {
        if (serverTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                return dateFormat.parse(serverTime).getTime();
            } catch (ParseException e) {
                return 0;
            }
        }
        return 0L;
    }

    public static InstituteSettings getInstance() {
        InstituteSettingsDao instituteSettingsDao = TestpressApplication.getDaoSession().getInstituteSettingsDao();
        List<InstituteSettings> instituteSettingList = instituteSettingsDao.queryBuilder()
                .where(InstituteSettingsDao.Properties.BaseUrl.eq(BuildConfig.BASE_URL))
                .list();
        if (instituteSettingList.size() == 0) {
            return null;
        }
        return instituteSettingList.get(0);
    }

    public VerificationMethod getVerificationType() {
        if(this.getVerificationMethod().equals("E")) {
            return VerificationMethod.EMAIL;
        } else if (this.getVerificationMethod().equals("M")) {
            return VerificationMethod.MOBILE;
        } else {
            return VerificationMethod.NONE;
        }
    }

    public boolean isStudentReportEnabled() {
        return Boolean.TRUE.equals(this.getCoursesEnabled()) && Boolean.FALSE.equals(this.getDisableStudentReport());
    }
    // KEEP METHODS END

}

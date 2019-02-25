package in.testpress.testpress.models;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
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
    private Boolean twilioEnabled;
    private Boolean allow_profile_edit;
    private String learnLabel;
    private String leaderboardLabel;
    private String dashboardLabel;
    private String bookmarksLabel;
    private String loginLabel;
    private String loginPasswordLabel;

    public InstituteSettings() {
    }

    public InstituteSettings(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public InstituteSettings(String baseUrl, String verificationMethod, Boolean allowSignup, Boolean forceStudentData, Boolean removeTpBranding, String url, Boolean showGameFrontend, Boolean coursesEnabled, Boolean coursesEnableGamification, String coursesLabel, Boolean postsEnabled, String postsLabel, Boolean storeEnabled, String storeLabel, Boolean documentsEnabled, String documentsLabel, Boolean resultsEnabled, Boolean dashboardEnabled, Boolean facebookLoginEnabled, Boolean googleLoginEnabled, boolean commentsVotingEnabled, Boolean bookmarksEnabled, Boolean forumEnabled, Boolean twilioEnabled, Boolean allow_profile_edit, String learnLabel, String leaderboardLabel, String dashboardLabel, String bookmarksLabel, String loginLabel, String loginPasswordLabel) {
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
        this.twilioEnabled = twilioEnabled;
        this.allow_profile_edit = allow_profile_edit;
        this.learnLabel = learnLabel;
        this.leaderboardLabel = leaderboardLabel;
        this.dashboardLabel = dashboardLabel;
        this.bookmarksLabel = bookmarksLabel;
        this.loginLabel = loginLabel;
        this.loginPasswordLabel = loginPasswordLabel;
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

}

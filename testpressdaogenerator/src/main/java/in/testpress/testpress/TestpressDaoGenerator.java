package in.testpress.testpress;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class TestpressDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(19, "in.testpress.testpress.models");

        Entity post = schema.addEntity("Post");
        post.addLongProperty("id").primaryKey();
        post.addStringProperty("title");
        post.addStringProperty("summary");
        post.addStringProperty("contentHtml");
        post.addStringProperty("url");
        post.addStringProperty("publishedDate");
        post.addLongProperty("published");
        post.addStringProperty("modified");
        post.addIntProperty("institute");
        post.addBooleanProperty("is_active");
        post.addLongProperty("modifiedDate");
        post.addStringProperty("short_web_url");
        post.addStringProperty("short_url");
        post.addStringProperty("shortLink");
        post.addStringProperty("web_url");
        post.addIntProperty("commentsCount");
        post.addStringProperty("commentsUrl");
        post.addStringProperty("coverImage");

        Entity category = schema.addEntity("Category");
        category.addLongProperty("id").primaryKey();
        category.addStringProperty("name");
        category.addStringProperty("color");
        category.addStringProperty("slug");

        Property categoryId = post.addLongProperty("categoryId").getProperty();
        post.addToOne(category, categoryId);

        Entity instituteSettings = schema.addEntity("InstituteSettings");
        instituteSettings.addStringProperty("baseUrl").primaryKey();
        instituteSettings.addStringProperty("verificationMethod");
        instituteSettings.addBooleanProperty("allowSignup");
        instituteSettings.addBooleanProperty("forceStudentData");
        instituteSettings.addBooleanProperty("removeTpBranding");
        instituteSettings.addStringProperty("url");
        instituteSettings.addBooleanProperty("showGameFrontend");
        instituteSettings.addBooleanProperty("coursesEnabled");
        instituteSettings.addBooleanProperty("coursesEnableGamification");
        instituteSettings.addStringProperty("coursesLabel");
        instituteSettings.addBooleanProperty("postsEnabled");
        instituteSettings.addStringProperty("postsLabel");
        instituteSettings.addBooleanProperty("storeEnabled");
        instituteSettings.addStringProperty("storeLabel");
        instituteSettings.addBooleanProperty("documentsEnabled");
        instituteSettings.addStringProperty("documentsLabel");
        instituteSettings.addBooleanProperty("resultsEnabled");
        instituteSettings.addBooleanProperty("dashboardEnabled");
        instituteSettings.addBooleanProperty("facebookLoginEnabled");
        instituteSettings.addBooleanProperty("googleLoginEnabled");
        instituteSettings.addBooleanProperty("commentsVotingEnabled").notNull();
        instituteSettings.addBooleanProperty("bookmarksEnabled");
        instituteSettings.addBooleanProperty("forumEnabled");
        instituteSettings.addBooleanProperty("twilioEnabled");
        instituteSettings.addBooleanProperty("allow_profile_edit");
        instituteSettings.addStringProperty("learnLabel");
        instituteSettings.addStringProperty("leaderboardLabel");
        instituteSettings.addStringProperty("dashboardLabel");
        instituteSettings.addStringProperty("bookmarksLabel");
        instituteSettings.addStringProperty("loginLabel");
        instituteSettings.addStringProperty("loginPasswordLabel");
        instituteSettings.addStringProperty("aboutUs");
        instituteSettings.addBooleanProperty("disableStudentAnalytics");
        instituteSettings.addBooleanProperty("customRegistrationEnabled");
        instituteSettings.addBooleanProperty("enableParallelLoginRestriction");
        instituteSettings.addIntProperty("maxParallelLogins");
        instituteSettings.addIntProperty("lockoutLimit");
        instituteSettings.addStringProperty("cooloffTime");

        Entity rssFeed = schema.addEntity("RssItem");
        rssFeed.addLongProperty("id").primaryKey().autoincrement();
        rssFeed.addStringProperty("title");
        rssFeed.addStringProperty("link").unique();
        rssFeed.addStringProperty("image");
        rssFeed.addLongProperty("publishDate");
        rssFeed.addStringProperty("description");

        Entity forum = addForum(schema);
        Entity user = addUser(schema);

        addUserToForum(forum, user, "createdBy", "creatorId");
        addUserToForum(forum, user, "lastCommentedBy", "commentorId");
        addCategoryToForum(forum, category);

        Entity leaderboardItem = addLeaderboardItem(schema);
        addUserToLeaderboardItem(leaderboardItem, user);
        addBanner(schema);
        addDashboardSections(schema);
        addUserStats(schema);
        addUserStatDates(schema);


        new DaoGenerator().generateAll(schema, "app/src/main/java/");
    }

    private static Entity addUserStatDates(Schema schema) {
        Entity userStatDates = schema.addEntity("UserStatDates");
        userStatDates.addStringProperty("monthStart");
        userStatDates.addStringProperty("monthEnd");
        userStatDates.addStringProperty("yearStart");
        userStatDates.addStringProperty("yearEnd");
        userStatDates.addStringProperty("weekStart");
        userStatDates.addStringProperty("weekEnd");
        return userStatDates;
    }

    private static Entity addUserStats(Schema schema) {
        Entity userStats = schema.addEntity("UserStats");
        userStats.addLongProperty("id").primaryKey();
        userStats.addStringProperty("dateFrom");
        userStats.addIntProperty("attemptsCount");
        userStats.addIntProperty("attemptsCountDifference");
        userStats.addStringProperty("videoWatchedDuration");
        userStats.addStringProperty("videoWatchedDurationDifference");
        userStats.addStringProperty("category");
        return userStats;
    }

    private static Entity addLeaderboardItem(Schema schema) {
        Entity leaderboardItem = schema.addEntity("LeaderboardItem");
        leaderboardItem.addLongProperty("id").primaryKey();
        leaderboardItem.addStringProperty("trophiesCount");
        leaderboardItem.addIntProperty("difference");
        leaderboardItem.addIntProperty("category");
        return leaderboardItem;
    }

    private static void addUserToLeaderboardItem(Entity leaderboardItem, Entity user) {
        Property userId = leaderboardItem.addLongProperty("userId").getProperty();
        leaderboardItem.addToOne(user, userId, "user");
    }

    private static Entity addDashboardSections(Schema schema) {
        Entity dashboardSection = schema.addEntity("DashboardSection");
        dashboardSection.addStringProperty("slug").primaryKey();
        dashboardSection.addStringProperty("displayName");
        dashboardSection.addStringProperty("url");
        dashboardSection.addStringProperty("contentType");
        dashboardSection.addStringProperty("order");
        dashboardSection.addStringProperty("displayType");
        dashboardSection.addStringProperty("items").customType(
                "in.testpress.util.IntegerList",
                "in.testpress.util.IntegerListConverter"
        );
        return dashboardSection;
    }

    private static Entity addBanner(Schema schema) {
        Entity banner = schema.addEntity("Banner");
        banner.addLongProperty("id").primaryKey();
        banner.addStringProperty("url");
        banner.addStringProperty("image");
        return banner;
    }

    public static void addUserToForum(Entity forum, Entity user, String name, String key) {
        Property userId = forum.addLongProperty(key).getProperty();
        forum.addToOne(user, userId, name);
    }

    public static void addCategoryToForum(Entity forum, Entity category) {
        Property categoryId = forum.addLongProperty("categoryId").getProperty();
        forum.addToOne(category, categoryId, "category");
    }

    public static Entity addForum(Schema schema) {
        Entity forum = schema.addEntity("Forum");
        forum.addStringProperty("shortWebUrl");
        forum.addStringProperty("shortUrl");
        forum.addStringProperty("webUrl");
        forum.addStringProperty("created");
        forum.addStringProperty("commentsUrl");
        forum.addStringProperty("url");
        forum.addLongProperty("id").primaryKey();
        forum.addStringProperty("modified");
        forum.addIntProperty("upvotes");
        forum.addIntProperty("downvotes");
        forum.addStringProperty("title");
        forum.addStringProperty("summary");
        forum.addBooleanProperty("isActive");
        forum.addStringProperty("publishedDate");
        forum.addIntProperty("commentsCount");
        forum.addBooleanProperty("isLocked");
        forum.addIntProperty("subject");
        forum.addIntProperty("viewsCount");
        forum.addIntProperty("participantsCount");
        forum.addStringProperty("lastCommentedTime");
        forum.addStringProperty("contentHtml");
        forum.addBooleanProperty("isPublic");
        forum.addStringProperty("shortLink");
        forum.addIntProperty("institute");
        forum.addStringProperty("slug");
        forum.addBooleanProperty("isPublished");
        forum.addBooleanProperty("isApproved");
        forum.addBooleanProperty("forum");
        forum.addStringProperty("ipAddress");
        forum.addLongProperty("voteId");
        forum.addIntProperty("typeOfVote");
        forum.addLongProperty("published");
        forum.addLongProperty("modifiedDate");
        return forum;
    }

    public static Entity addUser(Schema schema) {
        Entity user = schema.addEntity("User");
        user.addLongProperty("id").primaryKey();
        user.addStringProperty("url");
        user.addStringProperty("username");
        user.addStringProperty("firstName");
        user.addStringProperty("lastName");
        user.addStringProperty("displayName");
        user.addStringProperty("photo");
        user.addStringProperty("largeImage");
        user.addStringProperty("mediumImage");
        user.addStringProperty("mediumSmallImage");
        user.addStringProperty("smallImage");
        user.addStringProperty("xSmallImage");
        user.addStringProperty("miniImage");
        user.addIntProperty("followers_count");
        user.addIntProperty("following_count");
        user.addIntProperty("following");
        return user;
    }
}

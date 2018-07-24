package in.testpress.testpress;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class TestpressDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(8, "in.testpress.testpress.models");

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
        post.addStringProperty("web_url");
        post.addIntProperty("commentsCount");
        post.addStringProperty("commentsUrl");

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

        Entity rssFeed = schema.addEntity("RssItem");
        rssFeed.addLongProperty("id").primaryKey().autoincrement();
        rssFeed.addStringProperty("title");
        rssFeed.addStringProperty("link").unique();
        rssFeed.addStringProperty("image");
        rssFeed.addLongProperty("publishDate");
        rssFeed.addStringProperty("description");

        new DaoGenerator().generateAll(schema, "app/src/main/java/");
    }
}

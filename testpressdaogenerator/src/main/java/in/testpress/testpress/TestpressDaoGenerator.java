package in.testpress.testpress;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class TestpressDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "in.testpress.testpress.models");

        Entity post = schema.addEntity("Post");
        post.addLongProperty("id").primaryKey();
        post.addStringProperty("title");
        post.addStringProperty("summary");
        post.addStringProperty("contentHtml");
        post.addStringProperty("url");
        post.addStringProperty("created");
        post.addLongProperty("createdDate");
        post.addStringProperty("modified");
        post.addIntProperty("institute");
        post.addBooleanProperty("active");

        Entity category = schema.addEntity("Category");
        category.addLongProperty("id").primaryKey();
        category.addStringProperty("name");
        category.addStringProperty("color");
        category.addStringProperty("slug");

        Property categoryId = post.addLongProperty("categoryId").getProperty();
        post.addToOne(category, categoryId);

        Entity session = schema.addEntity("Session");
        session.addIdProperty();
        session.addLongProperty("created");
        session.addStringProperty("state");
        session.addStringProperty("latestPostReceived");
        session.addStringProperty("oldestPostReceived");
        session.addStringProperty("last_synced_date");

        new DaoGenerator().generateAll(schema, "../app/src/main/java/");
    }
}

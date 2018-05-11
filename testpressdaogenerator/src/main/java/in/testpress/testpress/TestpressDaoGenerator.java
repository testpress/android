package in.testpress.testpress;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;

public class TestpressDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(8, "in.testpress.testpress.models");

        Entity post = addPost(schema);
        Entity category = addCategory(schema);
        //Adding category to the post
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

        Entity forum = addForum(schema);
        Entity user = addUser(schema);
        addUserToForum(forum, user, "createdBy", "creatorId");
        addUserToForum(forum, user, "lastCommentedBy", "commentorId");
        addCategoryToForum(forum, category);

        new DaoGenerator().generateAll(schema, "../app/src/main/java/");
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
        user.implementsInterface("Parcelable");
        return user;
    }

    private static Entity addPost(Schema schema) {
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
        post.addStringProperty("slug");
        post.implementsInterface("Parcelable");
        return post;
    }

    private static Entity addCategory(Schema schema) {
        Entity category = schema.addEntity("Category");
        category.addLongProperty("id").primaryKey();
        category.addStringProperty("name");
        category.addIntProperty("order");
        category.addStringProperty("color");
        category.addStringProperty("slug");
        category.addBooleanProperty("isStarred");
        category.implementsInterface("Parcelable");
        return category;
    }

    private static void addInstituteSettings(Schema schema) {
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
        instituteSettings.addBooleanProperty("commentsVotingEnabled");
    }

    private static Entity addActivity(Schema schema) {
        Entity activity = schema.addEntity("Activity");
        activity.addIntProperty("id");
        activity.addIntProperty("actorContentType");
        activity.addStringProperty("actorObjectId");
        activity.addIntProperty("targetContentType");
        activity.addStringProperty("targetObjectId");
        activity.addIntProperty("actionObjectContentType");
        activity.addStringProperty("actionObjectObjectId");
        activity.addStringProperty("timestamp");
        activity.addStringProperty("verb");
        return activity;
    }

    private static Entity addAttachmentContent(Schema schema) {
        Entity attachmentContent = schema.addEntity("AttachmentContent");
        attachmentContent.addIntProperty("id");
        attachmentContent.addStringProperty("title");
        attachmentContent.addStringProperty("description");
        return attachmentContent;
    }

    private static Entity addVideoContent(Schema schema) {
        Entity video = schema.addEntity("VideoContent");
        video.addStringProperty("title");
        video.addStringProperty("url");
        video.addLongProperty("id").primaryKey();
        video.addStringProperty("embedCode");
        video.implementsInterface("Parcelable");
        return video;
    }

    private static Entity addChapter(Schema schema) {
        Entity chapter = schema.addEntity("Chapter");
        chapter.addLongProperty("id").primaryKey();
        chapter.addStringProperty("name");
        chapter.addStringProperty("description");
        chapter.addStringProperty("slug");
        return chapter;
    }

    private static Entity addContentType(Schema schema) {
        Entity contentType = schema.addEntity("ContentType");
        contentType.addIntProperty("id");
        contentType.addStringProperty("appLabel");
        contentType.addStringProperty("model");
        return contentType;
    }

    private static Entity addHtmlContent(Schema schema) {
        Entity htmlContent = schema.addEntity("HtmlContent");
        htmlContent.addIntProperty("id");
        htmlContent.addStringProperty("title");
        htmlContent.addStringProperty("textHtml");
        return htmlContent;
    }

    private static Entity addExam(Schema schema) {
        Entity exam = schema.addEntity("Exam");
        exam.addLongProperty("id").primaryKey();
        exam.addStringProperty("title");
        exam.addStringProperty("duration");
        exam.addIntProperty("numberOfQuestions");
        exam.addIntProperty("studentsAttemptedCount");
        return exam;
    }

    private static Entity addAttempt(Schema schema) {
        Entity attempt = schema.addEntity("Attempt");
        attempt.addLongProperty("id").primaryKey();
        attempt.addStringProperty("score");
        attempt.addStringProperty("reviewPdfUrl");
        attempt.addIntProperty("correctCount");
        attempt.addIntProperty("incorrectCount");
        attempt.addStringProperty("timeTaken");
        attempt.addStringProperty("percentage");
        attempt.addIntProperty("unansweredCount");
        attempt.addIntProperty("speed");
        attempt.addIntProperty("accuracy");
        attempt.addIntProperty("exam");
        attempt.implementsInterface("Parcelable");
        return attempt;
    }

    private static Entity addChapterContentAttempt(Schema schema) {
        Entity chapterContentAttempt = schema.addEntity("ChapterContentAttempt");
        chapterContentAttempt.addIntProperty("id");
        return chapterContentAttempt;
    }


}

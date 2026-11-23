package in.testpress.testpress.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import in.testpress.testpress.R;
import in.testpress.testpress.ui.SplashScreenActivity;

import static in.testpress.testpress.BuildConfig.APPLICATION_ID;
import static in.testpress.testpress.BuildConfig.BASE_URL;

/**
 * Helper class to manage notification channels, and create notifications.
 *
 * References:
 * https://github.com/googlesamples/android-ActiveNotifications/blob/master/Application/src/main/java/com/example/android/activenotifications/ActiveNotificationsFragment.java
 * https://github.com/googlesamples/android-NotificationChannels/blob/master/Application/src/main/java/com/example/android/notificationchannels/NotificationHelper.java
 * https://developer.android.com/training/notify-user/group
 * https://stackoverflow.com/a/41520145/5134215
 */
public class NotificationHelper extends ContextWrapper {

    private static final String POSTS_NOTIFICATION_GROUP = APPLICATION_ID + ".posts";
    private static final String CONTENTS_NOTIFICATION_GROUP = APPLICATION_ID + ".contents";

    private static final int POSTS_NOTIFICATION_GROUP_SUMMARY_ID = 0;
    private static final int CONTENTS_NOTIFICATION_GROUP_SUMMARY_ID = 1;
    private static final int SUMMARY_REQUEST_CODE_BASE = 10000;

    public static final String POSTS_CHANNEL = "posts";
    public static final String CONTENTS_CHANNEL = "contents";

    private static final String KEY_NOTIFICATION_ID = "notificationId";

    private static NotificationHelper sHelper;

    private NotificationManager manager;
    private NotificationCompat.InboxStyle inboxStyle;

    public NotificationHelper(Context ctx) {
        super(ctx);
        manager = getManager(this);
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, POSTS_CHANNEL, R.string.posts_channel);
            createChannel(context, CONTENTS_CHANNEL, R.string.contents_channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createChannel(Context context, String channelId, int channelName) {
        NotificationManager manager = getManager(context);
        NotificationChannel contentsChannel = new NotificationChannel(channelId,
                context.getString(channelName), NotificationManager.IMPORTANCE_HIGH);
        contentsChannel.setLightColor(Color.GREEN);
        contentsChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(contentsChannel);
    }

    private int getSmallIcon() {
        return R.drawable.ic_stat_notification;
    }

    public static NotificationManager getManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void addNotification(Context context, String title, String content, String url) {
        addNotification(context, title, content, url, null);
    }

    public static void addNotification(Context context, String title, String content, String url, Bitmap image) {
        if (sHelper == null) {
            sHelper = new NotificationHelper(context);
        }
        sHelper.addNotification(title, content, url, image);
    }

    public void addNotification(String title, String content, String url) {
        addNotification(title, content, url, null);
    }

    public static String buildFullUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        url = url.trim();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return BASE_URL + (url.startsWith("/") ? url : "/" + url);
    }

    public static Uri normalizeUrl(String url) {
        String fullUrl = buildFullUrl(url);
        if (fullUrl == null) {
            return null;
        }
        try {
            return Uri.parse(fullUrl);
        } catch (Exception e) {
            return null;
        }
    }

    public static String resolveSummaryUrl(String urlPath) {
        if (urlPath == null || urlPath.trim().isEmpty()) {
            return null;
        }
        return buildFullUrl(urlPath);
    }

    public static Uri resolveSummaryUri(String urlPath) {
        return (urlPath != null) ? normalizeUrl(urlPath) : null;
    }

    public static class NotificationRouting {
        public final String channelId;
        public final String groupUrlPath;

        public NotificationRouting(String channelId, String groupUrlPath) {
            this.channelId = channelId;
            this.groupUrlPath = groupUrlPath;
        }
    }

    public static NotificationRouting getNotificationRouting(String url) {
        String firstSegment = getFirstPathSegment(url);
        if (firstSegment != null) {
            switch (firstSegment) {
                case "exams":
                case "chapters":
                    return new NotificationRouting(CONTENTS_CHANNEL, "/learn/");
                case "p":
                    return new NotificationRouting(POSTS_CHANNEL, "/posts/");
                default:
                    return new NotificationRouting(POSTS_CHANNEL, null);
            }
        }
        return new NotificationRouting(POSTS_CHANNEL, null);
    }

    public static String getFirstPathSegment(String url) {
        if (url == null) {
            return null;
        }
        String path = url.trim();
        int schemeEnd = path.indexOf("://");
        if (schemeEnd != -1) {
            path = path.substring(schemeEnd + 3);
            int slashIndex = path.indexOf('/');
            if (slashIndex == -1) {
                return null;
            }
            path = path.substring(slashIndex);
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex != -1) {
            path = path.substring(0, fragmentIndex);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isEmpty()) {
            return null;
        }
        int nextSlash = path.indexOf('/');
        return nextSlash != -1 ? path.substring(0, nextSlash) : path;
    }

    public void addNotification(String title, String content, String url, Bitmap image) {
        Uri uri = normalizeUrl(url);
        NotificationRouting routing = getNotificationRouting(url);
        String channelId = routing.channelId;
        String groupUrlPath = routing.groupUrlPath;

        int notificationId = getNewNotificationId();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                .setContentIntent(getPendingIntent(uri, notificationId, false));

        if (image != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .setBigContentTitle(title)
                    .setSummaryText(content));
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(content));
        }

        if (Build.VERSION.SDK_INT >= 16) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        if (channelId.equals(CONTENTS_CHANNEL)) {
            builder.setGroup(CONTENTS_NOTIFICATION_GROUP);
        } else {
            builder.setGroup(POSTS_NOTIFICATION_GROUP);
        }

        manager.notify(notificationId, builder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && channelId.equals(POSTS_CHANNEL)) {
            updateNotificationSummary(POSTS_CHANNEL, POSTS_NOTIFICATION_GROUP,
                    POSTS_NOTIFICATION_GROUP_SUMMARY_ID, 1, groupUrlPath);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                channelId.equals(CONTENTS_CHANNEL)) {

            updateNotificationSummary(CONTENTS_CHANNEL, CONTENTS_NOTIFICATION_GROUP,
                    CONTENTS_NOTIFICATION_GROUP_SUMMARY_ID, 3, groupUrlPath);
        }
    }

    private PendingIntent getPendingIntent(Uri uri) {
        return getPendingIntent(uri, 0, false);
    }

    private PendingIntent getPendingIntent(Uri uri, int notificationId) {
        return getPendingIntent(uri, notificationId, false);
    }

    private PendingIntent getPendingIntent(Uri uri, int notificationId, boolean isSummary) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        if (uri != null) {
            intent.setData(uri);
            intent.putExtra("destination_url", uri.toString());
            intent.putExtra("short_url", uri.toString());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, notificationId, intent, this.getPendingIntentFlags(isSummary));
    }

    private int getPendingIntentFlags(boolean isSummary) {
        int baseFlag = isSummary ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE | baseFlag;
        } else {
            return baseFlag;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateNotificationSummary(String channelId, String groupKey, int groupSummaryId,
                                           int notificationsThreshold, String urlPath) {

        int numberOfNotifications = getNumberOfNotifications(groupKey, groupSummaryId);
        if (numberOfNotifications > notificationsThreshold) {
            Uri uri = resolveSummaryUri(urlPath);
            String title = getString(R.string.notification_summary_content, numberOfNotifications);
            inboxStyle.setBigContentTitle(title);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(getSmallIcon())
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                    .setContentTitle(title)
                    .setContentText(getString(R.string.tap_to_open))
                    .setAutoCancel(true)
                    .setContentIntent(getPendingIntent(uri, SUMMARY_REQUEST_CODE_BASE + groupSummaryId, true))
                    .setStyle(inboxStyle)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    .setGroup(groupKey)
                    .setGroupSummary(true);

            Notification notification = builder.build();
            manager.notify(groupSummaryId, notification);
        } else {
            manager.cancel(groupSummaryId);
        }
    }

    /**
     * Retrieves a unique notification ID.
     */
    public int getNewNotificationId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int notificationId = prefs.getInt(KEY_NOTIFICATION_ID, 0);
        notificationId++;
        // incremented notificationId if its value is equal to group summary id
        // Post summary id is 0, since we already incremented the notificationId it will never to 0
        if (notificationId == CONTENTS_NOTIFICATION_GROUP_SUMMARY_ID) {
            notificationId++;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_NOTIFICATION_ID, notificationId);
        editor.apply();
        return notificationId;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private int getNumberOfNotifications(String groupKey, int groupSummaryId) {
        inboxStyle = new NotificationCompat.InboxStyle();
        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();
        int numberOfNotifications = 0;
        for (StatusBarNotification notification : activeNotifications) {
            int groupKeyStartIndex = notification.getGroupKey().lastIndexOf(":") + 1;
            String notificationGroupKey = notification.getGroupKey().substring(groupKeyStartIndex);
            if (notificationGroupKey.equals(groupKey) && notification.getId() != groupSummaryId) {
                inboxStyle.addLine(notification.getNotification().extras.getString("android.title"));
                numberOfNotifications++;
            }
        }
        return numberOfNotifications;
    }
}

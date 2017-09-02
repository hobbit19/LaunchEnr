package com.enrico.launcher3.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.hiddenapps.HiddenAppsUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NotificationsDotListener extends NotificationListenerService {

    public static boolean isServiceRunning;
    //hash map containing an hash set (to avoid duplicates) notification keys for x package
    private static Map<String, HashSet<String>> NOTIFICATION_KEYS = new HashMap<>();
    private Ranking mTempRanking = new Ranking();

    //determine if the package has notifications
    public static boolean hasNotifications(String packageName) {

        return NOTIFICATION_KEYS.get(packageName) != null && !NOTIFICATION_KEYS.get(packageName).isEmpty();
    }

    //add or remove notification key from the notifications keys array list for x package
    private static void addOrRemoveNotificationKey(StatusBarNotification sbn, boolean add) {

        String packageName = sbn.getPackageName();

        //use hash set to avoid duplicates
        HashSet<String> notificationKeys;

        if (NOTIFICATION_KEYS.get(packageName) != null) {

            //update the hash map if there's already an hash set for the package containing keys
            notificationKeys = NOTIFICATION_KEYS.get(packageName);

            if (add) {
                notificationKeys.add(sbn.getKey());
            } else {
                notificationKeys.remove(sbn.getKey());
            }

            NOTIFICATION_KEYS.put(packageName, notificationKeys);

        } else {

            //if there's no hash set create new one and add notification keys for the package
            notificationKeys = new HashSet<>();
            notificationKeys.add(sbn.getKey());
            NOTIFICATION_KEYS.put(packageName, notificationKeys);
        }
    }

    //remove the notification keys hash set for the x package from the hash map
    private void clearNotificationKeysForPackage(String packageName) {

        NOTIFICATION_KEYS.remove(packageName);

    }

    //clear the notifications keys hash map
    private void clearNotificationKeys() {

        NOTIFICATION_KEYS.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        isServiceRunning = true;

        return START_STICKY;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        isServiceRunning = true;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();

        isServiceRunning = false;
        clearNotificationKeys();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (NotificationUtils.isNotificationServiceEnabled(getBaseContext())) {

            //the service will be restarted if killed
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("dontBreakNotifications");
            sendBroadcast(broadcastIntent);
            isServiceRunning = true;
        } else {
            isServiceRunning = false;
            clearNotificationKeys();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if (NotificationUtils.isNotificationServiceEnabled(getBaseContext()) && !shouldBeFilteredOut(sbn) && !HiddenAppsUtils.isPackageHidden(getBaseContext(), sbn.getPackageName())) {

            updateNotificationStatus(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if (NotificationUtils.isNotificationServiceEnabled(getBaseContext()) && !shouldBeFilteredOut(sbn) && !HiddenAppsUtils.isPackageHidden(getBaseContext(), sbn.getPackageName())) {

            removeNotificationStatus(sbn);
        }
    }

    //update notification status and draw the dot only if there's no one already
    private void updateNotificationStatus(StatusBarNotification sbn) {

        if (sbn != null) {

            if (!hasNotifications(sbn.getPackageName())) {
                //reload the workspace only if there's no dot already
                LauncherAppState.getInstanceNoCreate().getModel().forceReload();
            }

            addOrRemoveNotificationKey(sbn, true);
        }
    }

    //remove key for removed notification
    private void removeNotificationStatus(StatusBarNotification sbn) {

        //get package name
        String packageName = sbn.getPackageName();

        //remove a notification key from the notification keys hash set for x package
        addOrRemoveNotificationKey(sbn, false);

        //check if the package has no notifications
        //if yes clear everything and reload the workspace to remove the dot
        if (!hasNotifications(packageName)) {

            clearNotificationKeysForPackage(packageName);

            //reload the workspace
            LauncherAppState.getInstanceNoCreate().getModel().forceReload();
        }
    }

    /**
     * Filter out notifications that don't have an intent
     * or are headers for grouped notifications.
     *
     * @see #shouldBeFilteredOut(StatusBarNotification)
     * credits: https://android.googlesource.com/platform/packages/apps/Launcher3/+/android-8.0.0_r4/src/com/android/launcher3/notification/NotificationListener.java
     */
    private boolean shouldBeFilteredOut(StatusBarNotification sbn) {

        getCurrentRanking().getRanking(sbn.getKey(), mTempRanking);
        if (Utilities.ATLEAST_OREO && !mTempRanking.canShowBadge()) {
            return true;
        }
        Notification notification = sbn.getNotification();
        if (Utilities.ATLEAST_OREO && mTempRanking.getChannel().getId().equals(NotificationChannel.DEFAULT_CHANNEL_ID)) {
            // Special filtering for the default, legacy "Miscellaneous" channel.
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
                return true;
            }
        } else if (!Utilities.ATLEAST_OREO && (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
            return true;
        }
        boolean isGroupHeader = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        boolean missingTitleAndText = TextUtils.isEmpty(title) && TextUtils.isEmpty(text);
        return (isGroupHeader || missingTitleAndText);
    }
}

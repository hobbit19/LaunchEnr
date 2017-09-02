package com.enrico.launcher3.notifications;

import android.content.Context;

import com.enrico.launcher3.Utilities;

/**
 * Created by Enrico on 12/08/2017.
 */

public class NotificationUtils {

    public static final String NOTIFICATIONS_KEY = "pref_notificationBadges";

    //is notification service enabled?
    public static boolean isNotificationServiceEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(NOTIFICATIONS_KEY,
                false);
    }
}

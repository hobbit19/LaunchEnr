package com.enrico.launcher3;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Set;

/**
 * Created by Enrico on 02/08/2017.
 */

public class PermissionUtils {

    static final int CONTACT_REQUEST_CODE = 25;
    public static final int CALL_REQUEST_CODE = 8;

    private static void askNotificationAccess(Activity activity) {

        String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

        if (android.os.Build.VERSION.SDK_INT >= 22) {
            ACTION_NOTIFICATION_LISTENER_SETTINGS = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFICATION_LISTENER_SETTINGS);
        activity.startActivity(intent);
    }

    public static void askForAdmin(Activity activity) {

        ComponentName mComponentName = new ComponentName(activity, AdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        activity.startActivity(intent);
    }

    public static void checkNotificationAccess(final Activity activity) {

        if (!checkNotificationListenerPermission(activity)) {

            askNotificationAccess(activity);
        }
    }

    //check permissions
    private static boolean checkNotificationListenerPermission(Activity activity) {

        Set<String> enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(activity);

        boolean isAccess = false;
        if (enabledListeners != null) {
            isAccess = enabledListeners.contains(activity.getPackageName());
        }

        return isAccess;
    }
}

package com.enrico.launcher3.authorizations;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import com.enrico.launcher3.AdminReceiver;
import com.enrico.launcher3.R;
import com.enrico.launcher3.notifications.NotificationsDotListener;

import java.util.Set;

/**
 * Created by Enrico on 02/08/2017.
 */

public class PermissionUtils {

    public static final int CONTACT_REQUEST_CODE = 25;
    public static final int CALL_REQUEST_CODE = 8;
    public static final int ADMIN_CODE = 666;
    static final int NOTIFICATION_CODE = 1988;

    static String[] getPermissionRequested(Activity activity, int requestCode) {

        String permissionRequest = "";
        String title = "";
        String rationale = "";

        switch (requestCode) {


            case CONTACT_REQUEST_CODE:
                permissionRequest = Manifest.permission.READ_CONTACTS;
                title = activity.getString(R.string.contactsPermissionTitle);
                rationale = activity.getString(R.string.contactsPermissionMessage);
                break;

            case CALL_REQUEST_CODE:

                permissionRequest = Manifest.permission.CALL_PHONE;
                title = activity.getString(R.string.phonePermissionTitle);
                rationale = activity.getString(R.string.phonePermissionMessage);
                break;

            case NOTIFICATION_CODE:
                permissionRequest = null;
                title = activity.getString(R.string.notificationAccessPermission);
                rationale = activity.getString(R.string.notificationAccessPermissionRatio);
                break;

            case ADMIN_CODE:
                permissionRequest = null;
                title = activity.getString(R.string.adminAccess);
                rationale = activity.getString(R.string.adminPermissionRatio);
                break;

        }

        return new String[]{permissionRequest, title, rationale};

    }

    public static void requestPermissionWithRationale(final Activity activity, final int requestCode) {

        Bundle bundle = new Bundle();
        bundle.putInt("requestCode", requestCode);

        FragmentManager fm = activity.getFragmentManager();
        PermissionDialog dialogFragment = new PermissionDialog();

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "permission");

    }

    static void askNotificationAccess(Activity activity) {

        String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

        if (android.os.Build.VERSION.SDK_INT >= 22) {
            ACTION_NOTIFICATION_LISTENER_SETTINGS = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_NOTIFICATION_LISTENER_SETTINGS);
        activity.startActivity(intent);
    }

    static void askForAdmin(Activity activity) {

        ComponentName mComponentName = new ComponentName(activity, AdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        activity.startActivity(intent);
    }

    public static void askForNotificationAccess(final Activity activity) {

        if (!checkNotificationListenerPermission(activity)) {

            requestPermissionWithRationale(activity, NOTIFICATION_CODE);

        } else {

            Intent notificationServiceIntent = new Intent(activity, NotificationsDotListener.class);
            activity.startService(notificationServiceIntent);
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

package com.enrico.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;

import com.enrico.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.List;

public abstract class LauncherAppsCompat {

    private final static Object sInstanceLock = new Object();
    private static LauncherAppsCompat sInstance;

    protected LauncherAppsCompat() {
    }

    public static LauncherAppsCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new LauncherAppsCompatVL(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    public abstract List<LauncherActivityInfoCompat> getActivityList(String packageName,
                                                                     UserHandle user);

    public abstract LauncherActivityInfoCompat resolveActivity(Intent intent,
                                                               UserHandle user);

    public abstract void startActivityForProfile(ComponentName component, UserHandle user,
                                                 Rect sourceBounds, Bundle opts);

    public abstract void showAppDetailsForProfile(ComponentName component, UserHandle user);

    public abstract void addOnAppsChangedCallback(OnAppsChangedCallbackCompat listener);

    public abstract boolean isPackageEnabledForProfile(String packageName, UserHandle user);

    public abstract boolean isActivityEnabledForProfile(ComponentName component,
                                                        UserHandle user);

    public interface OnAppsChangedCallbackCompat {
        void onPackageRemoved(String packageName, UserHandle user);

        void onPackageAdded(String packageName, UserHandle user);

        void onPackageChanged(String packageName, UserHandle user);

        void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing);

        void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing);

        void onPackagesSuspended(String[] packageNames, UserHandle user);

        void onPackagesUnsuspended(String[] packageNames, UserHandle user);

        void onShortcutsChanged(String packageName, List<ShortcutInfoCompat> shortcuts,
                                UserHandle user);
    }

}

package com.enrico.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

public class LauncherActivityInfoCompat {

    /**
     * Creates a LauncherActivityInfoCompat for the primary user.
     */
    private LauncherActivityInfo mLauncherActivityInfo;

    public LauncherActivityInfoCompat(LauncherActivityInfo info) {
        mLauncherActivityInfo = info;
    }

    public static LauncherActivityInfoCompat create(Context context, UserHandle user, Intent intent) {
        LauncherApps launcherApps = (LauncherApps) context.getSystemService("launcherapps");
        LauncherActivityInfo info = launcherApps.resolveActivity(intent, user);
        return new LauncherActivityInfoCompat(info);
    }

    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    public UserHandle getUser() {
        return mLauncherActivityInfo.getUser();
    }

    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density) {
        return mLauncherActivityInfo.getIcon(density);
    }

    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    public String getName() {
        return mLauncherActivityInfo.getName();
    }
}

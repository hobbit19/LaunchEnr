package com.enrico.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;

import com.enrico.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LauncherAppsCompatVL extends LauncherAppsCompat {

    private final Map<OnAppsChangedCallbackCompat, WrappedCallback> mCallbacks
            = new HashMap<>();
    private LauncherApps mLauncherApps;

    LauncherAppsCompatVL(Context context) {

        mLauncherApps = (LauncherApps) context.getSystemService("launcherapps");
    }

    public List<LauncherActivityInfoCompat> getActivityList(String packageName,
                                                            UserHandle user) {
        List<LauncherActivityInfo> list = mLauncherApps.getActivityList(packageName,
                user);
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList<LauncherActivityInfoCompat> compatList =
                new ArrayList<LauncherActivityInfoCompat>(list.size());
        for (LauncherActivityInfo info : list) {
            compatList.add(new LauncherActivityInfoCompat(info));
        }
        return compatList;
    }

    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandle user) {
        LauncherActivityInfo activity = mLauncherApps.resolveActivity(intent, user);
        if (activity != null) {
            return new LauncherActivityInfoCompat(activity);
        } else {
            return null;
        }
    }

    public void startActivityForProfile(ComponentName component, UserHandle user,
                                        Rect sourceBounds, Bundle opts) {
        mLauncherApps.startMainActivity(component, user, sourceBounds, opts);
    }

    public void showAppDetailsForProfile(ComponentName component, UserHandle user) {
        mLauncherApps.startAppDetailsActivity(component, user, null, null);
    }

    public void addOnAppsChangedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat callback) {
        WrappedCallback wrappedCallback = new WrappedCallback(callback);
        synchronized (mCallbacks) {
            mCallbacks.put(callback, wrappedCallback);
        }
        mLauncherApps.registerCallback(wrappedCallback);
    }

    public boolean isPackageEnabledForProfile(String packageName, UserHandle user) {
        return mLauncherApps.isPackageEnabled(packageName, user);
    }

    public boolean isActivityEnabledForProfile(ComponentName component, UserHandle user) {
        return mLauncherApps.isActivityEnabled(component, user);
    }

    private static class WrappedCallback extends LauncherApps.Callback {
        private LauncherAppsCompat.OnAppsChangedCallbackCompat mCallback;

        public WrappedCallback(LauncherAppsCompat.OnAppsChangedCallbackCompat callback) {
            mCallback = callback;
        }

        public void onPackageRemoved(String packageName, UserHandle user) {
            mCallback.onPackageRemoved(packageName, user);
        }

        public void onPackageAdded(String packageName, UserHandle user) {
            mCallback.onPackageAdded(packageName, user);
        }

        public void onPackageChanged(String packageName, UserHandle user) {
            mCallback.onPackageChanged(packageName, user);
        }

        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            mCallback.onPackagesAvailable(packageNames, user, replacing);
        }

        public void onPackagesUnavailable(String[] packageNames, UserHandle user,
                                          boolean replacing) {
            mCallback.onPackagesUnavailable(packageNames, user,
                    replacing);
        }

        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
            mCallback.onPackagesSuspended(packageNames, user);
        }

        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
            mCallback.onPackagesUnsuspended(packageNames, user);
        }

        @Override
        public void onShortcutsChanged(@NonNull String packageName, @NonNull List<ShortcutInfo> shortcuts,
                                       @NonNull UserHandle user) {
            List<ShortcutInfoCompat> shortcutInfoCompats = new ArrayList<>(shortcuts.size());
            for (ShortcutInfo shortcutInfo : shortcuts) {
                shortcutInfoCompats.add(new ShortcutInfoCompat(shortcutInfo));
            }

            mCallback.onShortcutsChanged(packageName, shortcutInfoCompats,
                    user);
        }
    }
}


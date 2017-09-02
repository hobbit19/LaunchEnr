package com.enrico.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.enrico.launcher3.compat.LauncherActivityInfoCompat;
import com.enrico.launcher3.compat.LauncherAppsCompat;
import com.enrico.launcher3.icons.IconCache;
import com.enrico.launcher3.util.FlagOp;
import com.enrico.launcher3.util.StringFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Stores the list of all applications for the all apps view.
 */
class AllAppsList {
    private static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    /**
     * The list off all apps.
     */
    public ArrayList<AppInfo> data =
            new ArrayList<>(DEFAULT_APPLICATIONS_NUMBER);
    /**
     * The list of apps that have been added since the last notify() call.
     */
    public ArrayList<AppInfo> added =
            new ArrayList<>(DEFAULT_APPLICATIONS_NUMBER);
    /**
     * The list of apps that have been removed since the last notify() call.
     */
    public ArrayList<AppInfo> removed = new ArrayList<>();
    /**
     * The list of apps that have been modified since the last notify() call.
     */
    public ArrayList<AppInfo> modified = new ArrayList<>();

    private IconCache mIconCache;

    private AppFilter mAppFilter;

    /**
     * Boring constructor.
     */

    AllAppsList(Context context, IconCache iconCache, AppFilter appFilter) {
        mIconCache = iconCache;
        mAppFilter = appFilter;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<LauncherActivityInfoCompat> apps,
                                        ComponentName component) {
        for (LauncherActivityInfoCompat info : apps) {
            if (info.getComponentName().equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Query the launcher apps service for whether the supplied package has
     * MAIN/LAUNCHER activities in the supplied package.
     */
    static boolean packageHasActivities(Context context, String packageName,
                                        UserHandle user) {
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        return launcherApps.getActivityList(packageName, user).size() > 0;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(ArrayList<AppInfo> apps, ComponentName component,
                                        UserHandle user) {
        final int N = apps.size();
        for (int i = 0; i < N; i++) {
            final AppInfo info = apps.get(i);
            if (info.user.equals(user) && info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     * <p>
     * If the app is already in the list, doesn't add it.
     */
    public void add(AppInfo info, Context context) {
        if (mAppFilter != null && !mAppFilter.shouldShowApp(info.componentName, context)) {
            return;
        }
        if (findActivity(data, info.componentName, info.user)) {
            return;
        }
        data.add(info);
        added.add(info);
    }

    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
    }

    public int size() {
        return data.size();
    }

    public AppInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    void addPackage(Context context, String packageName, UserHandle user) {
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        final List<LauncherActivityInfoCompat> matches = launcherApps.getActivityList(packageName,
                user);

        for (LauncherActivityInfoCompat info : matches) {
            add(new AppInfo(context, info, user, mIconCache), context);
        }
    }

    /**
     * Remove the apps for the given apk identified by packageName.
     */
    void removePackage(String packageName, UserHandle user) {
        final List<AppInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            AppInfo info = data.get(i);
            final ComponentName component = info.intent.getComponent();
            if (info.user.equals(user) && packageName.equals(component.getPackageName())) {
                removed.add(info);
                data.remove(i);
            }
        }
    }

    /**
     * Updates the apps for the given packageName and user based on {@param op}.
     */
    void updatePackageFlags(StringFilter pkgFilter, UserHandle user, FlagOp op) {
        final List<AppInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            AppInfo info = data.get(i);
            final ComponentName component = info.intent.getComponent();
            if (info.user.equals(user) && pkgFilter.matches(component.getPackageName())) {
                info.isDisabled = op.apply(info.isDisabled);
                modified.add(info);
            }
        }
    }

    void updateIconsAndLabels(HashSet<String> packages, UserHandle user,
                              ArrayList<AppInfo> outUpdates) {
        for (AppInfo info : data) {
            if (info.user.equals(user) && packages.contains(info.componentName.getPackageName())) {

                mIconCache.updateTitleAndIcon(info);
                outUpdates.add(info);
            }
        }
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    void updatePackage(Context context, String packageName, UserHandle user) {
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
        final List<LauncherActivityInfoCompat> matches = launcherApps.getActivityList(packageName,
                user);
        if (matches.size() > 0) {
            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
            for (int i = data.size() - 1; i >= 0; i--) {
                final AppInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (user.equals(applicationInfo.user)
                        && packageName.equals(component.getPackageName())) {
                    if (!findActivity(matches, component)) {
                        removed.add(applicationInfo);
                        data.remove(i);
                    }
                }
            }

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            for (final LauncherActivityInfoCompat info : matches) {
                AppInfo applicationInfo = findApplicationInfoLocked(
                        info.getComponentName().getPackageName(), user,
                        info.getComponentName().getClassName());
                if (applicationInfo == null) {
                    add(new AppInfo(context, info, user, mIconCache), context);
                } else {
                    mIconCache.getTitleAndIcon(applicationInfo, info, true /* useLowResIcon */);
                    modified.add(applicationInfo);
                }
            }
        } else {
            // Remove all data for this package.
            for (int i = data.size() - 1; i >= 0; i--) {
                final AppInfo applicationInfo = data.get(i);
                final ComponentName component = applicationInfo.intent.getComponent();
                if (user.equals(applicationInfo.user)
                        && packageName.equals(component.getPackageName())) {
                    removed.add(applicationInfo);
                    mIconCache.remove(component, user);
                    data.remove(i);
                }
            }
        }
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private AppInfo findApplicationInfoLocked(String packageName, UserHandle user,
                                              String className) {
        for (AppInfo info : data) {
            final ComponentName component = info.intent.getComponent();
            if (user.equals(info.user) && packageName.equals(component.getPackageName())
                    && className.equals(component.getClassName())) {
                return info;
            }
        }
        return null;
    }
}

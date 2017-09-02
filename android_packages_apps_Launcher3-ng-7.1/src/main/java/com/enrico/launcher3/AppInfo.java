package com.enrico.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.UserHandle;
import android.util.Pair;

import com.enrico.launcher3.compat.LauncherActivityInfoCompat;
import com.enrico.launcher3.compat.UserManagerCompat;
import com.enrico.launcher3.icons.IconCache;
import com.enrico.launcher3.util.ComponentKey;
import com.enrico.launcher3.util.PackageManagerHelper;

/**
 * Represents an app in AllAppsView.
 */
public class AppInfo extends ItemInfo {

    static final int DOWNLOADED_FLAG = 1;
    private static final int UPDATED_SYSTEM_APP_FLAG = 2;
    /**
     * The intent used to start the application.
     */
    public Intent intent;
    public ComponentName componentName;

    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;
    /**
     * Indicates whether we're using a low res icon
     */
    public boolean usingLowResIcon;
    public int flags = 0;

    /**
     * {@see ShortcutInfo#isDisabled}
     */
    int isDisabled = ShortcutInfo.DEFAULT;

    public AppInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }

    /**
     * Must not hold the Context.
     */
    public AppInfo(Context context, LauncherActivityInfoCompat info, UserHandle user,
                   IconCache iconCache) {
        this(context, info, user, iconCache,
                UserManagerCompat.getInstance(context).isQuietModeEnabled(user));
    }

    protected Intent getRestoredIntent() {
        return null;
    }

    /**
     * @return the component name and flags if {@param info} is an AppInfo or an app shortcut.
     */
    public static Pair<ComponentName, Integer> getAppInfoFlags(Object item) {
        if (item instanceof AppInfo) {
            AppInfo info = (AppInfo) item;
            return Pair.create(info.componentName, info.flags);
        } else if (item instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) item;
            ComponentName component = info.getTargetComponent();
            if (info.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION
                    && component != null) {
                return Pair.create(component, info.flags);
            }
        }
        return null;
    }

    public AppInfo(Context context, LauncherActivityInfoCompat info, UserHandle user,
                   IconCache iconCache, boolean quietModeEnabled) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;
        flags = initFlags(info);
        if (PackageManagerHelper.isAppSuspended(info.getApplicationInfo())) {
            isDisabled |= ShortcutInfo.FLAG_DISABLED_SUSPENDED;
        }
        if (quietModeEnabled) {
            isDisabled |= ShortcutInfo.FLAG_DISABLED_QUIET_USER;
        }

        // Using the full res icon on init might need to be made configurable for low spec devices.
        iconCache.getTitleAndIcon(this, info, false /* useLowResIcon */);
        intent = makeLaunchIntent(context, info, user);
        this.user = user;
    }

    public AppInfo(AppInfo info) {
        super(info);
        componentName = info.componentName;
        title = Utilities.trim(info.title);
        intent = new Intent(info.intent);
        flags = info.flags;
        isDisabled = info.isDisabled;
        iconBitmap = info.iconBitmap;
    }

    static int initFlags(LauncherActivityInfoCompat info) {
        int appFlags = info.getApplicationInfo().flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;

            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    static Intent makeLaunchIntent(Context context, LauncherActivityInfoCompat info,
                                   UserHandle user) {
        long serialNumber = UserManagerCompat.getInstance(context).getSerialNumberForUser(user);
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(info.getComponentName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .putExtra(EXTRA_PROFILE, serialNumber);
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }

    public ComponentKey toComponentKey() {
        return new ComponentKey(componentName, user);
    }

    @Override
    public boolean isDisabled() {
        return isDisabled != 0;
    }
}

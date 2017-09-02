package com.enrico.launcher3.widget;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.PendingAddItemInfo;

/**
 * Meta data used for late binding of the short cuts.
 *
 * @see {@link PendingAddItemInfo}
 */
public class PendingAddShortcutInfo extends PendingAddItemInfo {

    ActivityInfo activityInfo;

    PendingAddShortcutInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
        componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
        itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
    }
}

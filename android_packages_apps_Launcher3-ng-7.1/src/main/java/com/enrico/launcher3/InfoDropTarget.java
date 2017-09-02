package com.enrico.launcher3;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.enrico.launcher3.compat.LauncherAppsCompat;

public class InfoDropTarget extends UninstallDropTarget {

    public InfoDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @return Whether the activity was started.
     */
    public static boolean startDetailsActivityForInfo(
            ItemInfo info, Launcher launcher, DropTargetResultCallback callback) {
        boolean result = false;
        ComponentName componentName = null;
        if (info instanceof AppInfo) {
            componentName = ((AppInfo) info).componentName;
        } else if (info instanceof ShortcutInfo) {
            componentName = ((ShortcutInfo) info).intent.getComponent();
        } else if (info instanceof PendingAddItemInfo) {
            componentName = ((PendingAddItemInfo) info).componentName;
        } else if (info instanceof LauncherAppWidgetInfo) {
            componentName = ((LauncherAppWidgetInfo) info).providerName;
        }
        if (componentName != null) {
            try {
                LauncherAppsCompat.getInstance(launcher)
                        .showAppDetailsForProfile(componentName, info.user);
                result = true;
            } catch (SecurityException | ActivityNotFoundException e) {
                Toast.makeText(launcher, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            }
        }

        if (callback != null) {
            sendUninstallResult(launcher, result, componentName, info.user, callback);
        }
        return result;
    }

    public static boolean supportsDrop(ItemInfo info) {

        return (info instanceof AppInfo || info instanceof ShortcutInfo
                || info instanceof PendingAddItemInfo || info instanceof LauncherAppWidgetInfo)
                && info.itemType != LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setDrawable(R.drawable.ic_information_outline_white);
    }

    @Override
    public void completeDrop(DragObject d) {
        DropTargetResultCallback callback = d.dragSource instanceof DropTargetResultCallback
                ? (DropTargetResultCallback) d.dragSource : null;
        startDetailsActivityForInfo(d.dragInfo, mLauncher, callback);
    }

    @Override
    protected boolean supportsDrop(DragSource source, ItemInfo info) {
        return source.supportsAppInfoDropTarget() && supportsDrop(info);
    }
}

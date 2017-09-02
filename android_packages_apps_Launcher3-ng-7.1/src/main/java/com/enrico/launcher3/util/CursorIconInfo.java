package com.enrico.launcher3.util;

import android.content.Context;
import android.content.Intent.ShortcutIconResource;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.ShortcutInfo;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.icons.IconUtils;

/**
 * Utility class to load icon from a cursor.
 */
public class CursorIconInfo {
    public final int titleIndex;
    private final int iconPackageIndex;
    private final int iconResourceIndex;
    private final int iconIndex;
    private final Context mContext;

    public CursorIconInfo(Context context, Cursor c) {
        mContext = context;

        iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
        iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
        iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);

        titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
    }

    /**
     * Loads the icon from the cursor and updates the {@param info} if the icon is an app resource.
     */
    public Bitmap loadIcon(Cursor c, ShortcutInfo info) {
        Bitmap icon = null;
        String packageName = c.getString(iconPackageIndex);
        String resourceName = c.getString(iconResourceIndex);
        if (!TextUtils.isEmpty(packageName) || !TextUtils.isEmpty(resourceName)) {
            info.iconResource = new ShortcutIconResource();
            info.iconResource.packageName = packageName;
            info.iconResource.resourceName = resourceName;
            icon = IconUtils.createIconBitmap(packageName, resourceName, mContext);
        }
        if (icon == null) {
            // Failed to load from resource, try loading from DB.
            icon = loadIcon(c);
        }
        return icon;
    }

    /**
     * Loads the fixed bitmap from the icon if available.
     */
    public Bitmap loadIcon(Cursor c) {
        return IconUtils.createIconBitmap(c, iconIndex, mContext);
    }

    /**
     * Returns the title or empty string
     */
    public String getTitle(Cursor c) {
        String title = c.getString(titleIndex);
        return TextUtils.isEmpty(title) ? "" : Utilities.trim(c.getString(titleIndex));
    }
}

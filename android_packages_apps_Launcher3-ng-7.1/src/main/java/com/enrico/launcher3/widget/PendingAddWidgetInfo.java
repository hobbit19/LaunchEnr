package com.enrico.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.os.Bundle;

import com.enrico.launcher3.LauncherAppWidgetProviderInfo;
import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.PendingAddItemInfo;
import com.enrico.launcher3.compat.AppWidgetManagerCompat;

/**
 * Meta data used for late binding of {@link LauncherAppWidgetProviderInfo}.
 *
 * @see {@link PendingAddItemInfo}
 */
public class PendingAddWidgetInfo extends PendingAddItemInfo {
    int previewImage;
    public int icon;
    public LauncherAppWidgetProviderInfo info;
    public AppWidgetHostView boundWidget;
    public Bundle bindOptions = null;

    public PendingAddWidgetInfo(Context context, LauncherAppWidgetProviderInfo i) {
        if (i.isCustomWidget) {
            itemType = LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET;
        } else {
            itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
        }
        this.info = i;
        user = AppWidgetManagerCompat.getInstance(context).getUser(i);
        componentName = i.provider;
        previewImage = i.previewImage;
        icon = i.icon;

        spanX = i.spanX;
        spanY = i.spanY;
        minSpanX = i.minSpanX;
        minSpanY = i.minSpanY;
    }

/*    public boolean isCustomWidget() {
        return itemType == LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET;
    }*/
}

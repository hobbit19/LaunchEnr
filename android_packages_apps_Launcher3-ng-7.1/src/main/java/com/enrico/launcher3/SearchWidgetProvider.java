package com.enrico.launcher3;

import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by Enrico on 26/07/2017.
 */

public class SearchWidgetProvider {

    /**
     * -     * Returns a widget with category {@link AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}
     * -     * provided by the same package which is set to be global search activity.
     * -     * If widgetCategory is not supported, or no such widget is found, returns the first widget
     * -     * provided by the package.
     * -
     */
    public static AppWidgetProviderInfo get(Context context) {
        SearchManager searchManager =
                (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchComponent = searchManager.getGlobalSearchActivity();
        if (searchComponent == null) return null;
        String providerPkg = searchComponent.getPackageName();

        AppWidgetProviderInfo defaultWidgetForSearchPackage = null;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
            if (info.provider.getPackageName().equals(providerPkg) && info.configure == null) {
                if ((info.widgetCategory & AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX) != 0) {
                    return info;
                } else if (defaultWidgetForSearchPackage == null) {
                    defaultWidgetForSearchPackage = info;
                }
            }
        }
        return defaultWidgetForSearchPackage;
    }
}

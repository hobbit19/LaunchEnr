package com.enrico.launcher3.compat;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;

import com.enrico.launcher3.LauncherAppWidgetProviderInfo;
import com.enrico.launcher3.icons.IconCache;
import com.enrico.launcher3.util.ComponentKey;

import java.util.HashMap;
import java.util.List;

public abstract class AppWidgetManagerCompat {

    private static final Object sInstanceLock = new Object();

    final AppWidgetManager mAppWidgetManager;
    final Context mContext;

    AppWidgetManagerCompat(Context context) {
        mContext = context;
        mAppWidgetManager = AppWidgetManager.getInstance(context);
    }

    public static AppWidgetManagerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {

            return new AppWidgetManagerCompatVL(context.getApplicationContext());
        }
    }

    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        return mAppWidgetManager.getAppWidgetInfo(appWidgetId);
    }

    public LauncherAppWidgetProviderInfo getLauncherAppWidgetInfo(int appWidgetId) {
        AppWidgetProviderInfo info = getAppWidgetInfo(appWidgetId);
        return info == null ? null : LauncherAppWidgetProviderInfo.fromProviderInfo(mContext, info);
    }

    public abstract List<AppWidgetProviderInfo> getAllProviders();

    public abstract String loadLabel(LauncherAppWidgetProviderInfo info);

    public abstract boolean bindAppWidgetIdIfAllowed(
            int appWidgetId, AppWidgetProviderInfo info, Bundle options);

    public abstract UserHandle getUser(LauncherAppWidgetProviderInfo info);

    public abstract void startConfigActivity(AppWidgetProviderInfo info, int widgetId,
                                             Activity activity, AppWidgetHost host, int requestCode);

    public abstract Drawable loadPreview(AppWidgetProviderInfo info);

    public abstract Drawable loadIcon(LauncherAppWidgetProviderInfo info, IconCache cache);

    public abstract Bitmap getBadgeBitmap(LauncherAppWidgetProviderInfo info, Bitmap bitmap,
                                          int imageWidth, int imageHeight);

    public abstract LauncherAppWidgetProviderInfo findProvider(
            ComponentName provider, UserHandle user);

    public abstract HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap();
}

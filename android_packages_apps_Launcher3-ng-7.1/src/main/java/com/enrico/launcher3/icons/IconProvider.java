package com.enrico.launcher3.icons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.LauncherModel;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.compat.LauncherActivityInfoCompat;
import com.enrico.launcher3.compat.UserManagerCompat;
import com.enrico.launcher3.shortcuts.ShortcutInfoCompat;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

class IconProvider {

    private static final String CALENDAR_PACKAGE = "com.google.android.calendar";
    private static final String CALENDAR_ROUND_ICONS = "com.google.android.calendar._icons_nexus_round";

    private PackageManager mPackageManager;
    private String mSystemState;
    private Context context;
    private IconsHandler mIconsHandler;

    static final int NONE = 28;
    static final int ROUND_COLORFUL = 30;

    private IconProvider(Context context) {
        this.context = context;
        BroadcastReceiver mBroadcastReceiver = new IconProviderReceiver(this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.DATE_CHANGED");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        context.registerReceiver(mBroadcastReceiver, intentFilter, null, new Handler(LauncherModel.getWorkerLooper()));
        mPackageManager = context.getPackageManager();
        mIconsHandler = IconCache.getIconsHandler(context);
        updateSystemStateString();
    }

    private Drawable getRoundIcon(Context context,String packageName, int iconDpi) {

        mPackageManager = context.getPackageManager();

        try {
            Resources resourcesForApplication = mPackageManager.getResourcesForApplication(packageName);
            AssetManager assets = resourcesForApplication.getAssets();
            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");
            int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT)
                if (eventType == XmlPullParser.START_TAG && parseXml.getName().equals("application"))
                    for (int i = 0; i < parseXml.getAttributeCount(); i++)
                        if (parseXml.getAttributeName(i).equals("roundIcon"))
                            return resourcesForApplication.getDrawableForDensity(Integer.parseInt(parseXml.getAttributeValue(i).substring(1)), iconDpi, context.getTheme());
            parseXml.close();
        }
        catch (Exception ex) {
            Log.w("getRoundIcon", ex);
        }
        return null;
    }

    static IconProvider loadByName(String className, Context context) {
        if (TextUtils.isEmpty(className)) return new IconProvider(context);

        try {
            Class<?> cls = Class.forName(className);
            return (IconProvider) cls.getDeclaredConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException | NoSuchMethodException | InvocationTargetException e) {

            return new IconProvider(context);
        }
    }

    void updateSystemStateString() {
        mSystemState = Locale.getDefault().toString();
    }

    String getIconSystemState(String packageName) {
        if (isCalendar(packageName)) {
            return mSystemState + " " + dayOfMonth();
        }
        return mSystemState;
    }

    private int getCorrectShape(Bundle bundle, Resources resources) {
        if (bundle != null) {
            int roundIcons = bundle.getInt(CALENDAR_ROUND_ICONS, 0);
            if (roundIcons != 0) {
                try {
                    TypedArray obtainTypedArray = resources.obtainTypedArray(roundIcons);
                    int resourceId = obtainTypedArray.getResourceId(dayOfMonth(), 0);
                    obtainTypedArray.recycle();
                    return resourceId;
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    private int dayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
    }

    private boolean isCalendar(final String packageName) {
        return CALENDAR_PACKAGE.equals(packageName);
    }

    public Drawable getIcon(LauncherActivityInfoCompat info, int iconDpi) {

        Drawable iconStyle = null;

        final int DEFAULT = 29;

        String choice = Utilities.getPrefs(context)
                .getString(IconUtils.ROUND_ICONS_KEY, String.valueOf(28));

        switch (Integer.parseInt(choice)) {
            case DEFAULT:
                iconStyle = getRoundIcon(context, info.getApplicationInfo().packageName, iconDpi);
                break;
            case ROUND_COLORFUL:
                Bitmap round = IconUtils.createRoundIcon(context, info);
                iconStyle = new BitmapDrawable(context.getResources(), round);
                break;
        }

        Drawable drawable = IconUtils.isRoundIcon(context) ? iconStyle : getIconFromHandler(info);

        String packageName = info.getApplicationInfo().packageName;

        if (isCalendar(packageName)) {
            try {

                int uninstalled = android.os.Build.VERSION.SDK_INT >= 24 ? PackageManager.MATCH_UNINSTALLED_PACKAGES : PackageManager.GET_UNINSTALLED_PACKAGES;

                ActivityInfo activityInfo = mPackageManager.getActivityInfo(info.getComponentName(),
                        PackageManager.GET_META_DATA | uninstalled);
                Bundle metaData = activityInfo.metaData;
                Resources resourcesForApplication = mPackageManager.getResourcesForApplication(packageName);
                int shape = getCorrectShape(metaData, resourcesForApplication);
                if (shape != 0) {
                    drawable = resourcesForApplication.getDrawableForDensity(shape, iconDpi, context.getTheme());
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return drawable != null ? drawable : info.getIcon(iconDpi);
    }

    private Drawable getIconFromHandler(LauncherActivityInfoCompat info) {
        Bitmap bm = mIconsHandler.getDrawableIconForPackage(info.getComponentName());
        if (bm == null) {
            return null;
        }
        return new BitmapDrawable(context.getResources(), IconUtils.createIconBitmap(bm, context));
    }

    class IconProviderReceiver extends BroadcastReceiver {
        IconProvider mIconProvider;

        IconProviderReceiver(final IconProvider IconProvider) {
            mIconProvider = IconProvider;
        }

        public void onReceive(final Context context, final Intent intent) {
            for (UserHandle userHandle : UserManagerCompat.getInstance(context).getUserProfiles()) {

                LauncherAppState instance = LauncherAppState.getInstance();
                instance.getModel().onPackageChanged(CALENDAR_PACKAGE, userHandle);
                List<ShortcutInfoCompat> queryForPinnedShortcuts = instance.getShortcutManager().queryForPinnedShortcuts(CALENDAR_PACKAGE, userHandle);
                if (!queryForPinnedShortcuts.isEmpty()) {
                    instance.getModel().updatePinnedShortcuts(CALENDAR_PACKAGE, queryForPinnedShortcuts, userHandle);
                }
            }
        }
    }
}

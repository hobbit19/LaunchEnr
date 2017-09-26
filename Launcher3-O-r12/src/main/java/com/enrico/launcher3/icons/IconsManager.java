package com.enrico.launcher3.icons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Pair;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.graphics.LauncherIcons;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IconsManager {

    private static String[] LAUNCHER_INTENTS = new String[] {
        "com.fede.launcher.THEME_ICONPACK",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "com.gau.go.launcherex.theme",
        "org.adw.launcher.THEMES",
        "org.adw.launcher.icons.ACTION_PICK_ICON"
    };

    private Map<String, IconPackInfo> mIconPacks = new HashMap<>();
    private static Map<String, String> mAppFilterDrawables = new HashMap<>();
    private static List<Bitmap> mBackImages = new ArrayList<>();
    private static List<String> mDrawables = new ArrayList<>();

    private static Bitmap mFrontImage;
    private static Bitmap mMaskImage;

    private static Resources mCurrentIconPackRes;
    private static  Resources mOriginalIconPackRes;
    private static String mIconPackPackageName;

    private Context mContext;
    private static PackageManager mPackageManager;
    private static String mDefaultIconPack;

    private static float mFactor = 1.0f;

    public IconsManager(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mDefaultIconPack = context.getString(R.string.default_iconpack_title);

        String iconPack =  Utilities.getPrefs(context)
                    .getString(ICON_PACK_PREFERENCE_KEY, mDefaultIconPack);
        loadAvailableIconPacks();
        loadIconPack(iconPack, false, context);
    }

    private static void loadIconPack(String packageName, boolean fallback, Context context) {

        mIconPackPackageName = packageName;
        if (!fallback) {
            mAppFilterDrawables.clear();
            mBackImages.clear();
            clearCache(context);
        } else {
            mDrawables.clear();
        }

        if (isDefaultIconPack()) {
            return;
        }

        XmlPullParser xpp = null;

        //do nothing if icon pack is the default one, i.e. ""
        if (!mIconPackPackageName.isEmpty()) {
            try {
                mOriginalIconPackRes = mPackageManager.getResourcesForApplication(mIconPackPackageName);
                mCurrentIconPackRes = mOriginalIconPackRes;
                int appfilterid = mOriginalIconPackRes.getIdentifier("appfilter", "xml", mIconPackPackageName);
                if (appfilterid > 0) {
                    xpp = mOriginalIconPackRes.getXml(appfilterid);
                }

                if (xpp != null) {
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (!fallback & xpp.getName().equals("iconback")) {
                                for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                    if (xpp.getAttributeName(i).startsWith("img")) {
                                        String drawableName = xpp.getAttributeValue(i);
                                        Bitmap iconback = loadBitmap(drawableName);
                                        if (iconback != null) {
                                            mBackImages.add(iconback);
                                        }
                                    }
                                }
                            } else if (!fallback && xpp.getName().equals("iconmask")) {
                                if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                                    String drawableName = xpp.getAttributeValue(0);
                                    mMaskImage = loadBitmap(drawableName);
                                }
                            } else if (!fallback && xpp.getName().equals("iconupon")) {
                                if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("img1")) {
                                    String drawableName = xpp.getAttributeValue(0);
                                    mFrontImage = loadBitmap(drawableName);
                                }
                            } else if (!fallback && xpp.getName().equals("scale")) {
                                if (xpp.getAttributeCount() > 0 && xpp.getAttributeName(0).equals("factor")) {
                                    mFactor = Float.valueOf(xpp.getAttributeValue(0));
                                }
                            }
                            if (xpp.getName().equals("item")) {
                                String componentName = null;
                                String drawableName = null;

                                for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                    if (xpp.getAttributeName(i).equals("component")) {
                                        componentName = xpp.getAttributeValue(i);
                                    } else if (xpp.getAttributeName(i).equals("drawable")) {
                                        drawableName = xpp.getAttributeValue(i);
                                    }
                                }
                                if (fallback && getIdentifier(packageName, drawableName, true) > 0
                                        && !mDrawables.contains(drawableName)) {
                                    mDrawables.add(drawableName);
                                }
                                if (!fallback && componentName != null && drawableName != null &&
                                        !mAppFilterDrawables.containsKey(componentName)) {
                                    mAppFilterDrawables.put(componentName, drawableName);
                                }
                            }
                        }
                        eventType = xpp.next();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    List<String> getAllDrawables(final String packageName) {
        loadAllDrawables(packageName);
        Collections.sort(mDrawables, new Comparator<String>() {
            @Override
            public int compare(String drawable, String drawable2) {
                return drawable.compareToIgnoreCase(drawable2);
            }
        });

        return mDrawables;
    }

    private void loadAllDrawables(String packageName) {
        mDrawables.clear();
        XmlPullParser xpp;
        try {
            Resources res = mPackageManager.getResourcesForApplication(packageName);
            mCurrentIconPackRes = res;
            int resource = res.getIdentifier("drawable", "xml", packageName);
            if (resource < 0) {
                return;
            }
            xpp = res.getXml(resource);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String drawableName = xpp.getAttributeValue(null, "drawable");
                        if (!mDrawables.contains(drawableName) &&
                                getIdentifier(packageName, drawableName, true) > 0) {
                            mDrawables.add(drawableName);
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // fallback onto appfilter if drawable xml fails
            loadIconPack(packageName, true, mContext);
        }
    }

    private static boolean isDefaultIconPack() {
        return mIconPackPackageName.equalsIgnoreCase(mDefaultIconPack);
    }

    List<String> getMatchingDrawables(String packageName) {
        List<String> matchingDrawables = new ArrayList<>();
        ApplicationInfo info = null;
        try {
            info = mPackageManager.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String packageLabel = (info != null ? mPackageManager.getApplicationLabel(info).toString()
                : packageName).replaceAll("[^a-zA-Z]", "").toLowerCase().trim();
        for (String drawable : mDrawables) {
            if (drawable == null) continue;
            String filteredDrawable = drawable.replaceAll("[^a-zA-Z]", "").toLowerCase().trim();
            if (filteredDrawable.length() > 2 && (packageLabel.contains(filteredDrawable)
                    || filteredDrawable.contains(packageLabel))) {
                matchingDrawables.add(drawable);
            }
        }
        return matchingDrawables;
    }

    private static int getIdentifier(String packageName, String drawableName, boolean currentIconPack) {
        if (drawableName == null) {
            return 0;
        }
        if (packageName == null) {
            packageName = mIconPackPackageName;
        }
        return (!currentIconPack ? mOriginalIconPackRes : mCurrentIconPackRes).getIdentifier(
                drawableName, "drawable", packageName);
    }

    static Drawable loadDrawable(String packageName, String drawableName, boolean currentIconPack) {
        if (packageName == null) {
            packageName = mIconPackPackageName;
        }
        int id = getIdentifier(packageName, drawableName, currentIconPack);
        if (id > 0) {
            return (!currentIconPack ? mOriginalIconPackRes : mCurrentIconPackRes).getDrawable(id);
        }
        return null;
    }

    private static Bitmap loadBitmap(String drawableName) {
        Drawable bitmap = loadDrawable(null, drawableName, true);
        if (bitmap != null && bitmap instanceof BitmapDrawable) {
            return ((BitmapDrawable) bitmap).getBitmap();
        }
        return null;
    }

    Bitmap getDefaultAppDrawable(String packageName) {
        Drawable drawable = null;
        try {
            drawable = mPackageManager.getApplicationIcon(mPackageManager.getApplicationInfo(
                    packageName, 0));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return generateBitmap(((BitmapDrawable) drawable).getBitmap());
        }
        return generateBitmap(Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888));
    }

    public static void switchIconPacks(String packageName, Context mContext) {

       IconCache mIconCache = LauncherAppState.getInstance(mContext).getIconCache();

        new IconPackLoader(packageName, mContext, mIconCache).execute();
    }

    Bitmap getDrawableIconForPackage(ComponentName componentName) {

        if (isDefaultIconPack()) {
            return getDefaultAppDrawable(componentName.getPackageName());
        }

        String drawableName = mAppFilterDrawables.get(componentName.toString());
        Drawable drawable = loadDrawable(null, drawableName, false);
        if (drawable != null && drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            cacheStoreDrawable(componentName.toString(), bitmap);
            return bitmap;
        }

        Bitmap cachedIcon = cacheGetDrawable(componentName.toString());
        if (cachedIcon != null) {
            return cachedIcon;
        }

        return getDefaultAppDrawable(componentName.getPackageName());
    }

    private Bitmap generateBitmap(Bitmap defaultBitmap) {
        if (mBackImages.isEmpty()) {
            return defaultBitmap;
        }
        Random random = new Random();
        int id = random.nextInt(mBackImages.size());
        Bitmap backImage = mBackImages.get(id);
        int w = backImage.getWidth();
        int h = backImage.getHeight();

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(backImage, 0, 0, null);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(defaultBitmap,
                (int) (w * mFactor), (int) (h * mFactor), false);

        Bitmap mutableMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(mutableMask);
        Bitmap targetBitmap = mMaskImage == null ? mutableMask : mMaskImage;
        maskCanvas.drawBitmap(targetBitmap, 0, 0, new Paint());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawBitmap(scaledBitmap, (w - scaledBitmap.getWidth()) / 2,
                (h - scaledBitmap.getHeight()) / 2, null);
        canvas.drawBitmap(mutableMask, 0, 0, paint);

        if (mFrontImage != null) {
            canvas.drawBitmap(mFrontImage, 0, 0, null);
        }
        return result;
    }

    public Pair<List<String>, List<String>> getAllIconPacks() {

        //be sure to update the icon packs list
        loadAvailableIconPacks();

        List<String> iconPackNames = new ArrayList<>();
        List<String> iconPackLabels = new ArrayList<>();
        List<IconPackInfo> iconPacks = new ArrayList<>(mIconPacks.values());
        Collections.sort(iconPacks, new Comparator<IconPackInfo>() {
            @Override
            public int compare(IconPackInfo info, IconPackInfo info2) {
                return info.label.toString().compareToIgnoreCase(info2.label.toString());
            }
        });
        for (IconPackInfo info : iconPacks) {
            iconPackNames.add(info.packageName);
            iconPackLabels.add(info.label.toString());
        }
        return new Pair<>(iconPackNames, iconPackLabels);
    }

    private void loadAvailableIconPacks() {
        List<ResolveInfo> launcherActivities = new ArrayList<>();
        mIconPacks.clear();
        for (String i : LAUNCHER_INTENTS) {
            launcherActivities.addAll(mPackageManager.queryIntentActivities(
                    new Intent(i), PackageManager.GET_META_DATA));
        }
        for (ResolveInfo ri : launcherActivities) {
            String packageName = ri.activityInfo.packageName;
            IconPackInfo info = new IconPackInfo(ri, mPackageManager);
            try {
                ApplicationInfo ai = mPackageManager.getApplicationInfo(packageName,
                        PackageManager.GET_META_DATA);
                mIconPacks.put(packageName, info);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDrawableInCache(String key) {
        File drawableFile = cacheGetFileName(key);
        return drawableFile.isFile();
    }

    private void cacheStoreDrawable(String key, Bitmap bitmap) {
        if (isDrawableInCache(key)) return;
        File drawableFile = cacheGetFileName(key);
        try (FileOutputStream fos = new FileOutputStream(drawableFile)) {
            bitmap.compress(CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private Bitmap cacheGetDrawable(String key) {
        if (!isDrawableInCache(key)) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(cacheGetFileName(key))) {
            BitmapDrawable drawable =
                    new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeStream(fis));
            fis.close();
            return drawable.getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private File cacheGetFileName(String key) {
        return new File(getIconsCacheDir(mContext) + mIconPackPackageName + "_" + key.hashCode() + ".png");
    }

    private static File getIconsCacheDir(Context mContext) {
        return new File(mContext.getCacheDir().getPath() + "/icons/");
    }

    private static void clearCache(Context mContext) {
        File cacheDir = getIconsCacheDir(mContext);
        if (!cacheDir.isDirectory()) {
            return;
        }

        for (File item : cacheDir.listFiles()) {
            if (!item.delete()) {
               // Do nothing
            }
        }
    }

    private static class IconPackInfo {
        String packageName;
        CharSequence label;
        Drawable icon;

        IconPackInfo(ResolveInfo r, PackageManager packageManager) {
            packageName = r.activityInfo.packageName;
            icon = r.loadIcon(packageManager);
            label = r.loadLabel(packageManager);
        }
    }

    private static class IconPackLoader extends AsyncTask<Void, Void, Void> {
        private String mIconPackPackageName;
        private Context mContext;
        private IconCache mIconCache;
        private IconPackLoader(String packageName, Context context, IconCache iconCache) {
            mIconPackPackageName = packageName;
            this.mContext = context;
            mIconCache = iconCache;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loadIconPack(mIconPackPackageName, false, mContext);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Utilities.getPrefs(mContext).edit()
                    .putString(ICON_PACK_PREFERENCE_KEY, mIconPackPackageName).apply();
            mIconCache.clearIconDataBase();
            mIconCache.flush();
            LauncherAppState.getInstance(mContext).getModel().forceReload();
        }
    }

    public static final String ICON_PACK_PREFERENCE_KEY = "pref_iconPackPackage";
    public static final String ROUND_ICONS_KEY = "pref_roundIcons";

    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    //are round icons enabled?
    static boolean isRoundIcon(Context context) {
        String choice = Utilities.getPrefs(context)
                .getString(ROUND_ICONS_KEY, String.valueOf(23));
        int value = Integer.parseInt(choice);
        return value != IconThemer.NONE && Utilities.getPrefs(context).getString(ICON_PACK_PREFERENCE_KEY, "").isEmpty();
    }

    public static boolean areColorfulRoundIcons(Context context) {

        String choice = Utilities.getPrefs(context)
                .getString(ROUND_ICONS_KEY, String.valueOf(23));
        int value = Integer.parseInt(choice);
        return value == IconThemer.ROUND_COLORFUL;
    }

    public Drawable getResetIconDrawable(Context context, LauncherActivityInfo app, ItemInfo info) {
        final Drawable icon = new BitmapDrawable(context.getResources(), getDrawableIconForPackage(info.getTargetComponent()));
        return new BitmapDrawable(context.getResources(), LauncherIcons.createBadgedIconBitmap(icon, info.user, context, app.getApplicationInfo().targetSdkVersion));
    }

    public Bitmap getAppliedIconBitmap(Context context, IconCache iconCache, LauncherActivityInfo app, ItemInfo info) {
        Drawable defaultIcon = new BitmapDrawable(context.getResources(), iconCache.getNonNullIcon(iconCache.getCacheEntry(app), info.user));
        return LauncherIcons.createBadgedIconBitmap(defaultIcon, info.user, context, app.getApplicationInfo().targetSdkVersion);
    }
}

package com.enrico.launcher3;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.enrico.launcher3.dynamicui.ExtractedColors;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

    public static final boolean ATLEAST_OREO =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    public static final boolean ATLEAST_MARSHMALLOW =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    //settings keys
    public static final String PREDICTIVE_APPS_PREFERENCE_KEY = "pref_showPredictiveApps";
    public static final String THEME_KEY = "pref_lightOrDark";
    public static final String CHOOSE_THEME_KEY = "pref_chooseTheme";
    public static final String LIGHT_BARS_KEY = "pref_lightBarS";

    public static final String KEY_HIDDEN_APPS = "hidden-app";
    static final String KEY_HIDDEN_APPS_SET = "hidden-app-set";
    public static final String PINCH_OVERVIEW_KEY = "pref_pinchToOverview";
    public static final String HOTSEAT_KEY = "pref_hotseatColor";
    public static final String COLOR_FOLDERS_KEY = "pref_coloredFolders";

    // An intent extra to indicate the horizontal scroll of the wallpaper.
    static final String EXTRA_WALLPAPER_OFFSET = "com.enrico.launcher3.WALLPAPER_OFFSET";
    private static final String DT_LOCK_KEY = "pref_dtLock";
    private static final int ACCENT_INDEX = 27;
    private static final Pattern sTrimPattern =
            Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");
    private static final int[] sLoc0 = new int[2];
    private static final int[] sLoc1 = new int[2];
    // These values are same as that in {@link AsyncTask}.
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    /**
     * An {@link Executor} to be used with async task with no limit on the queue size.
     */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    /**
     * Preferences
     */

    //are predictive apps enabled?
    static boolean isPredictAppsEnabled(Context context) {
        return getPrefs(context).getBoolean(PREDICTIVE_APPS_PREFERENCE_KEY, false);
    }

    //are colored folders enabled?
    public static boolean areColoredFoldersEnabled(Context context) {
        return getPrefs(context).getBoolean(COLOR_FOLDERS_KEY,
                false);
    }

    //is dark theme enabled?
    public static boolean isDarkThemeEnabled(Context context) {
        return getPrefs(context).getBoolean(THEME_KEY,
                false);
    }

    //is pinch to overview enabled?
    public static boolean isPinchToOverviewEnabled(Context context) {
        return getPrefs(context).getBoolean(PINCH_OVERVIEW_KEY, false);
    }

    // When enabled the status bar may show dark icons based on the top of the wallpaper.
    public static boolean areLightBarsEnabled(Context context) {
        return getPrefs(context).getBoolean(LIGHT_BARS_KEY, false);
    }

    //is double tap to lock enabled?
    public static boolean isDoubleTapToLockEnabled(Context context) {
        return getPrefs(context).getBoolean(DT_LOCK_KEY,
                false);
    }

    //method to apply selected theme
    public static void applyTheme(ContextThemeWrapper contextThemeWrapper, Context context) {
        int theme = resolveTheme(context);
        contextThemeWrapper.setTheme(theme);

    }

    //multi-preference dialog for theme options
    private static int resolveTheme(Context context) {

        //light theme
        int def = isDarkThemeEnabled(context) ? R.style.Base_Theme_Dark : R.style.Base_Theme;

        //other themes
        int red = isDarkThemeEnabled(context) ? R.style.Base_Theme_Red_Dark : R.style.Base_Theme_Red;
        int pink = isDarkThemeEnabled(context) ? R.style.Base_Theme_Pink_Dark : R.style.Base_Theme_Pink;
        int purple = isDarkThemeEnabled(context) ? R.style.Base_Theme_Purple_Dark : R.style.Base_Theme_Purple;
        int deepPurple = isDarkThemeEnabled(context) ? R.style.Base_Theme_DeepPurple_Dark : R.style.Base_Theme_DeepPurple;
        int indigo = isDarkThemeEnabled(context) ? R.style.Base_Theme_Indigo_Dark : R.style.Base_Theme_Indigo;
        int blue = isDarkThemeEnabled(context) ? R.style.Base_Theme_Blue_Dark : R.style.Base_Theme_Blue;
        int lightBlue = isDarkThemeEnabled(context) ? R.style.Base_Theme_LightBlue_Dark : R.style.Base_Theme_LightBlue;
        int cyan = isDarkThemeEnabled(context) ? R.style.Base_Theme_Cyan_Dark : R.style.Base_Theme_Cyan;
        int teal = isDarkThemeEnabled(context) ? R.style.Base_Theme_Teal_Dark : R.style.Base_Theme_Teal;
        int green = isDarkThemeEnabled(context) ? R.style.Base_Theme_Green_Dark : R.style.Base_Theme_Green;
        int amber = isDarkThemeEnabled(context) ? R.style.Base_Theme_Amber_Dark : R.style.Base_Theme_Amber;
        int orange = isDarkThemeEnabled(context) ? R.style.Base_Theme_Orange_Dark : R.style.Base_Theme_Orange;
        int deepOrange = isDarkThemeEnabled(context) ? R.style.Base_Theme_DeepOrange_Dark : R.style.Base_Theme_DeepOrange;
        int brown = isDarkThemeEnabled(context) ? R.style.Base_Theme_Brown_Dark : R.style.Base_Theme_Brown;
        int blueGrey = isDarkThemeEnabled(context) ? R.style.Base_Theme_BlueGrey_Dark : R.style.Base_Theme_BlueGrey;

        String choice = getPrefs(context)
                .getString(CHOOSE_THEME_KEY, String.valueOf(0));

        switch (Integer.parseInt(choice)) {
            default:
            case 0:
                return def;

            case 1:
                return red;

            case 2:
                return pink;

            case 3:
                return purple;

            case 4:
                return deepPurple;

            case 5:
                return indigo;

            case 6:
                return blue;

            case 7:
                return lightBlue;

            case 8:
                return cyan;

            case 9:
                return teal;

            case 10:
                return green;

            case 11:
                return amber;

            case 12:
                return orange;

            case 13:
                return deepOrange;

            case 14:
                return brown;

            case 15:
                return blueGrey;

        }
    }

    //resolve hotseat options
    static int resolveHotseatColor(Context context) {

        String hotseatColor = getPrefs(context).getString(HOTSEAT_KEY, String.valueOf(24));

        switch (Integer.parseInt(hotseatColor)) {
            default:
            case 24:
                return ExtractedColors.HOTSEAT_INDEX;

            case 25:

                return Color.TRANSPARENT;

            case 26:
                return ExtractedColors.VIBRANT_INDEX;
        }
    }

    //is accent colored hotseat enabled?
    static boolean isAccentColorHotseat(Context context) {

        String choice = Utilities.getPrefs(context).getString(Utilities.HOTSEAT_KEY, String.valueOf(24));

        int value = Integer.parseInt(choice);
        return value == ACCENT_INDEX;
    }

    /**
     * Utils
     */
    //get complementary color
    static int getComplementaryColor(int colorToInvert) {

        int r = Color.red(colorToInvert);
        int g = Color.green(colorToInvert);
        int b = Color.blue(colorToInvert);
        int red = 255 - r;
        int green = 255 - g;
        int blue = 255 - b;

        return android.graphics.Color.argb(255, red, green, blue);

    }

    public static boolean isNycMR1OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean isNycOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param ancestor          The root view to make the coordinates relative to.
     * @param coord             The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToAncestor(
            View descendant, View ancestor, int[] coord, boolean includeRootScroll) {
        float[] pt = {coord[0], coord[1]};
        float scale = 1.0f;
        View v = descendant;
        while (v != ancestor && v != null) {
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v != descendant || includeRootScroll) {
                pt[0] -= v.getScrollX();
                pt[1] -= v.getScrollY();
            }

            v.getMatrix().mapPoints(pt);
            pt[0] += v.getLeft();
            pt[1] += v.getTop();
            scale *= v.getScaleX();

            v = (View) v.getParent();
        }

        coord[0] = Math.round(pt[0]);
        coord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToAncestor(View, View, int[], boolean)}.
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while (v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i - 1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = Math.round(pt[0]);
        coord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    /**
     * Translates MotionEvents from src's coordinate system to dst's.
     */
/*    public static void translateEventCoordinates(View src, View dst, MotionEvent dstEvent) {
        toGlobalMotionEvent(src, dstEvent);
        toLocalMotionEvent(dst, dstEvent);
    }*/

    /**
     * Emulates View.toGlobalMotionEvent(). This implementation does not handle transformations
     * (scaleX, scaleY, etc).
     */
    private static void toGlobalMotionEvent(View view, MotionEvent event) {
        view.getLocationOnScreen(sLoc0);
        event.offsetLocation(sLoc0[0], sLoc0[1]);
    }

    /**
     * Emulates View.toLocalMotionEvent(). This implementation does not handle transformations
     * (scaleX, scaleY, etc).
     */
     private static void toLocalMotionEvent(View view, MotionEvent event) {
        view.getLocationOnScreen(sLoc0);
        event.offsetLocation(-sLoc0[0], -sLoc0[1]);
    }

    static int[] getCenterDeltaInScreenSpace(View v0, View v1, int[] delta) {
        v0.getLocationInWindow(sLoc0);
        v1.getLocationInWindow(sLoc1);

        sLoc0[0] += (v0.getMeasuredWidth() * v0.getScaleX()) / 2;
        sLoc0[1] += (v0.getMeasuredHeight() * v0.getScaleY()) / 2;
        sLoc1[0] += (v1.getMeasuredWidth() * v1.getScaleX()) / 2;
        sLoc1[1] += (v1.getMeasuredHeight() * v1.getScaleY()) / 2;

        if (delta == null) {
            delta = new int[2];
        }

        delta[0] = sLoc1[0] - sLoc0[0];
        delta[1] = sLoc1[1] - sLoc0[1];

        return delta;
    }

    static void scaleRectAboutCenter(Rect r, float scale) {
        if (scale != 1.0f) {
            int cx = r.centerX();
            int cy = r.centerY();
            r.offset(-cx, -cy);

            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);

            r.offset(cx, cy);
        }
    }

    static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     *
     * @param bitmap  The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<Float>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

    /**
     * Calculates the height of a given string at a specific text size.
     */
    static int calculateTextHeight(float textSizePx) {
        Paint p = new Paint();
        p.setTextSize(textSizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return (int) Math.ceil(fm.bottom - fm.top);
    }

    public static boolean isRtl(Resources res) {
        return res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Returns true if the intent is a valid launch intent for a launcher activity of an app.
     * This is used to identify shortcuts which are different from the ones exposed by the
     * applications' manifest file.
     *
     * @param launchIntent The intent that will be launched when the shortcut is clicked.
     */
    static boolean isLauncherAppTarget(Intent launchIntent) {
        if (launchIntent != null
                && Intent.ACTION_MAIN.equals(launchIntent.getAction())
                && launchIntent.getComponent() != null
                && launchIntent.getCategories() != null
                && launchIntent.getCategories().size() == 1
                && launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && TextUtils.isEmpty(launchIntent.getDataString())) {
            // An app target can either have no extra or have ItemInfo.EXTRA_PROFILE.
            Bundle extras = launchIntent.getExtras();
            if (extras == null) {
                return true;
            } else {
                Set<String> keys = extras.keySet();
                return keys.size() == 1 && keys.contains(ItemInfo.EXTRA_PROFILE);
            }
        }

        return false;
    }

    static float dpiFromPx(int size, DisplayMetrics metrics) {
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }

    static int pxFromSp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public static String createDbSelectionQuery(String columnName, Iterable<?> values) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", columnName, TextUtils.join(", ", values));
    }

    static boolean isBootCompleted() {
        return "1".equals(getSystemProperty("sys.boot_completed", "1"));
    }

    private static String getSystemProperty(String property, String defaultValue) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    /**
     * Ensures that a value is within given bounds. Specifically:
     * If value is less than lowerBound, return lowerBound; else if value is greater than upperBound,
     * return upperBound; else return value unchanged.
     */
    static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

    /**
     * @see #boundToRange(int, int, int).
     */
    public static float boundToRange(float value, float lowerBound, float upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

    /**
     * Wraps a message with a TTS span, so that a different message is spoken than
     * what is getting displayed.
     *
     * @param msg    original message
     * @param ttsMsg message to be spoken
     */

    public static CharSequence wrapForTts(CharSequence msg, String ttsMsg) {
        SpannableString spanned = new SpannableString(msg);
        spanned.setSpan(new TtsSpan.TextBuilder(ttsMsg).build(),
                0, spanned.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spanned;
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(
                LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    static boolean isPowerSaverOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isPowerSaveMode();
    }

    public static boolean isWallpaperAllowed(Context context) {
        if (isNycOrAbove()) {
            try {
                WallpaperManager wm = context.getSystemService(WallpaperManager.class);
                return (Boolean) wm.getClass().getDeclaredMethod("isSetWallpaperAllowed")
                        .invoke(wm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns whether the collection is null or empty.
     */
    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    public static int getColorAccent(Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, value, true);
        return value.data;

    }

    public static void sendCustomAccessibilityEvent(View target, int type, String text) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                target.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            target.onInitializeAccessibilityEvent(event);
            event.getText().add(text);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    /**
     * An extension of {@link BitmapDrawable} which returns the bitmap pixel size as intrinsic size.
     * This allows the badging to be done based on the action bitmap size rather than
     * the scaled bitmap size.
     */
    public static class FixedSizeBitmapDrawable extends BitmapDrawable {

        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super(null, bitmap);
        }

        @Override
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }
}

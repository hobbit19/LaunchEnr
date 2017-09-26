package com.enrico.launcher3.settings;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;

import com.enrico.launcher3.AndroidVersion;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.dynamicui.ExtractedColors;

/**
 * Created by Enrico on 23/09/2017.
 */

public class PreferencesState {

    public static boolean isAllowRotationPrefEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.ALLOW_ROTATION_PREFERENCE_KEY,
                getAllowRotationDefaultValue(context));
    }

    static boolean getAllowRotationDefaultValue(Context context) {
        if (AndroidVersion.isAtLeastNougat) {
            // If the device was scaled, used the original dimensions to determine if rotation
            // is allowed of not.
            Resources res = context.getResources();
            int originalSmallestWidth = res.getConfiguration().smallestScreenWidthDp
                    * res.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEVICE_STABLE;
            return originalSmallestWidth >= 600;
        }
        return false;
    }

    public static boolean isPinchToOverviewPrefEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.PINCH_OVERVIEW_PREFERENCE_KEY, false);
    }

    public static boolean isLightStatusBarPrefEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.LIGHT_STATUS_BAR_PREFERENCE_KEY, false);
    }

    public static boolean isShowBadgePrefEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.SHOW_BADGE_PREFERENCE_KEY, false);
    }

    //is dark theme enabled?
    public static boolean isDarkThemeEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.THEME_PREFERENCE_KEY,
                false);
    }

    //resolve hotseat options
    public static int resolveHotseatColor(Context context) {

        String hotseatColor = Utilities.getPrefs(context).getString(PreferenceKeys.HOTSEAT_COLOR_PREFERENCE_KEY, String.valueOf(24));

        switch (Integer.parseInt(hotseatColor)) {
            default:
            case 19:
                return ExtractedColors.HOTSEAT_INDEX;

            case 20:

                return Color.TRANSPARENT;

            case 21:
                return ExtractedColors.VIBRANT_INDEX;
        }
    }

    //is accent colored hotseat enabled?
    public static boolean isAccentColorHotseat(Context context) {

        String choice = Utilities.getPrefs(context).getString(PreferenceKeys.HOTSEAT_COLOR_PREFERENCE_KEY, String.valueOf(24));

        int value = Integer.parseInt(choice);
        return value == 22;
    }

    //are colored folders enabled?
    public static boolean areColoredFoldersEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.COLOR_FOLDERS_PREFERENCE_KEY,
                false);
    }

    //is unread count enabled
    public static boolean isUnreadCount(Context context) {
        return Utilities.getPrefs(context).getBoolean(PreferenceKeys.NUMERIC_BADGE_PREFERENCE_KEY,
                false);
    }
}

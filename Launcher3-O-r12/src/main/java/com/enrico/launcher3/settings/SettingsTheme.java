package com.enrico.launcher3.settings;

import android.content.Context;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

/**
 * Created by Enrico on 24/09/2017.
 */

public class SettingsTheme {

    //method to apply selected theme
    public static void apply(Context context) {
        int theme = resolveTheme(context);
        context.getTheme().applyStyle(theme, true);
    }

    //multi-preference dialog for theme options
    private static int resolveTheme(Context context) {

        boolean isDark = PreferencesState.isDarkThemeEnabled(context);
        //light theme
        int def = isDark ? R.style.Secondary_Theme_Dark : R.style.Secondary_Theme;

        //other themes
        int red = isDark ? R.style.Secondary_Theme_Red_Dark : R.style.Secondary_Theme_Red;
        int pink = isDark ? R.style.Secondary_Theme_Pink_Dark : R.style.Secondary_Theme_Pink;
        int purple = isDark ? R.style.Secondary_Theme_Purple_Dark : R.style.Secondary_Theme_Purple;
        int deep_purple = isDark ? R.style.Secondary_Theme_DeepPurple_Dark : R.style.Secondary_Theme_DeepPurple;
        int indigo = isDark ? R.style.Secondary_Theme_Indigo_Dark : R.style.Secondary_Theme_Indigo;
        int blue = isDark ? R.style.Secondary_Theme_Blue_Dark : R.style.Secondary_Theme_Blue;
        int light_blue = isDark ? R.style.Secondary_Theme_LightBlue_Dark : R.style.Secondary_Theme_LightBlue;
        int cyan = isDark ? R.style.Secondary_Theme_Cyan_Dark : R.style.Secondary_Theme_Cyan;
        int teal = isDark ? R.style.Secondary_Theme_Teal_Dark : R.style.Secondary_Theme_Teal;
        int green = isDark ? R.style.Secondary_Theme_Green_Dark : R.style.Secondary_Theme_Green;
        int amber = isDark ? R.style.Secondary_Theme_Amber_Dark : R.style.Secondary_Theme_Amber;
        int orange = isDark ? R.style.Secondary_Theme_Orange_Dark : R.style.Secondary_Theme_Orange;
        int deep_orange = isDark ? R.style.Secondary_Theme_DeepOrange_Dark : R.style.Secondary_Theme_DeepOrange;
        int brown = isDark ? R.style.Secondary_Theme_Brown_Dark : R.style.Secondary_Theme_Brown;
        int blue_grey = isDark ? R.style.Secondary_Theme_BlueGrey_Dark : R.style.Secondary_Theme_BlueGrey;

        String choice = Utilities.getPrefs(context)
                .getString(PreferenceKeys.CHOOSE_THEME_PREFERENCE_KEY, String.valueOf(0));

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
                return deep_purple;

            case 5:
                return indigo;

            case 6:
                return blue;

            case 7:
                return light_blue;

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
                return deep_orange;

            case 14:
                return brown;

            case 15:
                return blue_grey;

        }
    }
}

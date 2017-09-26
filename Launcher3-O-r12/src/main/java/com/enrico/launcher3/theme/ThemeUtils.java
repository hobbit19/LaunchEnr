/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enrico.launcher3.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.view.ContextThemeWrapper;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.settings.PreferenceKeys;
import com.enrico.launcher3.settings.PreferencesState;

/**
 * Various utility methods associated with theming.
 */
public class ThemeUtils {

    //method to apply selected theme
    public static void applyTheme(Context context) {
        int theme = resolveTheme(context);
        context.getTheme().applyStyle(theme, true);
    }

    //multi-preference dialog for theme options
    private static int resolveTheme(Context context) {

        boolean isDark = PreferencesState.isDarkThemeEnabled(context);
        //light theme
        int def = isDark ? R.style.Base_Theme_Dark : R.style.Base_Theme;

        //other themes
        int red = isDark ? R.style.Base_Theme_Red_Dark : R.style.Base_Theme_Red;
        int pink = isDark ? R.style.Base_Theme_Pink_Dark : R.style.Base_Theme_Pink;
        int purple = isDark ? R.style.Base_Theme_Purple_Dark : R.style.Base_Theme_Purple;
        int deep_purple = isDark ? R.style.Base_Theme_DeepPurple_Dark : R.style.Base_Theme_DeepPurple;
        int indigo = isDark ? R.style.Base_Theme_Indigo_Dark : R.style.Base_Theme_Indigo;
        int blue = isDark ? R.style.Base_Theme_Blue_Dark : R.style.Base_Theme_Blue;
        int light_blue = isDark ? R.style.Base_Theme_LightBlue_Dark : R.style.Base_Theme_LightBlue;
        int cyan = isDark ? R.style.Base_Theme_Cyan_Dark : R.style.Base_Theme_Cyan;
        int teal = isDark ? R.style.Base_Theme_Teal_Dark : R.style.Base_Theme_Teal;
        int green = isDark ? R.style.Base_Theme_Green_Dark : R.style.Base_Theme_Green;
        int amber = isDark ? R.style.Base_Theme_Amber_Dark : R.style.Base_Theme_Amber;
        int orange = isDark ? R.style.Base_Theme_Orange_Dark : R.style.Base_Theme_Orange;
        int deep_orange = isDark ? R.style.Base_Theme_DeepOrange_Dark : R.style.Base_Theme_DeepOrange;
        int brown = isDark ? R.style.Base_Theme_Brown_Dark : R.style.Base_Theme_Brown;
        int blue_grey = isDark ? R.style.Base_Theme_BlueGrey_Dark : R.style.Base_Theme_BlueGrey;

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

    public static int getColorAccent(Context context) {
        return getAttrColor(context, android.R.attr.colorAccent);
    }

    public static int getColorPrimary(Context context, int theme) {
        return getAttrColor(new ContextThemeWrapper(context, theme), android.R.attr.colorPrimary);
    }

    public static int getAttrColor(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    /**
     * Returns the alpha corresponding to the theme attribute {@param attr}, in the range [0, 255].
     */
    public static int getAlpha(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        float alpha = ta.getFloat(0, 0);
        ta.recycle();
        return (int) (255 * alpha + 0.5f);
    }

    /**
     * Scales a color matrix such that, when applied to color R G B A, it produces R' G' B' A' where
     * R' = r * R
     * G' = g * G
     * B' = b * B
     * A' = a * A
     * <p>
     * The matrix will, for instance, turn white into r g b a, and black will remain black.
     *
     * @param color  The color r g b a
     * @param target The ColorMatrix to scale
     */
    public static void setColorScaleOnMatrix(int color, ColorMatrix target) {
        target.setScale(Color.red(color) / 255f, Color.green(color) / 255f,
                Color.blue(color) / 255f, Color.alpha(color) / 255f);
    }
}

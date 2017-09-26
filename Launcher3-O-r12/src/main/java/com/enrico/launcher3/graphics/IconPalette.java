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

package com.enrico.launcher3.graphics;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import com.enrico.launcher3.R;
import com.enrico.launcher3.theme.ThemeUtils;

/**
 * Contains colors based on the dominant color of an icon.
 */
public class IconPalette {

    private static final float MIN_PRELOAD_COLOR_SATURATION = 0.2f;
    private static final float MIN_PRELOAD_COLOR_LIGHTNESS = 0.6f;

    private static IconPalette sFolderBadgePalette;

    public final int dominantColor;
    public final int backgroundColor;
    public final ColorMatrixColorFilter backgroundColorMatrixFilter;
    public final ColorMatrixColorFilter saturatedBackgroundColorMatrixFilter;
    public final int textColor;
    final int secondaryColor;

    private IconPalette(int color, boolean desaturateBackground) {
        dominantColor = color;
        backgroundColor = desaturateBackground ? getMutedColor(dominantColor, 0.87f) : dominantColor;
        ColorMatrix backgroundColorMatrix = new ColorMatrix();
        ThemeUtils.setColorScaleOnMatrix(backgroundColor, backgroundColorMatrix);
        backgroundColorMatrixFilter = new ColorMatrixColorFilter(backgroundColorMatrix);
        if (!desaturateBackground) {
            saturatedBackgroundColorMatrixFilter = backgroundColorMatrixFilter;
        } else {
            // Get slightly more saturated background color.
            ThemeUtils.setColorScaleOnMatrix(getMutedColor(dominantColor, 0.54f), backgroundColorMatrix);
            saturatedBackgroundColorMatrixFilter = new ColorMatrixColorFilter(backgroundColorMatrix);
        }
        textColor = getTextColorForBackground(backgroundColor);
        secondaryColor = getLowContrastColor(backgroundColor);
    }

    /**
     * Returns a color suitable for the progress bar color of preload icon.
     */
    int getPreloadProgressColor(Context context) {
        int result = dominantColor;

        // Make sure that the dominant color has enough saturation to be visible properly.
        float[] hsv = new float[3];
        Color.colorToHSV(result, hsv);
        if (hsv[1] < MIN_PRELOAD_COLOR_SATURATION) {
            result = ThemeUtils.getColorAccent(context);
        } else {
            hsv[2] = Math.max(MIN_PRELOAD_COLOR_LIGHTNESS, hsv[2]);
            result = Color.HSVToColor(hsv);
        }
        return result;
    }

    public static IconPalette fromDominantColor(int dominantColor, boolean desaturateBackground) {
        return new IconPalette(dominantColor, desaturateBackground);
    }

    /**
     * Returns an IconPalette based on the folder_badge_color in colors.xml.
     */
    public static @NonNull IconPalette getFolderBadgePalette(Resources resources) {
        if (sFolderBadgePalette == null) {
            int badgeColor = resources.getColor(R.color.folder_badge_color);
            sFolderBadgePalette = fromDominantColor(badgeColor, false);
        }
        return sFolderBadgePalette;
    }

    /**
     * Resolves a color such that it has enough contrast to be used as the
     * color of an icon or text on the given background color.
     *
     * @return a color of the same hue with enough contrast against the background.
     *
     * This was copied from com.android.internal.util.NotificationColorUtil.
     */
    public static int resolveContrastColor(Context context, int color, int background) {
        final int resolvedColor = resolveColor(context, color);
        return ensureTextContrast(resolvedColor, background);
    }

    /**
     * Resolves {@param color} to an actual color if it is {@link Notification#COLOR_DEFAULT}
     *
     * This was copied from com.android.internal.util.NotificationColorUtil.
     */
    private static int resolveColor(Context context, int color) {
        if (color == Notification.COLOR_DEFAULT) {
            return ContextCompat.getColor(context, R.color.notification_icon_default_color);
        }
        return color;
    }

    /**
     * Finds a text color with sufficient contrast over bg that has the same hue as the original
     * color.
     *
     * This was copied from com.android.internal.util.NotificationColorUtil.
     */
    private static int ensureTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 4.5);
    }
    /**
     * Finds a suitable color such that there's enough contrast.
     *
     * @param color the color to start searching from.
     * @param other the color to ensure contrast against. Assumed to be lighter than {@param color}
     * @param findFg if true, we assume {@param color} is a foreground, otherwise a background.
     * @param minRatio the minimum contrast ratio required.
     * @return a color with the same hue as {@param color}, potentially darkened to meet the
     *          contrast ratio.
     *
     * This was copied from com.android.internal.util.NotificationColorUtil.
     */
    private static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtils.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }

        double[] lab = new double[3];
        ColorUtils.colorToLAB(findFg ? fg : bg, lab);

        double low = 0, high = lab[0];
        final double a = lab[1], b = lab[2];
        for (int i = 0; i < 15 && high - low > 0.00001; i++) {
            final double l = (low + high) / 2;
            if (findFg) {
                fg = ColorUtils.LABToColor(l, a, b);
            } else {
                bg = ColorUtils.LABToColor(l, a, b);
            }
            if (ColorUtils.calculateContrast(fg, bg) > minRatio) {
                low = l;
            } else {
                high = l;
            }
        }
        return ColorUtils.LABToColor(low, a, b);
    }

    private static int getMutedColor(int color, float whiteScrimAlpha) {
        int whiteScrim = ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * whiteScrimAlpha));
        return ColorUtils.compositeColors(whiteScrim, color);
    }

    private static int getTextColorForBackground(int backgroundColor) {
        return getLighterOrDarkerVersionOfColor(backgroundColor, 4.5f);
    }

    private static int getLowContrastColor(int color) {
        return getLighterOrDarkerVersionOfColor(color, 1.5f);
    }

    public static int getLighterOrDarkerVersionOfColor(int color, float contrastRatio) {
        int whiteMinAlpha = ColorUtils.calculateMinimumAlpha(Color.WHITE, color, contrastRatio);
        int blackMinAlpha = ColorUtils.calculateMinimumAlpha(Color.BLACK, color, contrastRatio);
        int translucentWhiteOrBlack;
        if (whiteMinAlpha >= 0) {
            translucentWhiteOrBlack = ColorUtils.setAlphaComponent(Color.WHITE, whiteMinAlpha);
        } else if (blackMinAlpha >= 0) {
            translucentWhiteOrBlack = ColorUtils.setAlphaComponent(Color.BLACK, blackMinAlpha);
        } else {
            translucentWhiteOrBlack = Color.WHITE;
        }
        return ColorUtils.compositeColors(translucentWhiteOrBlack, color);
    }
}

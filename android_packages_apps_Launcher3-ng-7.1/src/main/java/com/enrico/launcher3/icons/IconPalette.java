package com.enrico.launcher3.icons;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;

import com.enrico.launcher3.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Enrico on 23/08/2017.
 */

class IconPalette {

    //the palette extracted from the bitmap
    private static ArrayList<Integer> palette(Palette p, int defaultColor) {

        ArrayList<Integer> extractedPalette = new ArrayList<>();

        //get all palettes
        Integer lightVibrant, vibrant, darkVibrant, lightMuted, muted, darkMuted;

        lightVibrant = p.getVibrantColor(defaultColor);
        vibrant = p.getVibrantColor(defaultColor);
        darkVibrant = p.getDarkVibrantColor(defaultColor);
        lightMuted = p.getLightMutedColor(defaultColor);
        muted = p.getMutedColor(defaultColor);
        darkMuted = p.getDarkMutedColor(defaultColor);

        extractedPalette.add(lightVibrant);
        extractedPalette.add(vibrant);
        extractedPalette.add(darkVibrant);
        extractedPalette.add(lightMuted);
        extractedPalette.add(muted);
        extractedPalette.add(darkMuted);

        //add also default color, because if the next method fails we have a color anyway
        extractedPalette.add(defaultColor);

        //pass these palettes to a hashset to avoid duplicates
        HashSet<Integer> hashSet = new HashSet<>();
        hashSet.addAll(extractedPalette);

        //add back these values to the palettes array list
        extractedPalette.clear();
        extractedPalette.addAll(hashSet);

        return extractedPalette;
    }

    //method to return a non-zero color from icon
    static int getDominantColor(Context activity, Bitmap bitmap) {

        // assign pixel accent to default color
        int defaultColor = ContextCompat.getColor(activity, R.color.accent);

        //define the returned color: extracted color, and set it to default value
        int extractedColor = defaultColor;

        //generate palette from bitmap
        Palette p = Palette.from(bitmap).generate();

        int dominant = p.getDominantColor(defaultColor);

        //extract other palettes
        ArrayList<Integer> extractedPalette = palette(p, defaultColor);

        //we want the dominant color, so if this color is different from the default value return it!
        if (dominant != defaultColor) {

            extractedColor = dominant;

            //else, get random palettes from the array list and get the first value different from default color
        } else {

            int randomPalette = getRandomPalette(extractedPalette, defaultColor);

            while (randomPalette == defaultColor) {
                extractedColor = getRandomPalette(extractedPalette, defaultColor);
            }
        }

        //return the extracted color
        return getLighterOrDarkerVersionOfColor(extractedColor, 1.5f);
    }

    //get random palette from palettes
    private static int getRandomPalette(ArrayList<Integer> palettes, int defaultColor) {
        try {
            return palettes.get((new Random()).nextInt(palettes.size()));
        } catch (Throwable e) {
            return defaultColor;
        }
    }

    //method to lighten or darken a color
    private static int getLighterOrDarkerVersionOfColor(int i, float f) {
        int i2 = -1;
        int arf = ColorUtils.calculateMinimumAlpha(-1, i, f);
        int arf2 = ColorUtils.calculateMinimumAlpha(0x1000000, i, f);
        if (arf >= 0) {
            i2 = ColorUtils.setAlphaComponent(-1, arf);
        } else if (arf2 >= 0) {
            i2 = ColorUtils.setAlphaComponent(0x1000000, arf2);
        }
        return ColorUtils.compositeColors(i2, i);
    }
}

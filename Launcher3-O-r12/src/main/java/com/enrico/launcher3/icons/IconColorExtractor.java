package com.enrico.launcher3.icons;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.enrico.launcher3.R;
import com.enrico.launcher3.graphics.IconPalette;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Enrico on 23/08/2017.
 */

public class IconColorExtractor {

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
    public static int get(Context context, Bitmap bitmap) {

        // assign pixel accent to default color
        int defaultColor = ContextCompat.getColor(context,R.color.badge_color);

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
        return IconPalette.getLighterOrDarkerVersionOfColor(extractedColor, 1.5f);
    }

    //get random palette from palettes
    private static int getRandomPalette(ArrayList<Integer> palettes, int defaultColor) {
        try {
            return palettes.get((new Random()).nextInt(palettes.size()));
        } catch (Throwable e) {
            return defaultColor;
        }
    }
}

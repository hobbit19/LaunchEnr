package com.enrico.launcher3.dynamicui;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;

import com.enrico.launcher3.LauncherProvider;
import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

/**
 * Extracts colors from the wallpaper, and saves results to {@link LauncherProvider}.
 */
public class ColorExtractionService extends IntentService {

    /**
     * The fraction of the wallpaper to extract colors for use on the hotseat.
     */
    private static final float HOTSEAT_FRACTION = 1f / 4;

    public ColorExtractionService() {
        super("ColorExtractionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        int wallpaperId = ExtractionUtils.getWallpaperId(wallpaperManager);
        ExtractedColors extractedColors = new ExtractedColors();
        if (wallpaperManager.getWallpaperInfo() != null) {
            // We can't extract colors from live wallpapers, so just use the default color always.
            extractedColors.updatePalette(null);
            extractedColors.updateHotseatPalette(null);
        } else {
            Bitmap wallpaper = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();
            Palette palette = Palette.from(wallpaper).generate();
            extractedColors.updatePalette(palette);
            // We extract colors for the hotseat and status bar separately,
            // since they only consider part of the wallpaper.
            Palette hotseatPalette = Palette.from(wallpaper)
                    .setRegion(0, (int) (wallpaper.getHeight() * (1f - HOTSEAT_FRACTION)),
                            wallpaper.getWidth(), wallpaper.getHeight())
                    .clearFilters()
                    .generate();
            extractedColors.updateHotseatPalette(hotseatPalette);

            if (Utilities.areLightBarsEnabled(getBaseContext())) {
                int statusBarHeight = getResources()
                        .getDimensionPixelSize(R.dimen.status_bar_height);
                Palette statusBarPalette = Palette.from(wallpaper)
                        .setRegion(0, 0, wallpaper.getWidth(), statusBarHeight)
                        .clearFilters()
                        .generate();
                extractedColors.updateStatusBarPalette(statusBarPalette);

                int navigationBarHeight = getResources()
                        .getDimensionPixelSize(R.dimen.navigation_bar_height);
                Palette navigationBarPalette = Palette.from(wallpaper)
                        .setRegion(0, wallpaper.getHeight() - navigationBarHeight, wallpaper.getWidth(), wallpaper.getHeight())
                        .clearFilters()
                        .generate();
                extractedColors.updateNavigationBarPalette(navigationBarPalette);
            }
        }

        // Save the extracted colors and wallpaper id to LauncherProvider.
        String colorsString = extractedColors.encodeAsString();
        Bundle extras = new Bundle();
        extras.putInt(LauncherSettings.Settings.EXTRA_WALLPAPER_ID, wallpaperId);
        extras.putString(LauncherSettings.Settings.EXTRA_EXTRACTED_COLORS, colorsString);
        getContentResolver().call(
                LauncherSettings.Settings.CONTENT_URI,
                LauncherSettings.Settings.METHOD_SET_EXTRACTED_COLORS_AND_WALLPAPER_ID,
                null, extras);
    }
}

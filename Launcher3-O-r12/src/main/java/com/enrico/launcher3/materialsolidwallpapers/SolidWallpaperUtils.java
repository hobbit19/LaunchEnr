package com.enrico.launcher3.materialsolidwallpapers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.enrico.launcher3.R;

import java.io.IOException;

/**
 * Created by Enrico on 21/07/2017.
 */

public class SolidWallpaperUtils {

    //create an array of colors that will populate the recycler view
    public static int[] material_colors = new int[]{

            R.color.material_red_400,
            R.color.material_pink_400,
            R.color.material_purple_400,
            R.color.material_deep_purple_400,
            R.color.material_indigo_400,
            R.color.material_blue_400,
            R.color.material_light_blue_400,
            R.color.material_cyan_400,
            R.color.material_teal_400,
            R.color.material_green_400,
            R.color.material_amber_400,
            R.color.material_orange_400,
            R.color.material_deep_orange_400,
            R.color.material_brown_400,
            R.color.material_blue_grey_400,
            R.color.black,
            R.color.white
    };

    //set the solid color
    @SuppressWarnings("NewApi")
    static void setWallpaper(final Activity activity, final WallpaperManager myWallpaperManager, int color) {

        try {

            //create a bitmap
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;

            final Bitmap bmp = Bitmap.createBitmap(1, 1, conf);

            bmp.eraseColor(color);

            //enhance the wallpaper activity to ask what wallpaper to set for android version >=Nougat
            //credits goes to Omni rom: https://github.com/omnirom/android_packages_apps_Gallery2/commit/8fe8f24c051641b8c12f1e63282847220a851a61
            if (android.os.Build.VERSION.SDK_INT >= 24) {

                final int DEFAULT_WALLPAPER_TYPE = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;

                AlertDialog.Builder wallpaperTypeDialog = new AlertDialog.Builder(activity);
                wallpaperTypeDialog.setTitle(activity.getResources().getString(R.string.wallpaper_type_dialog_title));
                wallpaperTypeDialog.setItems(R.array.wallpaper_type_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int wallpaperType = DEFAULT_WALLPAPER_TYPE;

                        if (item == 1) {
                            wallpaperType = WallpaperManager.FLAG_SYSTEM;
                        } else if (item == 2) {
                            wallpaperType = WallpaperManager.FLAG_LOCK;
                        }

                        try {
                            //noinspection WrongConstant
                            myWallpaperManager.setBitmap(bmp, null, true, wallpaperType);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                AlertDialog d = wallpaperTypeDialog.create();
                d.show();

            } else {

                myWallpaperManager.clear();
                myWallpaperManager.setBitmap(bmp);

            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    //get colorView color
    static int getColorViewColor(View colorView) {
        int color = Color.TRANSPARENT;
        Drawable background = colorView.getBackground();
        if (background instanceof ColorDrawable)
            color = ((ColorDrawable) background).getColor();
        return color;
    }
}

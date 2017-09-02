package com.enrico.launcher3.icons;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.enrico.launcher3.R;

import java.util.HashMap;

public class RoundIconsColorProvider {

    private static HashMap<String, Integer> extractedColors = new HashMap<>();

    static int getRoundIconColor(Context context, String packageName) {

        int tmp = ContextCompat.getColor(context, R.color.material_light_o);

        if (extractedColors.get(packageName) != null) {
            tmp = extractedColors.get(packageName);
        }

        return tmp;
    }

    private static void addPackageColor(String packageName, int iconColor) {

        if (extractedColors.get(packageName) == null) {
            extractedColors.put(packageName, iconColor);
        }
    }

    public static void init(Context context, String packageName) {

        new loadIconColor(context, packageName).execute();
    }

    //async class to load icon color
    private static class loadIconColor extends AsyncTask<Void, Void, Void> {

        private Context context;

        //package shit
        private int iconColor;
        private String packageName;

        private loadIconColor(Context context, String packageName) {
            this.context = context;
            this.packageName = packageName;
        }

        @Override
        protected Void doInBackground(Void... params) {

            //get default icon for package
            Bitmap packageBitmap = IconCache.getIconsHandler(context).getDefaultAppDrawable(packageName);
            iconColor = IconPalette.getDominantColor(context, packageBitmap);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            addPackageColor(packageName, iconColor);
        }
    }
}

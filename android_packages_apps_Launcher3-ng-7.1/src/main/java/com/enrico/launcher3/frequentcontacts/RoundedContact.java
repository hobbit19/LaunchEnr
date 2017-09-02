package com.enrico.launcher3.frequentcontacts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * Created by Enrico on 03/08/2017.
 */

class RoundedContact {

    static RoundedBitmapDrawable get(Activity activity, Uri thumbnail) {

        RoundedBitmapDrawable dr = null;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), thumbnail);
            dr = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
            dr.setCircular(true);
        } catch (Exception e) {
            //handle exception
        }
        return dr;
    }
}

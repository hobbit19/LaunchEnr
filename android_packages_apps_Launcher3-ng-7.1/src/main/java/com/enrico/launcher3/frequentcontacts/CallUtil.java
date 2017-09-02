package com.enrico.launcher3.frequentcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.enrico.launcher3.authorizations.PermissionUtils;

/**
 * Created by Enrico on 02/08/2017.
 */

class CallUtil {

    static void performCall(Activity activity, String number) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestPermissionWithRationale(activity, PermissionUtils.CALL_REQUEST_CODE);

            } else {

                call(activity, number);
            }

        } else {
            call(activity, number);
        }

    }

    private static void call(Activity activity, String number) {

        try {
            Uri call = Uri.parse("tel:" + number);
            Intent surf = new Intent(Intent.ACTION_CALL, call);
            activity.startActivity(surf);

        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
}

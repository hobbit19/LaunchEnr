package com.enrico.launcher3;

import android.os.Build;

/**
 * Created by Enrico on 23/09/2017.
 */

public class AndroidVersion {

    public static boolean isAtLeastOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static final boolean isAtLeastNougatMR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;

    public static final boolean isAtLeastNougat =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

    public static final boolean isAtLeastMarshmallow =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean isAtLeastLollipopMR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
}

package com.enrico.launcher3;

import android.content.Context;

import java.util.Set;

/**
 * Created by Enrico on 20/08/2017.
 */

public class HiddenAppUtils {

    public static boolean isPackageHidden(Context context, String packageName) {

        Set<String> hiddenApps = Utilities.getPrefs(context).getStringSet(Utilities.KEY_HIDDEN_APPS, null);

        boolean tmp = false;

        if (hiddenApps != null && !hiddenApps.isEmpty()) {
            tmp = hiddenApps.contains(packageName);
        }
        return tmp;
    }
}

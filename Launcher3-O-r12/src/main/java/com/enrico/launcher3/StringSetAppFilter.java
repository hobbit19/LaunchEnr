package com.enrico.launcher3;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Set;

class StringSetAppFilter implements AppFilter {

    @Override
    public boolean shouldShowApp(String packageName, Context context) {

        Set<String> hiddenApps = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(HiddenAppUtils.KEY_HIDDEN_APPS_SET, null);
        return hiddenApps == null || !hiddenApps.contains(packageName);
    }
}

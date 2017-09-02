package com.enrico.launcher3.hiddenapps;

import android.content.ComponentName;
import android.content.Context;

import com.enrico.launcher3.AppFilter;
import com.enrico.launcher3.Utilities;

import java.util.Set;

public class StringSetAppFilter implements AppFilter {

    @Override
    public boolean shouldShowApp(ComponentName app, Context context) {
        Set<String> hiddenApps = Utilities.getPrefs(context).getStringSet(Utilities.HIDDEN_APPS_SET_KEY, null);
        return hiddenApps == null || !hiddenApps.contains(app.flattenToString());
    }
}

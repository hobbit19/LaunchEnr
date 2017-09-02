package com.enrico.launcher3;

import android.content.ComponentName;
import android.content.Context;

public interface AppFilter {

    boolean shouldShowApp(ComponentName app, Context context);
}

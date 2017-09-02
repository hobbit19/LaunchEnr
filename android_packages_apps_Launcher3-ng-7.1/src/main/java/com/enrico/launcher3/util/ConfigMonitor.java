package com.enrico.launcher3.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;

/**
 * {@link BroadcastReceiver} which watches configuration changes and
 * restarts the process in case changes which affect the device profile occur.
 */
public class ConfigMonitor extends BroadcastReceiver {

    private final Context mContext;
    private final float mFontScale;
    private final int mDensity;

    public ConfigMonitor(Context context) {
        mContext = context;

        Configuration config = context.getResources().getConfiguration();
        mFontScale = config.fontScale;
        mDensity = getDensity(config);
    }

    private static int getDensity(Configuration config) {
        return config.densityDpi;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Configuration config = context.getResources().getConfiguration();
        if (mFontScale != config.fontScale || mDensity != getDensity(config)) {

            mContext.unregisterReceiver(this);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void register() {
        mContext.registerReceiver(this, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
    }
}

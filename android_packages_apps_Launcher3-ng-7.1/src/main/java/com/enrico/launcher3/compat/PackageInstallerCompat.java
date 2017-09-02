package com.enrico.launcher3.compat;

import android.content.Context;

import java.util.HashMap;

public abstract class PackageInstallerCompat {

    public static final int STATUS_INSTALLED = 0;
    public static final int STATUS_FAILED = 2;
    static final int STATUS_INSTALLING = 1;
    private static final Object sInstanceLock = new Object();
    private static PackageInstallerCompat sInstance;

    public static PackageInstallerCompat getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new PackageInstallerCompatVL(context);
            }
            return sInstance;
        }
    }

    /**
     * @return a map of active installs to their progress
     */
    public abstract HashMap<String, Integer> updateAndGetActiveSessionCache();

    public static final class PackageInstallInfo {
        public final String packageName;

        public int state;
        public int progress;

        PackageInstallInfo(String packageName, int state, int progress) {
            this.packageName = packageName;
            this.state = state;
            this.progress = progress;
        }
    }
}

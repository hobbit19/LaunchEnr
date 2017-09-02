package com.enrico.launcher3.compat;

import android.content.Context;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionCallback;
import android.content.pm.PackageInstaller.SessionInfo;
import android.os.Handler;
import android.os.UserHandle;
import android.util.SparseArray;

import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.LauncherModel;
import com.enrico.launcher3.icons.IconCache;
import com.enrico.launcher3.util.Thunk;

import java.util.HashMap;

class PackageInstallerCompatVL extends PackageInstallerCompat {

    @Thunk
    private final SparseArray<String> mActiveSessions = new SparseArray<>();

    @Thunk
    private final PackageInstaller mInstaller;
    private final IconCache mCache;
    private final Handler mWorker;
    private final SessionCallback mCallback = new SessionCallback() {

        @Override
        public void onCreated(int sessionId) {
            pushSessionDisplayToLauncher(sessionId);
        }

        @Override
        public void onFinished(int sessionId, boolean success) {
            // For a finished session, we can't get the session info. So use the
            // packageName from our local cache.
            String packageName = mActiveSessions.get(sessionId);
            mActiveSessions.remove(sessionId);

            if (packageName != null) {
                sendUpdate(new PackageInstallInfo(packageName,
                        success ? STATUS_INSTALLED : STATUS_FAILED, 0));
            }
        }

        @Override
        public void onProgressChanged(int sessionId, float progress) {
            SessionInfo session = mInstaller.getSessionInfo(sessionId);
            if (session != null && session.getAppPackageName() != null) {
                sendUpdate(new PackageInstallInfo(session.getAppPackageName(),
                        STATUS_INSTALLING,
                        (int) (session.getProgress() * 100)));
            }
        }

        @Override
        public void onActiveChanged(int sessionId, boolean active) {
        }

        @Override
        public void onBadgingChanged(int sessionId) {
            pushSessionDisplayToLauncher(sessionId);
        }

        private void pushSessionDisplayToLauncher(int sessionId) {
            SessionInfo session = mInstaller.getSessionInfo(sessionId);
            if (session != null && session.getAppPackageName() != null) {
                addSessionInfoToCache(session, UserHandleUtil.myUserHandle());
                LauncherAppState app = LauncherAppState.getInstance();

                if (app != null) {
                    app.getModel().updateSessionDisplayInfo(session.getAppPackageName());
                }
            }
        }
    };

    PackageInstallerCompatVL(Context context) {
        mInstaller = context.getPackageManager().getPackageInstaller();
        mCache = LauncherAppState.getInstance().getIconCache();
        mWorker = new Handler(LauncherModel.getWorkerLooper());

        mInstaller.registerSessionCallback(mCallback, mWorker);
    }

    @Override
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        HashMap<String, Integer> activePackages = new HashMap<>();
        UserHandle user = UserHandleUtil.myUserHandle();
        for (SessionInfo info : mInstaller.getAllSessions()) {
            addSessionInfoToCache(info, user);
            if (info.getAppPackageName() != null) {
                activePackages.put(info.getAppPackageName(), (int) (info.getProgress() * 100));
                mActiveSessions.put(info.getSessionId(), info.getAppPackageName());
            }
        }
        return activePackages;
    }

    @Thunk
    private void addSessionInfoToCache(SessionInfo info, UserHandle user) {
        String packageName = info.getAppPackageName();
        if (packageName != null) {
            mCache.cachePackageInstallInfo(packageName, user, info.getAppIcon(),
                    info.getAppLabel());
        }
    }

    @Thunk
    private void sendUpdate(PackageInstallInfo info) {
        LauncherAppState app = LauncherAppState.getInstanceNoCreate();
        if (app != null) {
            app.getModel().setPackageState(info);
        }
    }
}

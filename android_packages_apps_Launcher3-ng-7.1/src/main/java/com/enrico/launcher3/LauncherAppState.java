package com.enrico.launcher3;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.enrico.launcher3.compat.LauncherAppsCompat;
import com.enrico.launcher3.compat.UserManagerCompat;
import com.enrico.launcher3.dynamicui.ExtractionUtils;
import com.enrico.launcher3.icons.IconCache;
import com.enrico.launcher3.shortcuts.DeepShortcutManager;
import com.enrico.launcher3.shortcuts.ShortcutCache;
import com.enrico.launcher3.util.ConfigMonitor;
import com.enrico.launcher3.util.Thunk;

import java.lang.ref.WeakReference;

public class LauncherAppState {

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;
    private static LauncherAppState INSTANCE;
    @Thunk
    private final LauncherModel mModel;
    private final IconCache mIconCache;
    private final WidgetPreviewLoader mWidgetCache;
    private final DeepShortcutManager mDeepShortcutManager;
    @Thunk
    private boolean mWallpaperChangedSinceLastCheck;
    private InvariantDeviceProfile mInvariantDeviceProfile;

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        mInvariantDeviceProfile = new InvariantDeviceProfile(sContext);
        mIconCache = new IconCache(sContext, mInvariantDeviceProfile);
        mWidgetCache = new WidgetPreviewLoader(sContext, mIconCache);
        mDeepShortcutManager = new DeepShortcutManager(sContext, new ShortcutCache());

        AppFilter mAppFilter = new StringSetAppFilter();

        mModel = new LauncherModel(this, mIconCache, mAppFilter, mDeepShortcutManager);

        LauncherAppsCompat.getInstance(sContext).addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // For handling managed profiles
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);

        if (android.os.Build.VERSION.SDK_INT >= 24) {
            filter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
            filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
            filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED);
        }

        // For extracting colors from the wallpaper
        if (Utilities.isNycOrAbove()) {
            // TODO: add a broadcast entry to the manifest for pre-N.
            filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        }

        sContext.registerReceiver(mModel, filter);
        UserManagerCompat.getInstance(sContext).enableAndResetCache();

        new ConfigMonitor(sContext).register();

        if (Utilities.isNycOrAbove()) {
            ExtractionUtils.startColorExtractionServiceIfNecessary(sContext);
        } else {
            ExtractionUtils.startColorExtractionService(sContext);
        }
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    static void setLauncherProvider(LauncherProvider provider) {

        sLauncherProvider = new WeakReference<>(provider);

        // The content provider exists for the entire duration of the launcher main process and
        // is the first component to get created. Initializing application context here ensures
        // that LauncherAppState always exists in the main process.

        if (provider.getContext() != null) {
            sContext = provider.getContext().getApplicationContext();
        }
    }

    public Context getContext() {
        return sContext;
    }

    /**
     * Reloads the workspace items from the DB and re-binds the workspace. This should generally
     * not be called as DB updates are automatically followed by UI update
     */
   void reloadWorkspace() {
        mModel.resetLoadedState(false, true);
        mModel.startLoaderFromBackground();
    }

    LauncherModel setLauncher(Launcher launcher) {
        sLauncherProvider.get().setLauncherProviderChangeListener(launcher);
        mModel.initialize(launcher);
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return mWidgetCache;
    }

    public DeepShortcutManager getShortcutManager() {
        return mDeepShortcutManager;
    }

    boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }
}

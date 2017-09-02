package com.enrico.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.UserHandle;
import android.util.LruCache;

import java.util.HashMap;
import java.util.List;

/**
 * Loads {@link ShortcutInfoCompat}s on demand (e.g. when launcher
 * loads for pinned shortcuts and on long-press for dynamic shortcuts), and caches them
 * for handful of apps in an LruCache while launcher lives.
 */
@TargetApi(Build.VERSION_CODES.N)
public class ShortcutCache {

    private static final int CACHE_SIZE = 30; // Max number shortcuts we cache.

    private LruCache<ShortcutKey, ShortcutInfoCompat> mCachedShortcuts;
    // We always keep pinned shortcuts in the cache.
    private HashMap<ShortcutKey, ShortcutInfoCompat> mPinnedShortcuts;

    public ShortcutCache() {
        mCachedShortcuts = new LruCache<>(CACHE_SIZE);
        mPinnedShortcuts = new HashMap<>();
    }

 /*   */

    /**
     * Removes shortcuts from the cache when shortcuts change for a given package.
     * <p>
     * Returns a map of ids to their evicted shortcuts.
     *
     * @see android.content.pm.LauncherApps.Callback#onShortcutsChanged(String, List, UserHandle).
     *//*
    public void removeShortcuts(List<ShortcutInfoCompat> shortcuts) {
        for (ShortcutInfoCompat shortcut : shortcuts) {
            ShortcutKey key = ShortcutKey.fromInfo(shortcut);
            mCachedShortcuts.remove(key);
            mPinnedShortcuts.remove(key);
        }
    }*/
    public ShortcutInfoCompat get(ShortcutKey key) {
        if (mPinnedShortcuts.containsKey(key)) {
            return mPinnedShortcuts.get(key);
        }
        return mCachedShortcuts.get(key);
    }

    public void put(ShortcutKey key, ShortcutInfoCompat shortcut) {
        if (shortcut.isPinned()) {
            mPinnedShortcuts.put(key, shortcut);
        } else {
            mCachedShortcuts.put(key, shortcut);
        }
    }
}

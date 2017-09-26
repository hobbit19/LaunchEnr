/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enrico.launcher3.model;

import android.content.Context;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.MutableInt;

import com.enrico.launcher3.FolderInfo;
import com.enrico.launcher3.InstallShortcutReceiver;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherAppWidgetInfo;
import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.ShortcutInfo;
import com.enrico.launcher3.shortcuts.DeepShortcutManager;
import com.enrico.launcher3.shortcuts.ShortcutInfoCompat;
import com.enrico.launcher3.shortcuts.ShortcutKey;
import com.enrico.launcher3.util.ComponentKey;
import com.enrico.launcher3.util.LongArrayMap;
import com.enrico.launcher3.util.MultiHashMap;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * All the data stored in-memory and managed by the LauncherModel
 */
public class BgDataModel {

    /**
     * Map of all the ItemInfos (shortcuts, folders, and widgets) created by
     * LauncherModel to their ids
     */
    public final LongArrayMap<ItemInfo> itemsIdMap = new LongArrayMap<>();

    /**
     * List of all the folders and shortcuts directly on the home screen (no widgets
     * or shortcuts within folders).
     */
    public final ArrayList<ItemInfo> workspaceItems = new ArrayList<>();

    /**
     * All LauncherAppWidgetInfo created by LauncherModel.
     */
    public final ArrayList<LauncherAppWidgetInfo> appWidgets = new ArrayList<>();

    /**
     * Map of id to FolderInfos of all the folders created by LauncherModel
     */
    public final LongArrayMap<FolderInfo> folders = new LongArrayMap<>();

    /**
     * Ordered list of workspace screens ids.
     */
    public final ArrayList<Long> workspaceScreens = new ArrayList<>();

    /**
     * Map of ShortcutKey to the number of times it is pinned.
     */
    public final Map<ShortcutKey, MutableInt> pinnedShortcutCounts = new HashMap<>();

    /**
     * Maps all launcher activities to the id's of their shortcuts (if they have any).
     */
    public final MultiHashMap<ComponentKey, String> deepShortcutMap = new MultiHashMap<>();

    /**
     * Clears all the data
     */
    public synchronized void clear() {
        workspaceItems.clear();
        appWidgets.clear();
        folders.clear();
        itemsIdMap.clear();
        workspaceScreens.clear();
        pinnedShortcutCounts.clear();
        deepShortcutMap.clear();
    }

     public synchronized void dump(String prefix, FileDescriptor fd, PrintWriter writer,
             String[] args) {
        if (args.length > 0 && TextUtils.equals(args[0], "--proto")) {

            return;
        }
        writer.println(prefix + "Data Model:");
        writer.print(prefix + " ---- workspace screens: ");
        for (int i = 0; i < workspaceScreens.size(); i++) {
            writer.print(" " + workspaceScreens.get(i).toString());
        }
        writer.println();
        writer.println(prefix + " ---- workspace items ");
        for (int i = 0; i < workspaceItems.size(); i++) {
            writer.println(prefix + '\t' + workspaceItems.get(i).toString());
        }
        writer.println(prefix + " ---- appwidget items ");
        for (int i = 0; i < appWidgets.size(); i++) {
            writer.println(prefix + '\t' + appWidgets.get(i).toString());
        }
        writer.println(prefix + " ---- folder items ");
        for (int i = 0; i< folders.size(); i++) {
            writer.println(prefix + '\t' + folders.valueAt(i).toString());
        }
        writer.println(prefix + " ---- items id map ");
        for (int i = 0; i< itemsIdMap.size(); i++) {
            writer.println(prefix + '\t' + itemsIdMap.valueAt(i).toString());
        }

        if (args.length > 0 && TextUtils.equals(args[0], "--all")) {
            writer.println(prefix + "shortcuts");
            for (ArrayList<String> map : deepShortcutMap.values()) {
                writer.print(prefix + "  ");
                for (String str : map) {
                    writer.print(str.toString() + ", ");
                }
                writer.println();
            }
        }
    }

    synchronized void removeItem(Context context, ItemInfo... items) {
        removeItem(context, Arrays.asList(items));
    }

    synchronized void removeItem(Context context, Iterable<? extends ItemInfo> items) {
        for (ItemInfo item : items) {
            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    folders.remove(item.id);

                    workspaceItems.remove(item);
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT: {
                    // Decrement pinned shortcut count
                    ShortcutKey pinnedShortcut = ShortcutKey.fromItemInfo(item);
                    MutableInt count = pinnedShortcutCounts.get(pinnedShortcut);
                    if ((count == null || --count.value == 0)
                            && !InstallShortcutReceiver.getPendingShortcuts(context)
                                .contains(pinnedShortcut)) {
                        DeepShortcutManager.getInstance(context).unpinShortcut(pinnedShortcut);
                    }
                    // Fall through.
                }
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    workspaceItems.remove(item);
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                case LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET:
                    appWidgets.remove(item);
                    break;
            }
            itemsIdMap.remove(item.id);
        }
    }

    synchronized void addItem(Context context, ItemInfo item, boolean newItem) {
        itemsIdMap.put(item.id, item);
        switch (item.itemType) {
            case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                folders.put(item.id, (FolderInfo) item);
                workspaceItems.add(item);
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT: {
                // Increment the count for the given shortcut
                ShortcutKey pinnedShortcut = ShortcutKey.fromItemInfo(item);
                MutableInt count = pinnedShortcutCounts.get(pinnedShortcut);
                if (count == null) {
                    count = new MutableInt(1);
                    pinnedShortcutCounts.put(pinnedShortcut, count);
                } else {
                    count.value++;
                }

                // Since this is a new item, pin the shortcut in the system server.
                if (newItem && count.value == 1) {
                    DeepShortcutManager.getInstance(context).pinShortcut(pinnedShortcut);
                }
                // Fall through
            }
            case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
            case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
                        item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    workspaceItems.add(item);
                } else {
                    if (newItem) {
                        if (!folders.containsKey(item.container)) {
                            //do nothing
                        }
                    } else {
                        findOrMakeFolder(item.container).add((ShortcutInfo) item, false);
                    }

                }
                break;
            case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
            case LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET:
                appWidgets.add((LauncherAppWidgetInfo) item);
                break;
        }
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    public synchronized FolderInfo findOrMakeFolder(long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }

    /**
     * Clear all the deep shortcuts for the given package, and re-add the new shortcuts.
     */
    public synchronized void updateDeepShortcutMap(
            String packageName, UserHandle user, List<ShortcutInfoCompat> shortcuts) {
        if (packageName != null) {
            Iterator<ComponentKey> keysIter = deepShortcutMap.keySet().iterator();
            while (keysIter.hasNext()) {
                ComponentKey next = keysIter.next();
                if (next.componentName.getPackageName().equals(packageName)
                        && next.user.equals(user)) {
                    keysIter.remove();
                }
            }
        }

        // Now add the new shortcuts to the map.
        for (ShortcutInfoCompat shortcut : shortcuts) {
            boolean shouldShowInContainer = shortcut.isEnabled()
                    && (shortcut.isDeclaredInManifest() || shortcut.isDynamic());
            if (shouldShowInContainer) {
                ComponentKey targetComponent
                        = new ComponentKey(shortcut.getActivity(), shortcut.getUserHandle());
                deepShortcutMap.addToList(targetComponent, shortcut.getId());
            }
        }
    }
}

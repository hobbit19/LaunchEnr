package com.enrico.launcher3;

import android.content.ContentValues;
import android.content.Context;

import com.enrico.launcher3.compat.UserHandleUtil;

import java.util.ArrayList;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {

    public static final int NO_FLAGS = 0x00000000;

    /**
     * The folder is locked in sorted mode
     */
    public static final int FLAG_ITEMS_SORTED = 0x00000001;

    /**
     * It is a work folder
     */
    public static final int FLAG_WORK_FOLDER = 0x00000002;

    /**
     * The multi-page animation has run for this folder
     */
    public static final int FLAG_MULTI_PAGE_ANIMATION = 0x00000004;

    /**
     * Whether this folder has been opened
     */
    public boolean opened;

    public int options;

    /**
     * The apps and shortcuts
     */
    public ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();

    private ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    public FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
        user = UserHandleUtil.myUserHandle();
    }

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item, boolean animate) {
        contents.add(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAdd(item);
        }
        itemsChanged(animate);
    }

    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     */
    public void remove(ShortcutInfo item, boolean animate) {
        contents.remove(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onRemove(item);
        }
        itemsChanged(animate);
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onTitleChanged(title);
        }
    }

    @Override
    void onAddToDatabase(Context context, ContentValues values) {
        super.onAddToDatabase(context, values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
        values.put(LauncherSettings.Favorites.OPTIONS, options);

    }

    public void addListener(FolderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FolderListener listener) {
        listeners.remove(listener);
    }

    void itemsChanged(boolean animate) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged(animate);
        }
    }

    public boolean hasOption(int optionFlag) {
        return (options & optionFlag) != 0;
    }

    /**
     * @param option    flag to set or clear
     * @param isEnabled whether to set or clear the flag
     * @param context   if not null, save changes to the db.
     */
    public void setOption(int option, boolean isEnabled, Context context) {
        int oldOptions = options;
        if (isEnabled) {
            options |= option;
        } else {
            options &= ~option;
        }
        if (context != null && oldOptions != options) {
            LauncherModel.updateItemInDatabase(context, this);
        }
    }

    public interface FolderListener {
        void onAdd(ShortcutInfo item);

        void onRemove(ShortcutInfo item);

        void onTitleChanged(CharSequence title);

        void onItemsChanged(boolean animate);
    }
}

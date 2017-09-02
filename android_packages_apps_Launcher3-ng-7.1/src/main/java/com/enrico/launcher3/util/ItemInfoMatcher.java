package com.enrico.launcher3.util;

import android.content.ComponentName;
import android.os.UserHandle;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.LauncherSettings.Favorites;
import com.enrico.launcher3.ShortcutInfo;
import com.enrico.launcher3.shortcuts.ShortcutKey;

import java.util.HashSet;

/**
 * A utility class to check for {@link ItemInfo}
 */
public abstract class ItemInfoMatcher {

    public static ItemInfoMatcher ofComponent(final ComponentName componentName) {
        return new ItemInfoMatcher() {
            @Override
            public boolean matches(ItemInfo info, ComponentName cn) {

                return componentName.equals(cn);
            }
        };
    }

    public static ItemInfoMatcher ofComponents(
            final HashSet<ComponentName> components, final UserHandle user) {
        return new ItemInfoMatcher() {
            @Override
            public boolean matches(ItemInfo info, ComponentName cn) {
                return components.contains(cn) && info.user.equals(user);
            }
        };
    }

    public static ItemInfoMatcher ofPackages(
            final HashSet<String> packageNames, final UserHandle user) {
        return new ItemInfoMatcher() {
            @Override
            public boolean matches(ItemInfo info, ComponentName cn) {
                return packageNames.contains(cn.getPackageName()) && info.user.equals(user);
            }
        };
    }

    public static ItemInfoMatcher ofShortcutKeys(final HashSet<ShortcutKey> keys) {
        return new ItemInfoMatcher() {
            @Override
            public boolean matches(ItemInfo info, ComponentName cn) {
                return info.itemType == Favorites.ITEM_TYPE_DEEP_SHORTCUT &&
                        keys.contains(ShortcutKey.fromShortcutInfo((ShortcutInfo) info));
            }
        };
    }

    public abstract boolean matches(ItemInfo info, ComponentName cn);
}

package com.enrico.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.UserHandle;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.compat.LauncherActivityInfoCompat;
import com.enrico.launcher3.compat.UserManagerCompat;

/**
 * Wrapper class for {@link android.content.pm.ShortcutInfo}, representing deep shortcuts into apps.
 * <p>
 * Not to be confused with {@link com.enrico.launcher3.ShortcutInfo}.
 */
@TargetApi(Build.VERSION_CODES.N_MR1)
public class ShortcutInfoCompat {
    public static final String EXTRA_SHORTCUT_ID = "shortcut_id";
    private static final String INTENT_CATEGORY = "com.enrico.launcher3.DEEP_SHORTCUT";
    private ShortcutInfo mShortcutInfo;

    public ShortcutInfoCompat(ShortcutInfo shortcutInfo) {
        mShortcutInfo = shortcutInfo;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Intent makeIntent(Context context) {
        long serialNumber = UserManagerCompat.getInstance(context)
                .getSerialNumberForUser(getUserHandle());
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(INTENT_CATEGORY)
                .setComponent(getActivity())
                .setPackage(getPackage())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .putExtra(ItemInfo.EXTRA_PROFILE, serialNumber)
                .putExtra(EXTRA_SHORTCUT_ID, getId());
    }

    ShortcutInfo getShortcutInfo() {
        return mShortcutInfo;
    }

    public String getPackage() {
        return mShortcutInfo.getPackage();
    }

    public String getId() {
        return mShortcutInfo.getId();
    }

    public CharSequence getShortLabel() {
        return mShortcutInfo.getShortLabel();
    }

    public CharSequence getLongLabel() {
        return mShortcutInfo.getLongLabel();
    }

    public ComponentName getActivity() {
        return mShortcutInfo.getActivity();
    }

    public UserHandle getUserHandle() {

      return mShortcutInfo.getUserHandle();

    }

    public boolean isPinned() {
        return mShortcutInfo.isPinned();
    }

    public boolean isDeclaredInManifest() {
        return mShortcutInfo.isDeclaredInManifest();
    }

    public boolean isEnabled() {
        return mShortcutInfo.isEnabled();
    }

    public boolean isDynamic() {
        return mShortcutInfo.isDynamic();
    }

    public int getRank() {
        return mShortcutInfo.getRank();
    }

    public CharSequence getDisabledMessage() {
        return mShortcutInfo.getDisabledMessage();
    }

    @Override
    public String toString() {
        return mShortcutInfo.toString();
    }

    public LauncherActivityInfoCompat getActivityInfo(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(getActivity());
        return LauncherActivityInfoCompat.create(context, getUserHandle(), intent);

    }
}

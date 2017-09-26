package com.enrico.launcher3.icons;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.quicksettings.TileService;

import com.enrico.launcher3.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by Enrico on 05/08/2017.
 */

@TargetApi(24)
public class RandomIconsTile extends TileService {

    public static boolean fromTile;

    private static String getRandomIconPack(ArrayList<String> iconPacks) {
        try {
            return iconPacks.get((new Random()).nextInt(iconPacks.size()));
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();

    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!isSecure()) {

            applyRandomIcon();

        } else {

            unlockAndRun(new Runnable() {
                @Override
                public void run() {

                    applyRandomIcon();
                }
            });
        }
    }

    private void applyRandomIcon() {

        fromTile = true;

        String latestIconPack = Utilities.getPrefs(getBaseContext()).getString(IconsManager.ICON_PACK_PREFERENCE_KEY, "");

        String randomIconPack = getRandomIconPack(loadAvailableIconPacks());

        while (latestIconPack.equals(randomIconPack)) {
            randomIconPack = getRandomIconPack(loadAvailableIconPacks());
        }

        IconsManager.switchIconPacks(randomIconPack,getBaseContext());
        Utilities.getPrefs(getBaseContext()).edit().putString(IconsManager.ICON_PACK_PREFERENCE_KEY, randomIconPack).apply();
    }

    private ArrayList<String> loadAvailableIconPacks() {

        ArrayList<String> iconPacks = new ArrayList<>();

        PackageManager pm = getBaseContext().getPackageManager();

        List<ResolveInfo> list;
        list = pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), 0);
        list.addAll(pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), 0));
        list.addAll(pm.queryIntentActivities(new Intent("com.dlto.atom.launcher.THEME"), 0));
        list.addAll(pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), 0));
        list.addAll(pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"), 0));
        list.addAll(pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("com.teslacoilsw.launcher.THEME"), 0));
        list.addAll(pm.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("com.fede.launcher.THEME_ICONPACK"), 0));
        for (ResolveInfo info : list) {
            iconPacks.add(info.activityInfo.packageName);
        }
        HashSet<String> hashSet = new HashSet<>();
        hashSet.addAll(iconPacks);
        iconPacks.clear();
        iconPacks.addAll(hashSet);
        iconPacks.add(0, "");
        return iconPacks;
    }
}
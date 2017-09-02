package com.enrico.launcher3.hiddenapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.enrico.launcher3.DeleteDropTarget;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.enrico.launcher3.AppInfo.getAppInfoFlags;

/**
 * Created by Enrico on 20/08/2017.
 */

public class HiddenAppsUtils {

    public static boolean isPackageHidden(Context context, String packageName) {

        return hiddenPackages(context).contains(packageName);
    }

    public static Set<String> hiddenComponents(Context context) {
        //get hidden apps component names
        return Utilities.getPrefs(context).getStringSet(Utilities.HIDDEN_APPS_SET_KEY, null);
    }

    static ArrayList<String> hiddenPackages(Context context) {

        ArrayList<String> returnedPackages = new ArrayList<>();

        for (String cn : componentsArray(context)) {
            returnedPackages.add(packageName(cn));
        }

        return returnedPackages;
    }

    //get package name (com.example.something) from component name
    private static String packageName(String hiddenApp) {

        //get component name from hidden apps list
        ComponentName componentName = ComponentName.unflattenFromString(hiddenApp);

        return componentName.getPackageName();
    }

    static ArrayList<String> componentsArray(Context context) {

        final Set<String> hiddenApps = hiddenComponents(context);

        //add these components to an array list we pass to rv
        ArrayList<String> hiddenComponents = new ArrayList<>();

        if (hiddenApps != null) {

            for (String cn : hiddenApps) {

                hiddenComponents.add(cn);
            }
        }
        return hiddenComponents;
    }

    //method to hide apps
    public static boolean addToHideList(Launcher launcher, ItemInfo info) {

        Pair<ComponentName, Integer> componentInfo = getAppInfoFlags(info);

        ComponentName cn = null;

        if (componentInfo != null) {
            cn = componentInfo.first;
        }

        SharedPreferences prefs = Utilities.getPrefs(launcher.getApplicationContext());

        Set<String> hiddenApps = prefs.getStringSet(Utilities.HIDDEN_APPS_SET_KEY, null);

        if (hiddenApps == null) {
            hiddenApps = new HashSet<>();
        }

        if (cn != null) {
            hiddenApps.add(cn.flattenToString());
        }

        prefs.edit().putStringSet(Utilities.HIDDEN_APPS_SET_KEY, hiddenApps).apply();

        //remove the dragged item from workspace
        DeleteDropTarget.removeWorkspaceOrFolderItem(launcher, info, null);

        //remove also any other icon of the same item
        launcher.getWorkspace().removeItemsByMatcher(ItemInfoMatcher.ofComponent(cn), true);

        return true;
    }
}

package com.enrico.launcher3.customsettings;

import android.content.SharedPreferences;

import com.enrico.launcher3.BoardTitleUtils;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.LauncherAppState;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.icons.IconUtils;
import com.enrico.launcher3.icons.IconsHandler;
import com.enrico.launcher3.simplegestures.GesturesUtils;

public class CustomSettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static CustomSettings instance;
    private Launcher mLauncher;

    private CustomSettings(Launcher launcher) {
        SharedPreferences prefs = Utilities.getPrefs(launcher);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mLauncher = launcher;
    }

    public static void init(Launcher launcher) {
        instance = new CustomSettings(launcher);
    }

    public static CustomSettings getInstance() {
        return instance;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        switch (key) {
            case Utilities.THEME_KEY:
                Launcher.mustRecreate();
                break;

            case GesturesUtils.FLING_DOWN_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.LIGHT_BARS_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.PINCH_OVERVIEW_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.CHOOSE_THEME_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.PREDICTIVE_APPS_PREFERENCE_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.RESTORE_HIDDEN_APPS:
                reloadAll();
                break;

            case IconUtils.BADGE_POSITION_KEY:
                reloadAll();
                break;

            case IconUtils.BADGE_SHADOW_KEY:
                reloadAll();
                break;

            case Utilities.COLOR_FOLDERS_KEY:
                Launcher.mustRecreate();
                break;

            case Utilities.HOTSEAT_KEY:
                Launcher.mustRecreate();
                break;

            case BoardTitleUtils.BOARD_TITLE_KEY:
                Launcher.mustRecreate();
                break;

            case IconUtils.ROUND_ICONS_KEY:
                IconsHandler.switchIconPacks("", mLauncher);
                break;
        }
    }

    private void reloadAll() {
        LauncherAppState app = LauncherAppState.getInstanceNoCreate();
        if (app != null) {
            app.getModel().forceReload();
        }
    }
}


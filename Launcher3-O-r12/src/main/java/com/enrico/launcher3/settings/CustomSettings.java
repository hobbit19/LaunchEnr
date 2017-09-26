package com.enrico.launcher3.settings;

import android.content.SharedPreferences;

import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.board.BoardUtils;
import com.enrico.launcher3.icons.IconsManager;
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
            case PreferenceKeys.THEME_PREFERENCE_KEY:
                Launcher.setRecreate();
                break;

            case GesturesUtils.FLING_DOWN_KEY:
                Launcher.setRecreate();
                break;

            case BoardUtils.BOARD_TITLE_KEY:
                Launcher.setRecreate();
                break;

            case PreferenceKeys.LIGHT_STATUS_BAR_PREFERENCE_KEY:
                Launcher.setRecreate();
                break;

            case PreferenceKeys.CHOOSE_THEME_PREFERENCE_KEY:
                Launcher.setRecreate();
                break;

            case PreferenceKeys.HOTSEAT_COLOR_PREFERENCE_KEY:
                Launcher.setUpdateHotseatColor();
                break;

            case PreferenceKeys.COLOR_FOLDERS_PREFERENCE_KEY:
                Launcher.setRecreate();
                break;

            case IconsManager.ROUND_ICONS_KEY:
                IconsManager.switchIconPacks("", mLauncher);
                break;

            case BoardUtils.KEY_CUSTOM_APPS_SET:
                mLauncher.getBoardPanel().findViewById(R.id.customAppRecyclerView).invalidate();
                break;
        }
    }
}


package com.enrico.launcher3.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

/**
 * Accessibility delegate with actions pointing to various Overview entry points.
 */
public class OverviewAccessibilityDelegate extends AccessibilityDelegate {

    private static final int OVERVIEW = R.string.accessibility_action_overview;
    private static final int WALLPAPERS = R.string.wallpaper_button_text;
    private static final int WIDGETS = R.string.widget_button_text;
    private static final int SETTINGS = R.string.settings_button_text;

    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);

        Context context = host.getContext();
        info.addAction(new AccessibilityAction(OVERVIEW, context.getText(OVERVIEW)));

        if (Utilities.isWallpaperAllowed(context)) {
            info.addAction(new AccessibilityAction(WALLPAPERS, context.getText(WALLPAPERS)));
        }
        info.addAction(new AccessibilityAction(WIDGETS, context.getText(WIDGETS)));
        info.addAction(new AccessibilityAction(SETTINGS, context.getText(SETTINGS)));
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        Launcher launcher = Launcher.getLauncher(host.getContext());
        if (action == OVERVIEW) {
            launcher.showOverviewMode(true);
            return true;
        } else if (action == WALLPAPERS) {
            launcher.onClickWallpaperPicker(host);
            return true;
        } else if (action == WIDGETS) {
            launcher.onClickAddWidgetButton(host);
            return true;
        } else if (action == SETTINGS) {
            launcher.onClickSettingsButton();
            return true;
        }
        return super.performAccessibilityAction(host, action, args);
    }
}

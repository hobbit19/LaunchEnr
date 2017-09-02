package com.enrico.launcher3.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.LauncherModel;
import com.enrico.launcher3.LauncherSettings;
import com.enrico.launcher3.R;
import com.enrico.launcher3.ShortcutInfo;
import com.enrico.launcher3.shortcuts.DeepShortcutView;

import java.util.ArrayList;

/**
 * Extension of {@link LauncherAccessibilityDelegate} with actions specific to shortcuts in
 * deep shortcuts menu.
 */
public class ShortcutMenuAccessibilityDelegate extends LauncherAccessibilityDelegate {

    public ShortcutMenuAccessibilityDelegate(Launcher launcher) {
        super(launcher);
    }

    @Override
    protected void addActions(View host, AccessibilityNodeInfo info) {
        info.addAction(mActions.get(ADD_TO_WORKSPACE));
    }

    @Override
    public boolean performAction(View host, ItemInfo item, int action) {
        if (action == ADD_TO_WORKSPACE) {
            if (!(host.getParent() instanceof DeepShortcutView)) {
                return false;
            }
            final ShortcutInfo info = ((DeepShortcutView) host.getParent()).getFinalInfo();
            final int[] coordinates = new int[2];
            final long screenId = findSpaceOnWorkspace(item, coordinates);
            Runnable onComplete = new Runnable() {
                @Override
                public void run() {
                    LauncherModel.addItemToDatabase(mLauncher, info,
                            LauncherSettings.Favorites.CONTAINER_DESKTOP,
                            screenId, coordinates[0], coordinates[1]);
                    ArrayList<ItemInfo> itemList = new ArrayList<>();
                    itemList.add(info);
                    mLauncher.bindItems(itemList, 0, itemList.size(), true);
                    mLauncher.closeShortcutsContainer();
                    announceConfirmation(R.string.item_added_to_workspace);
                }
            };

            if (!mLauncher.showWorkspace(true, onComplete)) {
                onComplete.run();
            }
            return true;
        }
        return false;
    }
}

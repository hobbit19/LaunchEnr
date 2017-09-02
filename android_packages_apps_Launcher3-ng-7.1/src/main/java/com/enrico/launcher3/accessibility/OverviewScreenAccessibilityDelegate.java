package com.enrico.launcher3.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.Workspace;

public class OverviewScreenAccessibilityDelegate extends AccessibilityDelegate {

    private static final int MOVE_BACKWARD = R.id.action_move_screen_backwards;
    private static final int MOVE_FORWARD = R.id.action_move_screen_forwards;

    private final SparseArray<AccessibilityAction> mActions = new SparseArray<>();
    private final Workspace mWorkspace;

    public OverviewScreenAccessibilityDelegate(Workspace workspace) {
        mWorkspace = workspace;

        Context context = mWorkspace.getContext();
        boolean isRtl = Utilities.isRtl(context.getResources());
        mActions.put(MOVE_BACKWARD, new AccessibilityAction(MOVE_BACKWARD,
                context.getText(isRtl ? R.string.action_move_screen_right :
                    R.string.action_move_screen_left)));
        mActions.put(MOVE_FORWARD, new AccessibilityAction(MOVE_FORWARD,
                context.getText(isRtl ? R.string.action_move_screen_left :
                    R.string.action_move_screen_right)));
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (host != null) {
            if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS ) {
                int index = mWorkspace.indexOfChild(host);
                mWorkspace.setCurrentPage(index);
            } else if (action == MOVE_FORWARD) {
                movePage(mWorkspace.indexOfChild(host) + 1, host);
                return true;
            } else if (action == MOVE_BACKWARD) {
                movePage(mWorkspace.indexOfChild(host) - 1, host);
                return true;
            }
        }

        return super.performAccessibilityAction(host, action, args);
    }

    private void movePage(int finalIndex, View view) {
        mWorkspace.onStartReordering();
        mWorkspace.removeView(view);
        mWorkspace.addView(view, finalIndex);
        mWorkspace.onEndReordering();
        mWorkspace.announceForAccessibility(mWorkspace.getContext().getText(R.string.screen_moved));

        mWorkspace.updateAccessibilityFlags();
        view.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);

        int index = mWorkspace.indexOfChild(host);
        if (index < mWorkspace.getChildCount() - 1) {
            info.addAction(mActions.get(MOVE_FORWARD));
        }

        int startIndex = mWorkspace.numCustomPages();
        if (index > startIndex) {
            info.addAction(mActions.get(MOVE_BACKWARD));
        }
    }
}

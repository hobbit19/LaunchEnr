package com.enrico.launcher3.accessibility;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.enrico.launcher3.Launcher;

/**
 * Periodically sends accessibility events to announce ongoing state changed. Based on the
 * implementation in ProgressBar.
 */
public class DragViewStateAnnouncer implements Runnable {

    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;

    private final View mTargetView;

    private DragViewStateAnnouncer(View view) {
        mTargetView = view;
    }

    public void announce(CharSequence msg) {
        mTargetView.setContentDescription(msg);
        mTargetView.removeCallbacks(this);
        mTargetView.postDelayed(this, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    public void cancel() {
        mTargetView.removeCallbacks(this);
    }

    @Override
    public void run() {
        mTargetView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    public void completeAction(int announceResId) {
        cancel();
        Launcher launcher = Launcher.getLauncher(mTargetView.getContext());
        launcher.getDragLayer().announceForAccessibility(launcher.getText(announceResId));
    }

    public static DragViewStateAnnouncer createFor(View v) {
        if (((AccessibilityManager) v.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE))
                .isEnabled()) {
            return new DragViewStateAnnouncer(v);
        } else {
            return null;
        }
    }
}

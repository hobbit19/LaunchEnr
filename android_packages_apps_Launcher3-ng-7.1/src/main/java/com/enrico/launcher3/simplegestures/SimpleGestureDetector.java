package com.enrico.launcher3.simplegestures;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.Utilities;

public class SimpleGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private Launcher mLauncher;

    public SimpleGestureDetector(Launcher mLauncher) {
        this.mLauncher = mLauncher;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityX, float velocityY) {

        if (!mLauncher.getWorkspace().isInOverviewMode()) {
            GesturesUtils.resolveFlingDownTo(mLauncher);
        }

        return false;
    }

    // event when double tap occurs
    @Override
    public boolean onDoubleTap(MotionEvent e) {

        if (Utilities.isDoubleTapToLockEnabled(mLauncher) && !mLauncher.getWorkspace().isInOverviewMode()) {
            GesturesUtils.lockDevice(mLauncher);
        }

        return true;
    }

}

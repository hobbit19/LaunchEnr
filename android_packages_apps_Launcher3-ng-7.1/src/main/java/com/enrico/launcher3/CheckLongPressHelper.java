package com.enrico.launcher3;

import android.view.View;

import com.enrico.launcher3.util.Thunk;

public class CheckLongPressHelper {

    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 300;

    @Thunk
    private View mView;
    @Thunk
    private View.OnLongClickListener mListener;
    @Thunk
    private boolean mHasPerformedLongPress;
    private int mLongPressTimeout = DEFAULT_LONG_PRESS_TIMEOUT;
    private CheckForLongPress mPendingCheckForLongPress;

    public CheckLongPressHelper(View v) {
        mView = v;
    }

    public CheckLongPressHelper(View v, View.OnLongClickListener listener) {
        mView = v;
        mListener = listener;
    }

    /**
     * Overrides the default long press timeout.
     */
    void setLongPressTimeout(int longPressTimeout) {
        mLongPressTimeout = longPressTimeout;
    }

    public void postCheckForLongPress() {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mView.postDelayed(mPendingCheckForLongPress, mLongPressTimeout);
    }

    public void cancelLongPress() {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress != null) {
            mView.removeCallbacks(mPendingCheckForLongPress);
            mPendingCheckForLongPress = null;
        }
    }

    boolean hasPerformedLongPress() {
        return mHasPerformedLongPress;
    }

    private class CheckForLongPress implements Runnable {
        public void run() {
            if ((mView.getParent() != null) && mView.hasWindowFocus()
                    && !mHasPerformedLongPress) {
                boolean handled;
                if (mListener != null) {
                    handled = mListener.onLongClick(mView);
                } else {
                    handled = mView.performLongClick();
                }
                if (handled) {
                    mView.setPressed(false);
                    mHasPerformedLongPress = true;
                }
            }
        }
    }
}

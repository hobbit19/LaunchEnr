package com.enrico.launcher3.util;

import android.graphics.Rect;

/**
 * Extension of {@link PillRevealOutlineProvider} which only changes the width of the pill.
 */
public class PillWidthRevealOutlineProvider extends PillRevealOutlineProvider {

    private final int mStartLeft;
    private final int mStartRight;

    public PillWidthRevealOutlineProvider(Rect pillRect, int left, int right) {
        super(0, 0, pillRect);
        mOutline.set(pillRect);
        mStartLeft = left;
        mStartRight = right;
    }

    @Override
    public void setProgress(float progress) {
        mOutline.left = (int) (progress * mPillRect.left + (1 - progress) * mStartLeft);
        mOutline.right = (int) (progress * mPillRect.right + (1 - progress) * mStartRight);
    }
}

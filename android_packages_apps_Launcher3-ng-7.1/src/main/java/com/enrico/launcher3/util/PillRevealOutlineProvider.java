package com.enrico.launcher3.util;

import android.graphics.Rect;
import android.view.ViewOutlineProvider;

/**
 * A {@link ViewOutlineProvider} that animates a reveal in a "pill" shape.
 * A pill is simply a round rect, but we assume the width is greater than
 * the height and that the radius is equal to half the height.
 */
public class PillRevealOutlineProvider extends RevealOutlineAnimation {

    Rect mPillRect;
    private int mCenterX;
    private int mCenterY;

    /**
     * @param x        reveal center x
     * @param y        reveal center y
     * @param pillRect round rect that represents the final pill shape
     */
    public PillRevealOutlineProvider(int x, int y, Rect pillRect) {
        mCenterX = x;
        mCenterY = y;
        mPillRect = pillRect;
        mOutlineRadius = pillRect.height() / 2f;
    }

    @Override
    public boolean shouldRemoveElevationDuringAnimation() {
        return false;
    }

    @Override
    public void setProgress(float progress) {
        // Assumes width is greater than height.
        int centerToEdge = Math.max(mCenterX, mPillRect.width() - mCenterX);
        int currentSize = (int) (progress * centerToEdge);

        // Bound the outline to the final pill shape defined by mPillRect.
        mOutline.left = Math.max(mPillRect.left, mCenterX - currentSize);
        mOutline.top = Math.max(mPillRect.top, mCenterY - currentSize);
        mOutline.right = Math.min(mPillRect.right, mCenterX + currentSize);
        mOutline.bottom = Math.min(mPillRect.bottom, mCenterY + currentSize);
        mOutlineRadius = mOutline.height() / 2;
    }
}

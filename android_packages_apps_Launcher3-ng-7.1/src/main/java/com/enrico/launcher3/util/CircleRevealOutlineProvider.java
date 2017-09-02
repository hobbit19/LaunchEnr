package com.enrico.launcher3.util;

public class CircleRevealOutlineProvider extends RevealOutlineAnimation {

    private int mCenterX;
    private int mCenterY;
    private float mRadius0;
    private float mRadius1;

    /**
     * @param x  reveal center x
     * @param y  reveal center y
     * @param r0 initial radius
     * @param r1 final radius
     */
    public CircleRevealOutlineProvider(int x, int y, float r0, float r1) {
        mCenterX = x;
        mCenterY = y;
        mRadius0 = r0;
        mRadius1 = r1;
    }

    @Override
    public boolean shouldRemoveElevationDuringAnimation() {
        return true;
    }

    @Override
    public void setProgress(float progress) {
        mOutlineRadius = (1 - progress) * mRadius0 + progress * mRadius1;

        mOutline.left = (int) (mCenterX - mOutlineRadius);
        mOutline.top = (int) (mCenterY - mOutlineRadius);
        mOutline.right = (int) (mCenterX + mOutlineRadius);
        mOutline.bottom = (int) (mCenterY + mOutlineRadius);
    }
}

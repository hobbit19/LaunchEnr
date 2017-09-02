package com.enrico.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

public class ClickShadowView extends View {

    private static final int SHADOW_SIZE_FACTOR = 3;
    private static final int SHADOW_LOW_ALPHA = 30;
    private static final int SHADOW_HIGH_ALPHA = 60;

    private final Paint mPaint;

    @ViewDebug.ExportedProperty(category = "launcher")
    private final float mShadowOffset;
    @ViewDebug.ExportedProperty(category = "launcher")
    private final float mShadowPadding;

    private Bitmap mBitmap;

    public ClickShadowView(Context context) {
        super(context);
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mPaint.setColor(Color.BLACK);

        mShadowPadding = getResources().getDimension(R.dimen.blur_size_click_shadow);
        mShadowOffset = getResources().getDimension(R.dimen.click_shadow_high_shift);
    }

    /**
     * @return extra space required by the view to show the shadow.
     */
    public int getExtraSize() {
        return (int) (SHADOW_SIZE_FACTOR * mShadowPadding);
    }

    /**
     * Applies the new bitmap.
     *
     * @return true if the view was invalidated.
     */
    public boolean setBitmap(Bitmap b) {
        if (b != mBitmap) {
            mBitmap = b;
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            mPaint.setAlpha(SHADOW_LOW_ALPHA);
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            mPaint.setAlpha(SHADOW_HIGH_ALPHA);
            canvas.drawBitmap(mBitmap, 0, mShadowOffset, mPaint);
        }
    }

    public void animateShadow() {
        setAlpha(0);
        animate().alpha(1)
                .setDuration(FastBitmapDrawable.CLICK_FEEDBACK_DURATION)
                .setInterpolator(FastBitmapDrawable.CLICK_FEEDBACK_INTERPOLATOR)
                .start();
    }

    /**
     * Aligns the shadow with {@param view}
     *
     * @param viewParent immediate parent of {@param view}. It must be a sibling of this view.
     */
    public void alignWithIconView(BubbleTextView view, ViewGroup viewParent, View clipAgainstView) {
        float leftShift = view.getLeft() + viewParent.getLeft() - getLeft();
        float topShift = view.getTop() + viewParent.getTop() - getTop();
        int iconWidth = view.getRight() - view.getLeft();
        int iconHeight = view.getBottom() - view.getTop();
        int iconHSpace = iconWidth - view.getCompoundPaddingRight() - view.getCompoundPaddingLeft();
        float drawableWidth = view.getIcon().getBounds().width();

        if (clipAgainstView != null) {
            // Set the bounds to clip against
            int[] coords = new int[]{0, 0};
            Utilities.getDescendantCoordRelativeToAncestor(clipAgainstView, (View) getParent(),
                    coords, false);
            int clipLeft = (int) Math.max(0, coords[0] - leftShift - mShadowPadding);
            int clipTop = (int) Math.max(0, coords[1] - topShift - mShadowPadding);
            setClipBounds(new Rect(clipLeft, clipTop, clipLeft + iconWidth, clipTop + iconHeight));
        } else {
            // Reset the clip bounds
            setClipBounds(null);
        }

        setTranslationX(leftShift
                + viewParent.getTranslationX()
                + view.getCompoundPaddingLeft() * view.getScaleX()
                + (iconHSpace - drawableWidth) * view.getScaleX() / 2  /* drawable gap */
                + iconWidth * (1 - view.getScaleX()) / 2  /* gap due to scale */
                - mShadowPadding  /* extra shadow size */
        );
        setTranslationY(topShift
                + viewParent.getTranslationY()
                + view.getPaddingTop() * view.getScaleY()  /* drawable gap */
                + view.getHeight() * (1 - view.getScaleY()) / 2  /* gap due to scale */
                - mShadowPadding  /* extra shadow size */
        );
    }
}

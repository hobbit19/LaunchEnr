package com.enrico.launcher3.allapps;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.view.Gravity;

import com.enrico.launcher3.R;

class TransformedImageDrawable {

    private Drawable mImage;
    private float mXPercent;
    private float mYPercent;
    private int mGravity;
    private int mAlpha;

    TransformedImageDrawable(Context context, Resources res, int resourceId, float xPct, float yPct,
                             int gravity) {

        mImage = res.getDrawable(resourceId, context.getTheme());
        mXPercent = xPct;
        mYPercent = yPct;
        mGravity = gravity;
    }

    public int getAlpha() {
        return mAlpha;
    }

    public void setAlpha(int alpha) {
        mImage.setAlpha(alpha);
        mAlpha = alpha;
    }

    void updateBounds(Rect bounds) {
        int width = mImage.getIntrinsicWidth();
        int height = mImage.getIntrinsicHeight();
        int left = bounds.left + (int) (mXPercent * bounds.width());
        int top = bounds.top + (int) (mYPercent * bounds.height());
        if ((mGravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
            left -= (width / 2);
        }
        if ((mGravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL) {
            top -= (height / 2);
        }
        mImage.setBounds(left, top, left + width, top + height);
    }

    public void draw(Canvas canvas) {

        int c = canvas.save();
        mImage.draw(canvas);
        canvas.restoreToCount(c);
    }
}

public class AllAppsBackgroundDrawable extends Drawable {

    private final TransformedImageDrawable mHand;
    private final TransformedImageDrawable[] mIcons;
    private final int mWidth;
    private final int mHeight;

    private ObjectAnimator mBackgroundAnim;

    public AllAppsBackgroundDrawable(Context context) {
        Resources res = context.getResources();
        mHand = new TransformedImageDrawable(context, res, R.drawable.ic_all_apps_bg_hand,
                0.575f, 0.f, Gravity.CENTER_HORIZONTAL);
        mIcons = new TransformedImageDrawable[4];
        mIcons[0] = new TransformedImageDrawable(context, res, R.drawable.ic_all_apps_bg_icon_1,
                0.375f, 0, Gravity.CENTER_HORIZONTAL);
        mIcons[1] = new TransformedImageDrawable(context, res, R.drawable.ic_all_apps_bg_icon_2,
                0.3125f, 0.2f, Gravity.CENTER_HORIZONTAL);
        mIcons[2] = new TransformedImageDrawable(context, res, R.drawable.ic_all_apps_bg_icon_3,
                0.475f, 0.26f, Gravity.CENTER_HORIZONTAL);
        mIcons[3] = new TransformedImageDrawable(context, res, R.drawable.ic_all_apps_bg_icon_4,
                0.7f, 0.125f, Gravity.CENTER_HORIZONTAL);
        mWidth = res.getDimensionPixelSize(R.dimen.all_apps_background_canvas_width);
        mHeight = res.getDimensionPixelSize(R.dimen.all_apps_background_canvas_height);
    }

    void animateBgAlpha(float finalAlpha, int duration) {
        int finalAlphaI = (int) (finalAlpha * 255f);
        if (getAlpha() != finalAlphaI) {
            mBackgroundAnim = cancelAnimator(mBackgroundAnim);
            mBackgroundAnim = ObjectAnimator.ofInt(this, "alpha", finalAlphaI);
            mBackgroundAnim.setDuration(duration);
            mBackgroundAnim.start();
        }
    }

    void setBgAlpha(float finalAlpha) {
        int finalAlphaI = (int) (finalAlpha * 255f);
        if (getAlpha() != finalAlphaI) {
            mBackgroundAnim = cancelAnimator(mBackgroundAnim);
            setAlpha(finalAlphaI);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mHand.draw(canvas);

        for (TransformedImageDrawable transformedImageDrawable: mIcons) {
            transformedImageDrawable.draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mHand.updateBounds(bounds);

        for (TransformedImageDrawable transformedImageDrawable: mIcons) {
            transformedImageDrawable.updateBounds(bounds);
        }

        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mHand.getAlpha();
    }

    @Override
    @Keep
    public void setAlpha(int alpha) {
        mHand.setAlpha(alpha);

        for (TransformedImageDrawable transformedImageDrawable: mIcons) {
            transformedImageDrawable.setAlpha(alpha);
        }
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Do nothing
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private ObjectAnimator cancelAnimator(ObjectAnimator animator) {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
        }
        return null;
    }
}

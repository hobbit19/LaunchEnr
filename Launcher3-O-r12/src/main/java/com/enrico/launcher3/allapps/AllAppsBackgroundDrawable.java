/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * This is a custom composite drawable that has a fixed virtual size and dynamically lays out its
 * children images relatively within its bounds.  This way, we can reduce the memory usage of a
 * single, large sparsely populated image.
 */
public class AllAppsBackgroundDrawable extends Drawable {

    /**
     * A helper class to positon and orient a drawable to be drawn.
     */
    private static class TransformedImageDrawable {
        private Drawable mImage;
        private float mXPercent;
        private float mYPercent;
        private int mGravity;
        private int mAlpha;

        /**
         * @param gravity If one of the Gravity center values, the x and y offset will take the width
         *                and height of the image into account to center the image to the offset.
         */
        TransformedImageDrawable(Context context, Resources res, int resourceId, float xPct, float yPct,
                int gravity) {
            mImage = res.getDrawable(resourceId, context.getTheme());
            mXPercent = xPct;
            mYPercent = yPct;
            mGravity = gravity;
        }

        public void setAlpha(int alpha) {
            mImage.setAlpha(alpha);
            mAlpha = alpha;
        }

        public int getAlpha() {
            return mAlpha;
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
            mImage.draw(canvas);
        }

        public Rect getBounds() {
            return mImage.getBounds();
        }
    }

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

    /**
     * Animates the background alpha.
     */
    void animateBgAlpha(float finalAlpha, int duration) {
        int finalAlphaI = (int) (finalAlpha * 255f);
        if (getAlpha() != finalAlphaI) {
            mBackgroundAnim = cancelAnimator(mBackgroundAnim);
            mBackgroundAnim = ObjectAnimator.ofInt(this, "alpha", finalAlphaI);
            mBackgroundAnim.setDuration(duration);
            mBackgroundAnim.start();
        }
    }

    /**
     * Sets the background alpha immediately.
     */
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
        for (int i = 0; i < mIcons.length; i++) {
            mIcons[i].draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mHand.updateBounds(bounds);
        for (int i = 0; i < mIcons.length; i++) {
            mIcons[i].updateBounds(bounds);
        }
        invalidateSelf();
    }

    @Override
    @Keep
    public void setAlpha(int alpha) {
        mHand.setAlpha(alpha);
        for (int i = 0; i < mIcons.length; i++) {
            mIcons[i].setAlpha(alpha);
        }
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mHand.getAlpha();
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
            animator.cancel();
        }
        return null;
    }
}

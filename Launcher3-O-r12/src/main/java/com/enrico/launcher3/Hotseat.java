/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.enrico.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewDebug;
import android.widget.FrameLayout;

import com.enrico.launcher3.dynamicui.ExtractedColors;
import com.enrico.launcher3.settings.PreferencesState;
import com.enrico.launcher3.theme.ThemeUtils;

public class Hotseat extends FrameLayout {

    private CellLayout mContent;

    private Launcher mLauncher;

    @ViewDebug.ExportedProperty(category = "launcher")
    private final boolean mHasVerticalHotseat;

    @ViewDebug.ExportedProperty(category = "launcher")
    private int mBackgroundColor;
    @ViewDebug.ExportedProperty(category = "launcher")
    private ColorDrawable mBackground;
    private ValueAnimator mBackgroundColorAnimator;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
        mHasVerticalHotseat = mLauncher.getDeviceProfile().isVerticalBarLayout();
        mBackgroundColor = ColorUtils.setAlphaComponent(
                ThemeUtils.getAttrColor(context, android.R.attr.colorPrimary), 0);
        mBackground = new ColorDrawable(mBackgroundColor);
        setBackground(mBackground);
    }

    public CellLayout getLayout() {
        return mContent;
    }

    /**
     * Returns whether there are other icons than the all apps button in the hotseat.
     */
    public boolean hasIcons() {
        return mContent.getShortcutsAndWidgets().getChildCount() > 1;
    }

    /**
     * Registers the specified listener on the cell layout of the hotseat.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return mHasVerticalHotseat ? (mContent.getCountY() - y - 1) : x;
    }

    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return mHasVerticalHotseat ? 0 : rank;
    }

    int getCellYFromOrder(int rank) {
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DeviceProfile grid = mLauncher.getDeviceProfile();
        mContent = (CellLayout) findViewById(R.id.layout);
        if (grid.isVerticalBarLayout()) {
            mContent.setGridSize(1, grid.inv.numHotseatIcons);
        } else {
            mContent.setGridSize(grid.inv.numHotseatIcons, 1);
        }

        resetLayout();
    }

    void resetLayout() {
        mContent.removeAllViewsInLayout();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state or an accessible drag is in progress.
        return !mLauncher.getWorkspace().workspaceIconsCanBeDragged() &&
                !mLauncher.getAccessibilityDelegate().isInAccessibleDrag();
    }

    public void updateColor(ExtractedColors extractedColors, boolean animate) {
        if (!mHasVerticalHotseat) {
            int selectedIndex = PreferencesState.resolveHotseatColor(mLauncher);

            //set hotseat color
            int color;

            if (PreferencesState.isAccentColorHotseat(mLauncher)) {

                float m = 0.22f*255;
                int colorAccent = Launcher.mHotseatAccentColor;

                int alpha = Math.round(m);
                color = ColorUtils.setAlphaComponent(colorAccent, alpha);

            } else {

                color = extractedColors.getColor(selectedIndex, Color.TRANSPARENT);

            }

            if (mBackgroundColorAnimator != null) {
                mBackgroundColorAnimator.cancel();
            }
            if (!animate) {
                setBackgroundColor(color);
            } else {
                mBackgroundColorAnimator = ValueAnimator.ofInt(mBackgroundColor, color);
                mBackgroundColorAnimator.setEvaluator(new ArgbEvaluator());
                mBackgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mBackground.setColor((Integer) animation.getAnimatedValue());
                    }
                });
                mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mBackgroundColorAnimator = null;
                    }
                });
                mBackgroundColorAnimator.start();
            }
            mBackgroundColor = color;
        }
    }

    public void setBackgroundTransparent(boolean enable) {
        if (enable) {
            mBackground.setAlpha(0);
        } else {
            mBackground.setAlpha(255);
        }
    }

    public int getBackgroundDrawableColor() {
        return mBackgroundColor;
    }
}

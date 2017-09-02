package com.enrico.launcher3;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewPropertyAnimator;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.enrico.launcher3.dragndrop.DragController;
import com.enrico.launcher3.dragndrop.DragOptions;

/*
 * The top bar containing various drop targets: Delete/App Info/Uninstall/Hide.
 */
public class DropTargetBar extends LinearLayout implements DragController.DragListener {

    protected static final int DEFAULT_DRAG_FADE_DURATION = 175;
    protected static final TimeInterpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator();

    private final Runnable mFadeAnimationEndRunnable = new Runnable() {

        @Override
        public void run() {
            AccessibilityManager am = (AccessibilityManager)
                    getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            boolean accessibilityEnabled = am.isEnabled();
            AlphaUpdateListener.updateVisibility(DropTargetBar.this, accessibilityEnabled);
        }
    };

    @ViewDebug.ExportedProperty(category = "launcher")
    protected boolean mDeferOnDragEnd;

    @ViewDebug.ExportedProperty(category = "launcher")
    protected boolean mVisible = false;

    private ViewPropertyAnimator mCurrentAnimation;

    // Drop targets
    private ButtonDropTarget mDeleteDropTarget;
    private ButtonDropTarget mAppInfoDropTarget;
    private ButtonDropTarget mUninstallDropTarget;
    private ButtonDropTarget mEditDropTarget;

    private Activity activity;

    public DropTargetBar(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DropTargetBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get the individual components
        mDeleteDropTarget = findViewById(R.id.delete_target_text);
        mAppInfoDropTarget = findViewById(R.id.info_target_text);
        mUninstallDropTarget = findViewById(R.id.uninstall_target_text);
        mEditDropTarget = findViewById(R.id.edit_target_text);

        mDeleteDropTarget.setDropTargetBar(this);
        mAppInfoDropTarget.setDropTargetBar(this);
        mUninstallDropTarget.setDropTargetBar(this);
        mEditDropTarget.setDropTargetBar(this);

        // Initialize with hidden state
        setAlpha(0f);
    }

    public void setup(Activity activity, DragController dragController) {
        dragController.addDragListener(this);
        dragController.setFlingToDeleteDropTarget(mDeleteDropTarget);

        dragController.addDragListener(mDeleteDropTarget);
        dragController.addDragListener(mAppInfoDropTarget);
        dragController.addDragListener(mUninstallDropTarget);
        dragController.addDragListener(mEditDropTarget);

        dragController.addDropTarget(mDeleteDropTarget);
        dragController.addDropTarget(mAppInfoDropTarget);
        dragController.addDropTarget(mUninstallDropTarget);
        dragController.addDropTarget(mEditDropTarget);

        this.activity = activity;
    }

    private void animateToVisibility(final boolean isVisible) {

        if (mVisible != isVisible) {
            mVisible = isVisible;

            // Cancel any existing animation
            if (mCurrentAnimation != null) {
                mCurrentAnimation.cancel();
                mCurrentAnimation = null;
            }

            float finalAlpha = mVisible ? 1 : 0;
            if (Float.compare(getAlpha(), finalAlpha) != 0) {
                setVisibility(View.VISIBLE);
                mCurrentAnimation = animate().alpha(finalAlpha)
                        .setInterpolator(DEFAULT_INTERPOLATOR)
                        .setDuration(DEFAULT_DRAG_FADE_DURATION)
                        .withEndAction(mFadeAnimationEndRunnable);

                final int statusBarColor = isVisible ? Utilities.getColorAccent(activity) : Color.TRANSPARENT;

                mCurrentAnimation.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                        if (!isVisible) {
                            activity.getWindow().setStatusBarColor(statusBarColor);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        if (isVisible) {

                            int color = ColorUtils.setAlphaComponent(statusBarColor, 200);
                            activity.getWindow().setStatusBarColor(color);
                            setBackgroundColor(color);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        }
    }

    /*
     * DragController.DragListener implementation
     */
    @Override
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) {

        animateToVisibility(true);
    }

    /**
     * This is called to defer hiding the delete drop target until the drop animation has completed,
     * instead of hiding immediately when the drag has ended.
     */
    protected void deferOnDragEnd() {
        mDeferOnDragEnd = true;
    }

    @Override
    public void onDragEnd() {
        if (!mDeferOnDragEnd) {
            animateToVisibility(false);
        } else {
            mDeferOnDragEnd = false;
        }
    }
}

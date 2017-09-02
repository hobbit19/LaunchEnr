package com.enrico.launcher3.shortcuts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.enrico.launcher3.BubbleTextView;
import com.enrico.launcher3.R;
import com.enrico.launcher3.Utilities;

/**
 * A {@link BubbleTextView} that has the shortcut icon on the left and drag handle on the right.
 */
public class DeepShortcutTextView extends BubbleTextView {
    private final Rect mDragHandleBounds = new Rect();
    private final int mDragHandleWidth;
    private boolean mShouldPerformClick = true;

    public DeepShortcutTextView(Context context) {
        this(context, null, 0);
    }

    public DeepShortcutTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeepShortcutTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources resources = getResources();
        mDragHandleWidth = resources.getDimensionPixelSize(R.dimen.deep_shortcut_padding_end)
                + resources.getDimensionPixelSize(R.dimen.deep_shortcut_drag_handle_size)
                + resources.getDimensionPixelSize(R.dimen.deep_shortcut_drawable_padding) / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mDragHandleBounds.set(0, 0, mDragHandleWidth, getMeasuredHeight());
        if (!Utilities.isRtl(getResources())) {
            mDragHandleBounds.offset(getMeasuredWidth() - mDragHandleBounds.width(), 0);
        }
    }

    @Override
    protected void applyCompoundDrawables(Drawable icon) {
        // The icon is drawn in a separate view.
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Ignore clicks on the drag handle (long clicks still start the drag).
            mShouldPerformClick = !mDragHandleBounds.contains((int) ev.getX(), (int) ev.getY());
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return mShouldPerformClick && super.performClick();
    }
}

package com.enrico.launcher3.keyboard;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import android.view.View.OnFocusChangeListener;

/**
 * {@link ItemDecoration} for drawing and animating focused view background.
 */
public class FocusedItemDecorator extends ItemDecoration {

    private FocusIndicatorHelper mHelper;

    public FocusedItemDecorator(View container) {
        mHelper = new FocusIndicatorHelper(container) {

            @Override
            public void viewToRect(View v, Rect outRect) {
                outRect.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            }
        };
    }

    public OnFocusChangeListener getFocusListener() {
        return mHelper;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        mHelper.draw(c);
    }
}

package com.enrico.launcher3.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

/**
 * {@link DynamicDrawableSpan} which draws a drawable tinted with the current paint color.
 */
public class TintedDrawableSpan extends DynamicDrawableSpan {

    private final Drawable mDrawable;
    private int mOldTint;

    public TintedDrawableSpan(Context context, int resourceId) {
        super(ALIGN_BOTTOM);
        mDrawable = context.getDrawable(resourceId);
        mOldTint = 0;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        fm = fm == null ? paint.getFontMetricsInt() : fm;
        int iconSize = fm.bottom - fm.top;
        mDrawable.setBounds(0, 0, iconSize, iconSize);
        return super.getSize(paint, text, start, end, fm);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int color = paint.getColor();
        if (mOldTint != color) {
            mOldTint = color;
            mDrawable.setTint(mOldTint);
        }
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
    }

    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }
}

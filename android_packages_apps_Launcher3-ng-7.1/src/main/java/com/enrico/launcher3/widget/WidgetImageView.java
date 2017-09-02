package com.enrico.launcher3.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * View that draws a bitmap horizontally centered. If the image width is greater than the view
 * width, the image is scaled down appropriately.
 */
public class WidgetImageView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF mDstRectF = new RectF();
    private Bitmap mBitmap;

    public WidgetImageView(Context context) {
        super(context);
    }

    public WidgetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            updateDstRectF();
            canvas.drawBitmap(mBitmap, null, mDstRectF, mPaint);
        }
    }

    /**
     * Prevents the inefficient alpha view rendering.
     */
    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    private void updateDstRectF() {
        if (mBitmap.getWidth() > getWidth()) {
            float scale = ((float) getWidth()) / mBitmap.getWidth();
            mDstRectF.set(0, 0, getWidth(), scale * mBitmap.getHeight());
        } else {
            mDstRectF.set(
                    (getWidth() - mBitmap.getWidth()) * 0.5f,
                    0,
                    (getWidth() + mBitmap.getWidth()) * 0.5f,
                    mBitmap.getHeight());
        }
    }

    /**
     * @return the bounds where the image was drawn.
     */
    public Rect getBitmapBounds() {
        updateDstRectF();
        Rect rect = new Rect();
        mDstRectF.round(rect);
        return rect;
    }
}

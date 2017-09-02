package com.enrico.launcher3.shortcuts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.enrico.launcher3.HolographicOutlineHelper;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.Utilities;
import com.enrico.launcher3.graphics.DragPreviewProvider;

/**
 * Extension of {@link DragPreviewProvider} which generates bitmaps scaled to the default icon size.
 */
class ShortcutDragPreviewProvider extends DragPreviewProvider {

    private final Point mPositionShift;

    ShortcutDragPreviewProvider(View icon, Point shift) {
        super(icon);
        mPositionShift = shift;
    }

    @Override
    public Bitmap createDragOutline(Canvas canvas) {
        Bitmap b = drawScaledPreview(canvas, Bitmap.Config.ALPHA_8);

        HolographicOutlineHelper.obtain(mView.getContext())
                .applyExpensiveOutlineWithBlur(b, canvas);
        canvas.setBitmap(null);
        return b;
    }

    @Override
    public Bitmap createDragBitmap(Canvas canvas) {
        Bitmap b = drawScaledPreview(canvas, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(null);
        return b;
    }

    private Bitmap drawScaledPreview(Canvas canvas, Bitmap.Config config) {
        Drawable d = mView.getBackground();
        Rect bounds = getDrawableBounds(d);

        int size = Launcher.getLauncher(mView.getContext()).getDeviceProfile().iconSizePx;

        final Bitmap b = Bitmap.createBitmap(
                size + DRAG_BITMAP_PADDING,
                size + DRAG_BITMAP_PADDING,
                config);

        canvas.setBitmap(b);
        canvas.save();
        canvas.translate(DRAG_BITMAP_PADDING / 2, DRAG_BITMAP_PADDING / 2);
        canvas.scale(((float) size) / bounds.width(), ((float) size) / bounds.height(), 0, 0);
        canvas.translate(bounds.left, bounds.top);
        d.draw(canvas);
        canvas.restore();
        return b;
    }

    @Override
    public float getScaleAndPosition(Bitmap preview, int[] outPos) {
        Launcher launcher = Launcher.getLauncher(mView.getContext());
        int iconSize = getDrawableBounds(mView.getBackground()).width();
        float scale = launcher.getDragLayer().getLocationInDragLayer(mView, outPos);

        int iconLeft = mView.getPaddingStart();
        if (Utilities.isRtl(mView.getResources())) {
            iconLeft = mView.getWidth() - iconSize - iconLeft;
        }

        outPos[0] += Math.round(scale * iconLeft + (scale * iconSize - preview.getWidth()) / 2 +
                mPositionShift.x);
        outPos[1] += Math.round((scale * mView.getHeight() - preview.getHeight()) / 2
                + mPositionShift.y);
        float size = launcher.getDeviceProfile().iconSizePx;
        return scale * iconSize / size;
    }
}

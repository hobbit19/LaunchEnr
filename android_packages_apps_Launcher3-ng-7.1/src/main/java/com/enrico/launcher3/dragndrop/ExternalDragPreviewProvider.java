package com.enrico.launcher3.dragndrop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.enrico.launcher3.DeviceProfile;
import com.enrico.launcher3.HolographicOutlineHelper;
import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.graphics.DragPreviewProvider;

/**
 * Extension of {@link DragPreviewProvider} which provides a dummy outline when drag starts from
 * a different window.
 * It just draws an empty circle to a placeholder outline.
 */
class ExternalDragPreviewProvider extends DragPreviewProvider {

    private final Launcher mLauncher;

    private final int[] mOutlineSize;

    ExternalDragPreviewProvider(Launcher launcher, ItemInfo addInfo) {
        super(null);
        mLauncher = launcher;

        mOutlineSize = mLauncher.getWorkspace().estimateItemSize(addInfo, false);
    }

    Rect getPreviewBounds() {
        Rect rect = new Rect();
        DeviceProfile dp = mLauncher.getDeviceProfile();
        rect.left = DRAG_BITMAP_PADDING / 2;
        rect.top = (mOutlineSize[1] - dp.cellHeightPx) / 2;
        rect.right = rect.left + dp.iconSizePx;
        rect.bottom = rect.top + dp.iconSizePx;
        return rect;
    }

    @Override
    public Bitmap createDragOutline(Canvas canvas) {
        final Bitmap b = Bitmap.createBitmap(mOutlineSize[0], mOutlineSize[1], Bitmap.Config.ALPHA_8);
        canvas.setBitmap(b);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        // Use 0.9f times the radius for the actual circle to account for icon normalization.
        float radius = getPreviewBounds().width() * 0.5f;
        canvas.drawCircle(DRAG_BITMAP_PADDING / 2 + radius,
                DRAG_BITMAP_PADDING / 2 + radius, radius * 0.9f, paint);

        HolographicOutlineHelper.obtain(mLauncher).applyExpensiveOutlineWithBlur(b, canvas);
        canvas.setBitmap(null);
        return b;
    }
}

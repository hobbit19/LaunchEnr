package com.enrico.launcher3.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import com.enrico.launcher3.HolographicOutlineHelper;
import com.enrico.launcher3.Launcher;
import com.enrico.launcher3.PendingAddItemInfo;
import com.enrico.launcher3.Workspace;
import com.enrico.launcher3.graphics.DragPreviewProvider;

/**
 * Extension of {@link DragPreviewProvider} with logic specific to pending widgets/shortcuts
 * dragged from the widget tray.
 */
class PendingItemPreviewProvider extends DragPreviewProvider {

    private final PendingAddItemInfo mAddInfo;
    private final Bitmap mPreviewBitmap;

    PendingItemPreviewProvider(View view, PendingAddItemInfo addInfo, Bitmap preview) {
        super(view);
        mAddInfo = addInfo;
        mPreviewBitmap = preview;
    }

    @Override
    public Bitmap createDragOutline(Canvas canvas) {
        Workspace workspace = Launcher.getLauncher(mView.getContext()).getWorkspace();
        int[] size = workspace.estimateItemSize(mAddInfo, false);

        int w = size[0];
        int h = size[1];
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        canvas.setBitmap(b);

        Rect src = new Rect(0, 0, mPreviewBitmap.getWidth(), mPreviewBitmap.getHeight());
        float scaleFactor = Math.min((w - DRAG_BITMAP_PADDING) / (float) mPreviewBitmap.getWidth(),
                (h - DRAG_BITMAP_PADDING) / (float) mPreviewBitmap.getHeight());
        int scaledWidth = (int) (scaleFactor * mPreviewBitmap.getWidth());
        int scaledHeight = (int) (scaleFactor * mPreviewBitmap.getHeight());
        Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);

        // center the image
        dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);

        canvas.drawBitmap(mPreviewBitmap, src, dst, null);

        // Don't clip alpha values for the drag outline if we're using the default widget preview
        boolean clipAlpha = !(mAddInfo instanceof PendingAddWidgetInfo &&
                (((PendingAddWidgetInfo) mAddInfo).previewImage == 0));
        HolographicOutlineHelper.obtain(mView.getContext())
                .applyExpensiveOutlineWithBlur(b, canvas, clipAlpha);
        canvas.setBitmap(null);

        return b;
    }
}
